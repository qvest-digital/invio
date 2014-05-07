package de.tarent.nic.android.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.tarent.nic.android.base.config.Config;
import de.tarent.nic.android.base.config.ValueProvider;
import de.tarent.nic.android.base.mapmatching.WayManager;
import de.tarent.nic.android.base.position.UserPositionManager;
import de.tarent.nic.android.base.sensor.DeadReckoning;
import de.tarent.nic.android.base.task.CachedDownloadMapDataTask;
import de.tarent.nic.android.base.task.CachedDownloadMapResourceTask;
import de.tarent.nic.android.base.task.NicOsmParser;
import de.tarent.nic.android.base.tileprovider.TileProviderFactory;
import de.tarent.nic.android.base.wifi.FingerprintManager;
import de.tarent.nic.android.base.wifi.UserLocator;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.mapserver.MapServerClientImpl;
import de.tarent.nic.android.base.wifi.WifiCapturer;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.views.MapView;

import static de.tarent.nic.android.base.config.Property.MAP_PROVIDER_SCHEMA;


/**
 * Jede {@link android.app.Activity} des Nic-Projektes sollte von
 * dieser Klasse erben! Sie beinhaltet nötige Initialisierungs-
 * schritte und Methoden, die allgemein von Bedeutung sind.
 *
 * @author Sven Schumann, <s.schumann@tarent.de>
 */
public abstract class AbstractMapActivity extends Activity {

    /**
     * The key under which we store our fingerprints in the instance-state-bundle.
     */
    protected static final String FINGERPRINTS_JSON = "fingerprintsJson";

    protected static final String EDGES_JSON = "edgesJson";

    /**
     * The {@link WayManager} is static so that HomeActivity can access it, even before the AbstractMapActivity is
     * instantiated, to pass it as a DownloadListener to the MapDatadownloadTask.
     */
    private static final WayManager WAY_MANAGER = new WayManager();

    /**
     * the {@linke DeadReckoning} tracks the user's movement until a new wifi-position is available. It is static so
     * that HomeActivity can access it, even before the AbstractMapActivity is instantiated, to pass it as a
     * DownloadListener to the MapDatadownloadTask.
     */
    private static DeadReckoning deadReckoning;

    /**
     * The {@link UserLocator} is used to do the fingerprint-matching/tracking of our current position.
     */
    protected UserLocator userLocator;

    /**
     * The name of the map the current map. Will be used in the URL from which to download tiles, fingerprints,
     * etc. from the server.
     */
    protected String mapName;

    /**
     * The {@link MapView}from osmdroid is the main-feature of this activity.
     */
    protected MapView mapView;

    /**
     * The {@link FingerprintManager}.
     */
    protected FingerprintManager fingerprintManager;

    /**
     * The {@link de.tarent.nic.android.base.wifi.WifiCapturer} builds the scan-buffer for the tracking-mode:
     */
    protected WifiCapturer wifiCapturer;

    /**
     * The MapServerClient is used by all the tasks that need to talk to the map-server:
     */
    protected MapServerClient mapServerClient;

    private int mapViewLayoutId;

    private int osmMapViewId;

    private ValueProvider mConfig;

    private boolean isMultiLevelMap;

    private MapTileProviderBase tileProvider;

    /**
     * This flag stores whether the UserLocator is currently scanning (using all its available sensors).
     */
    private boolean scanning = false;


    /**
     * Constructor.
     */
    protected AbstractMapActivity() {
        initialiseConfig();

        this.mConfig = Config.getInstance();
    }


    /**
     * Get the global WayManager. (Not a very nice solution to the problem that the waymanager needs to be created
     * before the activity that needs it, because we just happen to download the data earlier...)
     * @return the WayManager
     */
    public static WayManager getWayManager() {
        return WAY_MANAGER;
    }

    /**
     * Get the {@link MapView} - the main feature of this activity.
     *
     * @return the {@link MapView}
     */
    public MapView getMapView() {
        return mapView;
    }

    /**
     * Get the DeadReckoning-instance, creating it if it doesn't exist.
     * @param activity the activity where the DeadReckoning, if it has to be created, can get its resources from.
     * @return the static DeadReckoning
     */
    public static DeadReckoning getDeadReckoning(final Activity activity) {
        if (deadReckoning == null) {
            deadReckoning = new DeadReckoning(activity);
        }
        return deadReckoning;
    }

    /**
     * Get the {@link de.tarent.nic.android.base.wifi.FingerprintManager}
     *
     * @return the {@link de.tarent.nic.android.base.wifi.FingerprintManager}
     */
    public FingerprintManager getFingerprintManager() {
        return fingerprintManager; //NOSONAR Initialized in the implemented activities
    }

    /**
     * Get the {@link WifiCapturer}-instance, creating it if it doesn't exist.
     * @param activity the activity where the {@link WifiCapturer}, if it has to be created, can get its resources
     *                   from.
     * @return the {@link WifiCapturer}-instance
     */
    public WifiCapturer getWifiCapturer(final Activity activity) {
        initSensorCollectors(userLocator);
        return wifiCapturer;
    }

    /**
     * (Re-)start scanning.
     * @return true = activity was already scanning; false = activity was not scanning (but is now)
     */
    public boolean startScan() {
        final boolean wasScanning = scanning;
        scanning = true;
        userLocator.startTracking();
        return wasScanning;
    }

    /**
     * Stop scanning.
     * @return true = activity was not scanning; false = activity was  scanning (but has now stopped)
     */
    public boolean stopScan() {
        final boolean wasScanning = scanning;
        scanning = false;
        userLocator.stopTracking();
        return wasScanning;
    }

    /**
     * Detach all tileProviders on stop to avoid leaks
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (tileProvider != null) {
            tileProvider.detach();
        }
        if (fingerprintManager != null) { // NOSONAR - Unwritten public or protected field - Not always null
            fingerprintManager.detach();
        }
    }

    /**
     * This onCreate method is called when the need to distinguish between layouts is present. For example the
     * admin-app requires a toggle button in the {@link MapView} whereas the kunden-app does not.
     *
     * @param savedInstanceState the {@link Bundle}
     * @param mapViewLayoutId the layout id for the map view
     * @param osmMapViewId the is for the osm map view
     */
    protected void onCreate(final Bundle savedInstanceState, final int mapViewLayoutId, final int osmMapViewId) {
        this.mapViewLayoutId = mapViewLayoutId;
        this.osmMapViewId = osmMapViewId;
        final String serverEndpoint = getResources().getString(R.string.server_endpoint);
        mapServerClient = new MapServerClientImpl(serverEndpoint);
        onBaseCreate(savedInstanceState);
    }

    /**
     * Configure map. Download or restore fingerprints and ways. IMPORTANT: fingerprints will be restored if the map is
     * not an multilevel map.
     * @param savedInstanceState the state that was saved in onSaveInstanceState
     */
    private void onBaseCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapName = findMapName();

        setContentView(getMapLayout());
        configureMapView();

        if (isMultiLevelMap) {
            createMultiLevelFingerprintManager(savedInstanceState);
        } else {
            createOrRecoverFingerprintManager(savedInstanceState);
        }

        final UserPositionManager userPositionManager = new UserPositionManager(this, mapView);
        userLocator = new UserLocator(this, fingerprintManager, userPositionManager, WAY_MANAGER);
        initSensorCollectors(userLocator);
    }

    private void initSensorCollectors(UserLocator myUserLocator) {
        if (wifiCapturer == null) {
            wifiCapturer = new WifiCapturer(this,
                    myUserLocator,
                    getResources().getInteger(R.integer.tracker_scanbuffer_maxage));

            myUserLocator.addSensorCollector(wifiCapturer);
        }

        myUserLocator.addSensorCollector(getDeadReckoning(this));
    }

    private void createOrRecoverFingerprintManager(Bundle savedInstanceState) {
        //TODO: Need to save map ressources in the Bundle
        new CachedDownloadMapResourceTask(this, mapServerClient, mapView, mapName).execute();
        if (savedInstanceState == null){
            // For single-level-maps we can download the resources here, but for multi-level-maps this will
            // be handled by the MultiLevelFingerprintManager.
            createFingerprintManager();
            // Crude hack: maybe the WayManager already has edges, downloaded from some other activity (e.g.
            // HomeActivity in sellfio-customer-app). But maybe it doesn't (e.g. admin-app). In that case do download
            // them now:
            if (WAY_MANAGER.getEdges().size() == 0) {
                new CachedDownloadMapDataTask(WAY_MANAGER, mapServerClient, mapName, new NicOsmParser()).execute();
            }
        } else {
            //If savedInstanceState is not null then we can restore fingerprints and ways from Bundle and don't
            //need to download it again.
            final String edgesJson = savedInstanceState.getString(EDGES_JSON);
            WAY_MANAGER.setEdgesJson(edgesJson);
            restoreFingerprintManagerFromBundle(savedInstanceState);
        }
    }


    /**
     * This method is called before an activity may be killed to restore the state in the future. This is the case when
     * user presses the home button
     * IMPORTANT: please note that this method will NOT be called it user navigates back from activity or another
     * activity is launched in front of this activity, because there is no need to it.
     * @param   outState the Bundle, in which to save our state
     */
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        if (fingerprintManager != null) { // NOSONAR this field is written by the child-classes only and that's ok
            outState.putString(FINGERPRINTS_JSON, fingerprintManager.getFingerprintsJson());
        }
        if (WAY_MANAGER != null) { // NOSONAR this field is written by the child-classes only and that's ok
            outState.putString(EDGES_JSON, WAY_MANAGER.getEdgesJson());
        }

        super.onSaveInstanceState(outState);
    }

    /**
     * Create a {@link FingerprintManager} using the savedInstanceState.
     */
    protected abstract void createFingerprintManager();

    /**
     * Create new fingerprintManager instance and put the saved fingerprints from bundle.
     * @param savedInstanceState the Bundle from which to read the data that the FingerprintManager needs.
     */
    protected abstract void restoreFingerprintManagerFromBundle(final Bundle savedInstanceState);

    /**
     * Create a {@link de.tarent.nic.android.base.wifi.MultiLevelFingerprintManager} using the savedInstanceState.
     *
     * @param savedInstanceState the {@link Bundle} with the savedInstanceState
     */
    protected abstract void createMultiLevelFingerprintManager(final Bundle savedInstanceState);

    /**
     * Liefert den zu verwendenden {@link ValueProvider}.
     *
     * @return den zu verwendenden {@link ValueProvider}
     */
    protected ValueProvider config() {
        return mConfig;
    }


    /**
     * Find the name of the map in the intent that started this activity. The relevant intent-extra will have been set
     * by the MapSelectionActivity, which comes before this activity to let the user chose a map:
     * Here we also detect whether this is a multi-level-map.
     * @return the name of the map
     */
    private String findMapName() {
        final Intent intent = getIntent();
        final String mapName = intent.getStringExtra("MapName");
        isMultiLevelMap = intent.getBooleanExtra("multilevelMap", false);
        return mapName;
    }


    private ViewGroup getMapLayout() {
        final LayoutInflater inflater = getLayoutInflater();
        final View v = inflater.inflate(mapViewLayoutId, null);
        return (ViewGroup) v;
    }

    private MapView configureMapView() {
        mapView = (MapView)findViewById(osmMapViewId);

        // For multi level maps the tileprovider will later be set by the MultiLevelFingerprintManager.
        if (!isMultiLevelMap) {
            tileProvider = buildTileProvider();
            mapView.setTileSource(tileProvider.getTileSource());
        }

        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);    //zoom with finger

        return mapView;
    }

    private MapTileProviderBase buildTileProvider() {
        final TileProviderFactory factory = new TileProviderFactory(this);
        final MapTileProviderBase provider = factory.buildWebTileProvider(
                config().getPropertyValue(MAP_PROVIDER_SCHEMA),
                mapName);

        return provider;
    }

    private void initialiseConfig() {
        Config.getInstance().setContext(this);
    }

}
