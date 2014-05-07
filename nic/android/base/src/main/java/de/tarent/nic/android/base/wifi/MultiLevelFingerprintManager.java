package de.tarent.nic.android.base.wifi;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import de.tarent.nic.android.base.AbstractMapActivity;
import de.tarent.nic.android.base.R;
import de.tarent.nic.android.base.map.IndoorMap;
import de.tarent.nic.android.base.mapmatching.WayManager;
import de.tarent.nic.android.base.task.CachedDownloadFingerprintsTask;
import de.tarent.nic.android.base.task.CachedDownloadMapResourceTask;
import de.tarent.nic.android.base.task.DownloadListener;
import de.tarent.nic.android.base.task.DownloadMapDataTask;
import de.tarent.nic.android.base.task.DownloadMapResourceTask;
import de.tarent.nic.android.base.task.DownloadTask;
import de.tarent.nic.android.base.task.OsmParser;
import de.tarent.nic.android.base.tileprovider.TileProviderFactory;
import de.tarent.nic.entities.Fingerprint;
import de.tarent.nic.entities.Histogram;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.tracker.wifi.HistogramConsumer;
import de.tarent.nic.tracker.wifi.RouterLevelHistogram;
import de.tarent.nic.tracker.wifi.divergence.KullbackLeibler;
import de.tarent.nic.tracker.wifi.divergence.RouterLevelDivergence;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The MultiLevelFingerprintManager can handle multiple maps, which are several levels of one building combined,
 * with.
 * TODO: perhaps the special case of the single-map-FingerprintManager should be generalised to a
 *       MultiLevelFingerprintManager with just one level.
 * TODO: perhaps this is becoming more of a MapManager, than a FingerprintManager.
 */
public class MultiLevelFingerprintManager extends FingerprintManager implements DownloadListener, HistogramConsumer {

    // There are 3 downloads per level (fingerprints + mapresource + edges):
    protected static final int DOWNLOADS_PER_MAP = 3;

    /**
     * A mapping of level-number to IndoorMap. It's not an array because we want to allow negative indices too.
     */
    protected Map<Integer, IndoorMap> maps;


    // TODO: This is, of course, just a temporary test-template:
    //       It must conform to what the GetMapListTask supplies and it should work for all map names. for that is
    //       needs to include a placeholder for the "base" name.
    private String mapNameTemplate = "[baseMapName]-floor-[level]";

    /**
     * The number of the lowest level. Levels should be continuous, i.e. have no gaps between minLevel and maxLevel.
     */
    private int minLevel = 0;

    /**
     * The number of the highest level. Levels should be continuous, i.e. have no gaps between minLevel and maxLevel.
     */
    private int maxLevel = 9;

    /**
     * Where we currently are.
     */
    private Integer currentLevel = null;

    /**
     * The ProgressDialog that is shown until we have downloaded everything from our mapserver.
     */
    private ProgressDialog progressDialog;

    private boolean mapActivityWasScanning;

    private WifiCapturer wifiCapturer;

    private MapTileProviderBase provider;

    private int downloadsFailed = 0;

    private String baseMapName;

    private List<AsyncTask> downloadTasksList;

    private OsmParser osmParser;


    /**
     * Construct a new MultiLevelFingerprintManager.
     *
     * @param activity the parent activity, from which we need the Context and FragmentManager
     * @param mapServerClient the {@link MapServerClient}
     * @param mapView  the {@link MapView}
     * @param mapName  the name of the multi-map, as created by GetMapListTask.makeMultilevelMaps
     * @param osmParser the OsmParser that shall be used to extract all necessary data by the DownloadMapDataTask.
     */
    public MultiLevelFingerprintManager(final AbstractMapActivity activity,
                                        final MapServerClient mapServerClient,
                                        final MapView mapView,
                                        final String mapName,
                                        final OsmParser osmParser) {

        super(activity, mapServerClient, mapView, mapName, false);
        this.osmParser = osmParser;
        downloadTasksList = new ArrayList<AsyncTask>();
        final Pattern baseNamePattern = Pattern.compile("(.*) \\[\\d,.*\\d\\]");
        final Matcher m = baseNamePattern.matcher(mapName);
        if (m.find()) {
            baseMapName = m.group(1);
            mapNameTemplate = mapNameTemplate.replace("[baseMapName]", baseMapName);
        }

        findMinMaxLevel();

        showProgressDialog();

        createMaps();
    }


    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getLevel() {
        return currentLevel;
    }


    /**
     * Deserialize a json-string into fingerprints for a specific level.
     *
     * @param json the fingerprints as json
     * @param level the level of the building for which these are the fingerprints
     */
    public void setFingerprintsJson(final String json, final int level) {
        final List<Fingerprint> fingerprints = parseJson(json);
        final IndoorMap map = maps.get(level);
        map.setFingerprints(fingerprints);
    }

    /**
     * Switch to the level of the building on which we currently are. Includes autodetection.
     */
    public void switchLevel() {
        mapActivityWasScanning = ((AbstractMapActivity) activity).stopScan();
        // Do a single scan, as if we were tracking:
        wifiCapturer = new WifiCapturer(activity,
                                        this,
                                        activity.getResources().getInteger(R.integer.tracker_scanbuffer_maxage));
        wifiCapturer.startSensors();
    }

    /**
     * Adding a histogram will compare this histogram to all the fingerprints on all levels and then switch to the
     * level that most closely matched the histogram.
     * If this method is called while there are still pending downloads it will do nothing.
     * @param histogram the current histogram
     */
    @Override
    public void addHistogram(final Histogram histogram) {
        if (progressDialog == null) {
            wifiCapturer.stopSensors();

            final RouterLevelHistogram rlh = RouterLevelHistogram.makeAverageHistogram(histogram);
            final SortedMap<Float, Integer> divergenceMap = new TreeMap<Float, Integer>();

            for (int level = minLevel; level <= maxLevel; level++) {
                calculateLevelDivergence(level, rlh, divergenceMap);
            }

            if (!divergenceMap.isEmpty()) {
                setLevel(findClosestLevel(divergenceMap));
            }

            if (mapActivityWasScanning) {
                ((AbstractMapActivity) activity).startScan();
            }
        }
    }

    /**
     * Set a specific level, exchanging the active overlay and thereby the fingerprints too.
     *
     * @param level the number of the level (with 0 being the ground floor)
     */
    public void setLevel(final int level) {
        if ((level <= maxLevel) && (level >= minLevel)) {
            currentLevel = level;
            final Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(getNewRunnable());
        }
    }

    // TODO: strange name...
    private Runnable getNewRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                final IndoorMap map = maps.get(currentLevel);
                configureMapView(map);
                overlay = map.getFingerprintOverlay();
                mapView.invalidate();
                final WayManager wayManager = ((AbstractMapActivity) activity).getWayManager();
                if (wayManager != null) {
                    wayManager.setEdges(map.getEdges());
                }
            }
        };
    }

    @Override
    public void onDownloadFinished(final DownloadTask task, final boolean success, final Object data) {
        if (!success) {
            downloadsFailed++;
        }

        progressDialog.incrementProgressBy(1);
        if (progressDialog.getMax() == progressDialog.getProgress()) {
            progressDialog.dismiss();
            progressDialog = null;
            if (downloadsFailed > 0) {
                handleFailedDownloadTask();
                setLevel(minLevel);
            }
            switchLevel();
        }
    }

    private void handleFailedDownloadTask() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity,
                        "Some downloads failed - things might not work as expected.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void detach(){
        super.detach();
        if(provider != null){
            provider.detach();
        }
        if(progressDialog != null){
            progressDialog.dismiss();
        }
        for(AsyncTask task : downloadTasksList){
            task.cancel(true);
        }
    }


    /**
     * Calculate the divergence from one histogram to all the fingerprints on one level and add them to a map.
     * @param level the level to analyse
     * @param rlh the current histogram
     * @param divergenceMap map of divergence -> level where we add the data for the level
     */
    private void calculateLevelDivergence(final int level,
                                          final RouterLevelHistogram rlh,
                                          final SortedMap<Float, Integer> divergenceMap) {
        overlay = maps.get(level).getFingerprintOverlay();
        final List<Fingerprint> fingerprints = getFingerprints();

        final RouterLevelDivergence divergenceAlgorithm = new KullbackLeibler();

        for (Fingerprint fingerprint : fingerprints) {
            final RouterLevelHistogram fingerprintRlh =
                    RouterLevelHistogram.makeAverageHistogram(fingerprint.getHistogram());
            divergenceAlgorithm.init(rlh, fingerprintRlh);
            // A confidence < 0.4 is not considered relevant enough:
            if (divergenceAlgorithm.getConfidence() > 0.4f) {
                // TODO: in case of identical divergence this will overwrite the old value!!
                divergenceMap.put(divergenceAlgorithm.getDivergence(), level);
            }
        }
    }

    /**
     * Find the level that we most probably are on. At the moment that is the first level to reach 5 fingerprints,
     * counted from the beginning of the divergenceMap.
     * @param divergenceMap a sorted mapping of divergence to level number
     * @return the current level number
     */
    private int findClosestLevel(final SortedMap<Float, Integer> divergenceMap) {
        final Map<Integer, Integer> hitList = new HashMap<Integer, Integer>();

        for (Integer level : divergenceMap.values()) {
            Integer count = hitList.get(level);
            if (count == null) {
                count = 0;
            }
            hitList.put(level, ++count);
            if (count == 5) {
                return level;
            }
        }

        // Too few fingerprints for any level to reach 3!?
        return divergenceMap.get(divergenceMap.firstKey());
    }


    /**
     * Switch a MapView to a new IndoorMap, i.e. get the configuration from the IndoorMap and apply it to the MapView.
     * @param map the new IndoorMap
     */
    private void configureMapView(final IndoorMap map) {
        mapView.setTileSource(makeTileSource());

        mapView.getOverlays().remove(overlay);
        mapView.getOverlays().add(map.getFingerprintOverlay());

        mapView.setMinZoomLevel(map.getMinZoomLevel());
        mapView.setMaxZoomLevel(map.getMaxZoomLevel());
        // TODO: does it even make sense to arrive at this place without a boundingbox? Maybe we get called
        //       too early, if that happens? Anyway, the MapView doesn't like "null"...
        final BoundingBoxE6 boundinBox = map.getBoundingBox();
        if (boundinBox != null) {
            mapView.zoomToBoundingBox(boundinBox);
            mapView.setScrollableAreaLimit(boundinBox);
        }
    }

    /**
     * Get the appropriate tile source for the current level.
     *
     * @return the ITileSource for the current level
     */
    private ITileSource makeTileSource() {
        // TODO: maybe we need to dispose of the old provider/source?
        final TileProviderFactory factory = new TileProviderFactory(activity);
        provider = factory.buildWebTileProvider(
                activity.getResources().getString(R.string.map_provider_url_schema),
                mapNameTemplate.replace("[level]", String.valueOf(currentLevel)));
        return provider.getTileSource();
    }


    /**
     * Find the min and max level, based on the mapName that we got.
     */
    private void findMinMaxLevel() {
        final Pattern levelPattern = Pattern.compile(" \\[(\\d),.*(\\d)\\]");
        final Matcher m = levelPattern.matcher(mapName);
        if (m.find()) {
            minLevel = Integer.parseInt(m.group(1));
            maxLevel = Integer.parseInt(m.group(2));
        }
    }


    /**
     * Show a progress dialog to calm the user while we download the data for all the levels.
     */
    private void showProgressDialog() {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Hole Kartendaten vom Server");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMax((maxLevel - minLevel + 1) * DOWNLOADS_PER_MAP);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.show();
            }
        });
    }


    /**
     * Fill the maps with IndoorMaps for all the levels.
     */
    private void createMaps() {
        maps = new HashMap<Integer, IndoorMap>();
        final Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ips_histo);
        final Drawable icon = new BitmapDrawable(activity.getResources(), bm);
        // First we need to fill our map with all required instances of IndoorMap:
        for (int level = minLevel; level <= maxLevel; level++) {
            maps.put(level, createOneMap(icon));
        }

        addDownloadTasksToTasksList();
    }

    private void addDownloadTasksToTasksList() {
        for (int level = minLevel; level <= maxLevel; level++) {
            final IndoorMap map = maps.get(level);
            final String levelName = mapNameTemplate.replace("[level]", String.valueOf(level));

            // TODO: The first two tasks need to be refactored to make better use of the DownloadListener so that we
            //       don't need to have this ugly double-loop.
            final CachedDownloadFingerprintsTask cachedDownloadFingerprintsTask =
                    new CachedDownloadFingerprintsTask(this, activity, mapServerClient, levelName, this, level);
            final DownloadMapResourceTask downloadMapResourceTask =
                    new CachedDownloadMapResourceTask(activity, mapServerClient, levelName, map, this);
            final DownloadMapDataTask downloadEdgesTask = createDownloadEdgesTask(map, levelName);

            executeDownloadTasks(cachedDownloadFingerprintsTask, downloadMapResourceTask, downloadEdgesTask);

            downloadTasksList.add(cachedDownloadFingerprintsTask);
            downloadTasksList.add(downloadMapResourceTask);
            downloadTasksList.add(downloadEdgesTask);
        }
    }

    private void executeDownloadTasks(
            CachedDownloadFingerprintsTask cachedDownloadFingerprintsTask,
            DownloadMapResourceTask downloadMapResourceTask,
            DownloadMapDataTask downloadEdgesTask) {
        downloadMapResourceTask.execute();
        cachedDownloadFingerprintsTask.execute();
        downloadEdgesTask.execute();
    }

    private DownloadMapDataTask createDownloadEdgesTask(IndoorMap map, String levelName) {
        final DownloadMapDataTask downloadEdgesTask = new DownloadMapDataTask(mapServerClient, levelName, osmParser);
        downloadEdgesTask.addDownloadListener(map);
        downloadEdgesTask.addDownloadListener(this);
        return downloadEdgesTask;
    }


    /**
     * Create one IndoorMap and trigger the download tasks that it needs.
     *
     * @param icon the icon for the fingerprints in the overlays.
     * @return the new IndoorMap
     */
    private IndoorMap createOneMap(final Drawable icon) {
        final ItemizedIconOverlay<FingerprintItem> levelOverlay =
                new ItemizedIconOverlay<FingerprintItem>(new ArrayList<FingerprintItem>(),
                        icon,
                        this,
                        new DefaultResourceProxyImpl(activity));
        final IndoorMap map = new IndoorMap(levelOverlay);
        return map;
    }

}
