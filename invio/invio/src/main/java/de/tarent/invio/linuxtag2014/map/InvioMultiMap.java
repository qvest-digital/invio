package de.tarent.invio.linuxtag2014.map;

import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.widget.Toast;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.tarent.invio.linuxtag2014.MapActivity;
import de.tarent.invio.linuxtag2014.R;
import de.tarent.invio.linuxtag2014.products.ProductItem;
import de.tarent.invio.linuxtag2014.products.ProductManager;
import de.tarent.invio.linuxtag2014.task.InvioOsmParser;
import de.tarent.invio.linuxtag2014.task.ZipFingerprintsTask;
import de.tarent.invio.linuxtag2014.task.ZipMapDataTask;
import de.tarent.invio.linuxtag2014.task.ZipMapResourceTask;
import de.tarent.nic.android.base.AbstractMapActivity;
import de.tarent.nic.android.base.json.NicGeoPointDeserializer;
import de.tarent.nic.android.base.mapmatching.WayManager;
import de.tarent.nic.android.base.task.DownloadListener;
import de.tarent.nic.android.base.task.DownloadTask;
import de.tarent.nic.android.base.tileprovider.TileProviderFactory;
import de.tarent.nic.android.base.wifi.FingerprintManager;
import de.tarent.nic.android.base.wifi.UserLocator;
import de.tarent.nic.android.base.wifi.WifiCapturer;
import de.tarent.nic.entities.Fingerprint;
import de.tarent.nic.entities.Histogram;
import de.tarent.nic.entities.NicGeoPoint;
import de.tarent.nic.tracker.wifi.HistogramConsumer;
import de.tarent.nic.tracker.wifi.RouterLevelHistogram;
import de.tarent.nic.tracker.wifi.divergence.KullbackLeibler;
import de.tarent.nic.tracker.wifi.divergence.RouterLevelDivergence;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;

import java.io.File;
import java.lang.reflect.Type;
import java.util.*;

/**
 * A InvioMultiMap bundles one or more instances of InvioIndoorMap together to form a multi-level building.
 */
public class InvioMultiMap implements DownloadListener, HistogramConsumer {

    private static final int DOWNLOADS_PER_MAP = 3;

    protected File groupDir;

    protected MapActivity activity;

    protected MapView mapView;

    private MapTileProviderBase provider;

    protected Map<String, InvioIndoorMap> maps;

    protected List<InvioIndoorMap> mapsList;

    private int successfulPartDownloads = 0;

    private int partDownloads = 0;

    private WifiCapturer wifiCapturer;

    //TODO Multilevel: this is the position of the level inside the mapsList (kinda wrong)
    private int level = 0;

    InvioIndoorMap userSelectedMap;

    public InvioMultiMap(final File groupDir, final MapActivity activity, final MapView mapView) {
        this.groupDir = groupDir;
        this.activity = activity;
        this.mapView = mapView;
        createMaps();
    }

    public void detach(){
        if(provider != null) {
            provider.detach();
        }
    }

    public int getLevel() {
        return level;
    }

    /**
     * Create indoor maps per directory inside the unzipped group data directory.
     */
    private void createMaps() {
        mapsList = new ArrayList<InvioIndoorMap>();
        if(groupDir.exists() && groupDir.isDirectory()) {
            File[] mapDirs = groupDir.listFiles();
            for(int i = 0; i < mapDirs.length; i++) {
                InvioIndoorMap map = new InvioIndoorMap(mapDirs[i]);
                mapsList.add(map);
            }

            for(InvioIndoorMap map : mapsList) {
                final ZipMapDataTask mapDataTask = new ZipMapDataTask(map, map.getMapDirectory(), new InvioOsmParser(map.getShortName()));
                mapDataTask.addDownloadListener(this);
                mapDataTask.addDownloadListener(ProductManager.getMainProductManager());
                mapDataTask.execute();
                final ZipMapResourceTask mapResourceTask = new ZipMapResourceTask(this, map);
                mapResourceTask.execute();
                final ZipFingerprintsTask fingerprintsTask = new ZipFingerprintsTask(map, map);
                fingerprintsTask.addDownloadListener(this);
                fingerprintsTask.execute();
            }
        }
    }

    /**
     * Switches the map in the UI and sets all data needed for tracking (fingerprints, ways, scale ..)
     *
     * @param map {@link de.tarent.invio.linuxtag2014.map.InvioIndoorMap} to show
     */
    private void switchMapForReal(final InvioIndoorMap map){
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(getNewRunnable(map));
        activity.dismissProgressDialog();
    }

    /**
     * Get Map by Name
     * @param shortName short id of map
     * @return map
     */
    public InvioIndoorMap findMapByName(final String shortName){
        return maps.get(shortName);
    }

    // TODO: strange name...
    private Runnable getNewRunnable(final InvioIndoorMap map) {
        return new Runnable() {
            @Override
            public void run() {

                final Integer angle = map.getBaseAngle();
                if(angle != null) {
                    AbstractMapActivity.getDeadReckoning(activity).setBaseAngle(map.getBaseAngle());
                }
                final Float scale = map.getScale();
                if(scale != null) {
                    final float mapScaleDivisor = (float) activity.getResources().getInteger(R.integer.mapscale_divisor);
                    UserLocator.setScale(scale / mapScaleDivisor);
                }
                final FingerprintManager fingerprintManager = activity.getFingerprintManager();
                if(fingerprintManager != null) {
                    fingerprintManager.removeAllFingerprintFromOverlay();
                    fingerprintManager.setFingerprintsJson(map.getFingerprintsJson());
                }
                final WayManager wayManager = activity.getWayManager();
                if (wayManager != null) {
                    wayManager.setEdges(map.getEdges());
                }
                final Set<ProductItem> productItems = map.getProductItems();
                activity.getProductOverlay().removeAllItems();
                if(productItems != null) {
                    for(final ProductItem productItem : productItems) {
                        activity.addProductItemToMap(productItem, false);
                    }
                }
                configureMapView(map);
                mapView.invalidate();

            }
        };
    }

    /**
     * Switch to the level of the building on which we currently are. Includes autodetection.
     */
    public void switchMap(InvioIndoorMap map) {
        if (!activity.getWifiOff()) {
            activity.showProgressDialog();
        }
        userSelectedMap = map;
        activity.stopScan();
        // Do a single scan, as if we were tracking:
        wifiCapturer = new WifiCapturer(activity,
                this,
                activity.getResources().getInteger(de.tarent.nic.android.base.R.integer.tracker_scanbuffer_maxage));
        wifiCapturer.startSensors();
    }

    /**
     * Return an {@link de.tarent.invio.linuxtag2014.map.InvioIndoorMap} by its short name
     *
     * @param shortName of the map
     * @return the {@link de.tarent.invio.linuxtag2014.map.InvioIndoorMap}
     */
    public InvioIndoorMap getMapByShortName(final String shortName) {
        return maps.get(shortName);
    }

    /**
     * Adding a histogram will compare this histogram to all the fingerprints on all levels and then switch to the
     * level that most closely matched the histogram.
     * If this method is called while there are still pending downloads it will do nothing.
     * @param histogram the current histogram
     */
    @Override
    public void addHistogram(final Histogram histogram) {
        wifiCapturer.stopSensors();

        final RouterLevelHistogram rlh = RouterLevelHistogram.makeAverageHistogram(histogram);
        final SortedMap<Float, Integer> divergenceMap = new TreeMap<Float, Integer>();

        for(int i = 0; i < mapsList.size(); i++) {
            calculateLevelDivergence(i, rlh, divergenceMap);
        }

        if (userSelectedMap == null) {
            if (divergenceMap.isEmpty()) {
                switchMapForReal(maps.get(activity.getString(R.string.multilevel_default_map_short_name)));
                // TODO hardcoded
                activity.setLevelFocus(1);
            } else {
                level = findClosestLevel(divergenceMap);
                activity.setLevelFocus(level);
                switchMapForReal(mapsList.get(level));
            }
            activity.startScan();
        } else {
            switchMapForReal(userSelectedMap);
            if ((!divergenceMap.isEmpty()) && (userSelectedMap == mapsList.get(findClosestLevel(divergenceMap)))) {
                activity.startScan();
            } else {
                activity.stopScan();
            }
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

        final List<Fingerprint> fingerprints = parseJson(mapsList.get(level).getFingerprintsJson());
        if(fingerprints == null || fingerprints.isEmpty()) {
            return;
        }
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
     * Create Fingerprints from json-string.
     *
     * @param json the json that contains the serialized fingerprints
     * @return the List of Fingerprints
     */
    //TODO Refactoring: THIS CODE IS STRICTLY DUPLICATE FROM FingerprintManager
    protected List<Fingerprint> parseJson(final String json) {
        // We need this TypeToken-thingy because it is not possible to have a Class-object for a generic type.
        // But we must tell gson what kind of object it is supposed to create. That's how we can do is:
        final Type genericFingerprintArrayListType = new TypeToken<ArrayList<Fingerprint>>() {
        }.getType();
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(NicGeoPoint.class, new NicGeoPointDeserializer());

        final List<Fingerprint> fingerprints = gsonBuilder.create().fromJson(json, genericFingerprintArrayListType);
        return fingerprints;
    }

    private void configureMapView(final InvioIndoorMap map) {
            mapView.setTileSource(makeTileSource(map));
            mapView.setMinZoomLevel(map.getMinZoomLevel());
            mapView.setMaxZoomLevel(map.getMaxZoomLevel());
            // TODO: does it even make sense to arrive at this place without a boundingbox? Maybe we get called
            //       too early, if that happens? Anyway, the MapView doesn't like "null"...
            final BoundingBoxE6 boundingBox = map.getBoundingBox();
            if (boundingBox != null) {
                mapView.setMinZoomLevel(getZoomLevel(map.getMinZoomLevel()));
                mapView.zoomToBoundingBox(boundingBox);
                mapView.setScrollableAreaLimit(boundingBox);
            }
    }

    private int getZoomLevel(int minZoomLevel) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int screenSize = Math.min(size.x, size.y);
        int zoomLvl = 0;

        while ( screenSize > 512 ) {
            screenSize = screenSize/2;
            zoomLvl++;
        }

        return minZoomLevel + zoomLvl;
    }

    /**
     * Get the appropriate tile source for the current level.
     *
     * @return the ITileSource for the current level
     */
    private ITileSource makeTileSource(final InvioIndoorMap map) {
        // TODO: maybe we need to dispose of the old provider/source?
        final TileProviderFactory factory = new TileProviderFactory(activity);
        provider = factory.buildWebTileProvider(
                activity.getResources().getString(de.tarent.nic.android.base.R.string.map_provider_url_schema),
                map.getMapDirectory().getName());
        return provider.getTileSource();
    }


    @Override
    public void onDownloadFinished(DownloadTask task, boolean success, Object data) {
        //Count all part downloads - map data, fingerprints and tilemapresource, no matter if it was successful or not.
        partDownloads++;

        //Then count only the successful downloads
        if(success) {
            if(task instanceof ZipMapDataTask) {
                successfulPartDownloads++;
            } else if(task instanceof ZipMapResourceTask) {
                successfulPartDownloads++;
            } else if(task instanceof ZipFingerprintsTask) {
                successfulPartDownloads++;
            }
        }

        //Inform the user, when some parts are missing!
        //TODO Multilevel: what to do if some fingerprints are missing?
        if(partDownloads == (mapsList.size() * DOWNLOADS_PER_MAP)) {
            if(successfulPartDownloads < partDownloads) {
                //TODO: Some parts are missing! Things may not work properly
                Toast.makeText(activity, "Some parts are missing! Things may not work as expected!", 7000);
            }

            //Configure searchview in order to be able to search for rooms or exhibitors
            activity.configureSearch();

            createHashMapFromMapsList();
            switchMap(null);
            //Create the level buttons in the map view
            activity.createLevelButtons(mapsList);
            activity.setLevelFocus(getLevel());
        }
    }

    /**
     * Now that we have downloaded and parsed short names, create a map with short name as a key
     *
     * and {@link InvioIndoorMap} objects as values.
     */
    private void createHashMapFromMapsList() {
        maps = new HashMap<String, InvioIndoorMap>();
        for(final InvioIndoorMap map : mapsList) {
            maps.put(map.getShortName(), map);
        }
    }
}
