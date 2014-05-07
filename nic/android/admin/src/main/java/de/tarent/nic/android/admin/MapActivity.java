package de.tarent.nic.android.admin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import de.tarent.nic.android.admin.wifi.AdminFingerprintManager;
import de.tarent.nic.android.admin.wifi.FingerprintCaptureOverlay;
import de.tarent.nic.android.base.AbstractMapActivity;
import de.tarent.nic.android.base.task.CachedDownloadMapDataTask;
import de.tarent.nic.android.base.task.DownloadListener;
import de.tarent.nic.android.base.task.DownloadTask;
import de.tarent.nic.android.base.task.NicOsmParser;
import de.tarent.nic.android.base.task.OsmParserKeys;
import de.tarent.nic.android.base.wifi.UserLocator;
import de.tarent.nic.entities.Edge;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.mapserver.MapServerClientImpl;
import org.osmdroid.views.overlay.Overlay;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * The MapActivity is, currently, the OSM-MapView, where the admin can create and delete fingerprints.
 */
public class MapActivity extends AbstractMapActivity implements DownloadListener<Map<String, Collection>> {

    private static final String TAG = MapActivity.class.getName();

    /**
     * This is a demonstration-overlay for experimental MapMatching that will be relevant to a later story.
     */
    protected WayOverlay wayOverlay;

    /**
     * Scale will be shown in the Map Data dialog
     */
    protected float scale;

    /**
     * Base angle will be shown in the Map Data dialog
     */
    protected int baseAngle;

    /**
     * This is an invisible Overlay that will be added to the MapView in edit-mode only. It intercepts the events that
     * would otherwise scroll the map and will provide the coordinates on which the user clicked.
     */
    private FingerprintCaptureOverlay fingerprintCaptureOverlay;

    /**
     * This flag tells us whether we are in tracking-mode or in fingerprint-edit-mode.
     */
    private boolean tracking = false;


    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.mapview, R.id.osmmapview);

        final String serverEndpoint = getResources().getString(de.tarent.nic.android.base.R.string.server_endpoint);
        final MapServerClient mapServerClient = new MapServerClientImpl(serverEndpoint);

        final CachedDownloadMapDataTask task = new CachedDownloadMapDataTask(this,
                mapServerClient,
                mapName,
                new NicOsmParser());
        task.addDownloadListener(getDeadReckoning(this));
        task.execute();

        // AbstractMapActivity has already started the wifi sensor, but we stop it here
        // because of our toggleTracking() functionality
        super.stopScan();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onDownloadFinished(final DownloadTask task, final boolean success, final Map<String, Collection> data) {
        if(success){
            final List<Float> indoorScaleList = (List<Float>) data.get(OsmParserKeys.INDOOR_SCALE);
            if ((indoorScaleList != null) && (!indoorScaleList.isEmpty()) && (indoorScaleList.get(0) != null)) {
                final float mapScaleDivisor = (float) getResources().getInteger(R.integer.mapscale_divisor);
                UserLocator.setScale(indoorScaleList.get(0) / mapScaleDivisor);
                scale = indoorScaleList.get(0);
            }
            final List<Integer> baseAngleList = (List<Integer>) data.get(OsmParserKeys.NORTH_ANGLE);
            if ((baseAngleList != null) && (!baseAngleList.isEmpty()) && (baseAngleList.get(0) != null)) {
                baseAngle = baseAngleList.get(0);
            }
        } else {
            Log.e(TAG, "Scale download signals failure. Dead reckoning will not work.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override // NOSONAR - Method cannot be shortened!!!
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case R.id.track_me:
                toggleTracking();
                return true;
            case R.id.upload_fingerprints:
                uploadFingerprints();
                return true;
            case R.id.menu_showMapData:
                showMapData();
                return true;
            case R.id.dev_submenu:
                final Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.show_version:
                showVersionInformation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Toggle edit-mode. In this mode the user can create a new fingerprint with a single tap. Existing fingerprints
     * cannot be selected in this mode and the map cannot be scrolled (but it can be zoomed. Is that ok? TODO)
     * Edit-mode cannot be selected while we are tracking our position.
     *
     * @param view the button which is supposed to have this method as its action
     */
    public void toggleFingerprintMode(final View view) {
        if (view.getId() == R.id.create_fingerprint_button) {
            if (fingerprintCaptureOverlay.isEditModeEnabled() || tracking) {
                fingerprintCaptureOverlay.disableEditMode();
            } else {
                fingerprintCaptureOverlay.enableEditMode();
            }
            showToast("edit-mode: " + fingerprintCaptureOverlay.isEditModeEnabled());
        }
    }


    /**
     * Stop tracking on pause. For now we just stop tracking in the admin app and don't start it on resume.
     * TODO: we need to start tracking on resume if tracking was enabled
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(tracking){
            toggleTracking();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySettings();
    }

    /**
     * Here we read the settings from the {@link android.content.SharedPreferences} and then apply them.
     */
    private void applySettings() {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // Choose localization method
        setLocalizationMethod(sharedPref);

        // Show or disable way overlay
        final String keyWayOverlay = getString(R.string.key_pref_mapMatching_showWayOverlay);
        final boolean showWayOverlay = sharedPref.getBoolean(keyWayOverlay, false);
        setWayOverlayEnabled(showWayOverlay);

        // Enable or disable map matching
        final String keyMapMatching = getString(R.string.key_pref_mapMatching_toggle);
        final boolean enableMatMatching = sharedPref.getBoolean(keyMapMatching, false);
        setMapMatchingEnabled(enableMatMatching);

        // Enable or disable outlier detection. If enabled, then simple way snap algorithm will be used.
        final String keyOutlier = getString(R.string.key_pref_outlierEnabled);
        final boolean enableOutlierElimination = sharedPref.getBoolean(keyOutlier, false);
        setOutlierEliminationEnabled(enableOutlierElimination, sharedPref);

        // Enable or disable statistic filter.
        final String keyFilter = getString(R.string.key_pref_filteringEnabled);
        final boolean filteringEnabled = sharedPref.getBoolean(keyFilter, false);
        setFilteringEnabled(filteringEnabled, sharedPref);

        // Enable or disable low pass filter
        final String keyLowPassFilter = getString(R.string.key_pref_deadReckoning_lowPassToggle);
        final boolean lowPassEnabled = sharedPref.getBoolean(keyLowPassFilter, false);
        userLocator.setCompassFilterEnabled(lowPassEnabled);

        // Enable or disable showing the particles of the particle filter
        final String keyShowParticles = getString(R.string.key_pref_showparticles);
        final boolean showParticles = sharedPref.getBoolean(keyShowParticles, false);
        userLocator.getParticleFilter().setShowParticles(showParticles);

        mapView.invalidate();
    }

    /**
     * Choose localization method out of particle filter (default), wifi or dead reckoning
     * @param sharedPref to get localization method
     */
    @SuppressLint("StringFormatMatches") //Yes lint, it's ok to get string from the shared pref. list
    private void setLocalizationMethod(final SharedPreferences sharedPref) {
        final String key = getString(R.string.key_pref_localizationMethodList);
        final String defaultKey = getString(R.string.localizationMethodParticlefilter);
        // Get the method name from the Shared Preferences
        final String localizationMethod = sharedPref.getString(key, defaultKey);

        if (localizationMethod.equals(defaultKey)) {

            userLocator.setLocalizationMethod(UserLocator.LOCALIZATION_MODE_PARTICLEFILTER);
            userLocator.resetParticleFilter();
            userLocator.addSensorCollector(getWifiCapturer(this));
            userLocator.addSensorCollector(getDeadReckoning(this));

        } else if (localizationMethod.equals(getString(R.string.localizationMethodWifi))) {
            userLocator.resetParticleFilter();
            userLocator.setLocalizationMethod(UserLocator.LOCALIZATION_MODE_WIFI);
            userLocator.removeSensorCollector(getDeadReckoning(this));
            userLocator.addSensorCollector(getWifiCapturer(this));

        } else {
            userLocator.resetParticleFilter();
            userLocator.setLocalizationMethod(UserLocator.LOCALIZATION_MODE_DEADRECKONING);
            userLocator.removeSensorCollector(getWifiCapturer(this));
            userLocator.addSensorCollector(getDeadReckoning(this));
        }
    }

    /**
     * Enable or disable statistic filtering of the user position.
     *
     * @param enabled for enabling or disabling filter
     * @param sharedPref to get the filter algorithm
     */
    @SuppressLint("StringFormatMatches") //Yes lint, it's ok to get string from the shared pref. list
    private void setFilteringEnabled(boolean enabled, final SharedPreferences sharedPref) {
        if (enabled) {
            final String key = getString(R.string.key_pref_filterAlgorithmList);
            final String defaultKey = getString(R.string.filterAlgorithmMedian);
            // Get the filter algorithm name from the SharedPreferences
            final String filterAlgorithm = sharedPref.getString(key, defaultKey);
            // Because we have only 2 algorithms at the moment, we use either median or average
            if (filterAlgorithm.equals(defaultKey)) {
                userLocator.setStatisticFilterMode(UserLocator.STATISTIC_FILTER_MODE_MEDIAN);
            } else {
                userLocator.setStatisticFilterMode(UserLocator.STATISTIC_FILTER_MODE_AVERAGE);
            }
        } else {
            // if filtering is disabled then set the filter mode to none
            userLocator.setStatisticFilterMode(UserLocator.STATISTIC_FILTER_MODE_NO_FILTER);
        }
    }

    /**
     * Enable or disable map matching. Because we have only one algorithm at the moment, we use simple way snap.
     *
     * @param enabled for enabling or disabling map matching
     */
    private void setMapMatchingEnabled(final boolean enabled) {
        if (enabled) {
            userLocator.setMapMatchingMode(UserLocator.MAP_MATCHING_MODE_SIMPLE_WAY_SNAP);
        } else {
            userLocator.setMapMatchingMode(UserLocator.MAP_MATCHING_MODE_NONE);
        }
    }

    /**
     * Enable or disable outlier detection and elimination.
     *
     * @param enabled for enabling or disabling filter
     * @param sharedPref to get the outlier elimination algorithm
     */
    @SuppressLint("StringFormatMatches") //Yes lint, it's ok to get string from the shared pref. list
    private void setOutlierEliminationEnabled(final boolean enabled, final SharedPreferences sharedPref) {
        if(enabled) {
            final String key = getString(R.string.key_pref_outlierAlgorithmList);
            final String defaultKey = getString(R.string.outlierAlgorithmCME2);
            // Get the outlier elimination algorithm name from the SharedPreferences
            final String outlierAlgorithm = sharedPref.getString(key, defaultKey);
            // Because we have only 2 algorithms at the moment, we use either cme(2) or plasmona
            if (outlierAlgorithm.equals(defaultKey)) {
                userLocator.setOutlierMode(UserLocator.OUTLIER_MODE_CME);
            } else {
                userLocator.setOutlierMode(UserLocator.OUTLIER_MODE_PLASMONA);
            }
        } else {
            // if outlier detection is disabled then set the outlier mode to none
            userLocator.setOutlierMode(UserLocator.OUTLIER_MODE_NO_DETECTION);
        }
    }

    /**
     * Show the map data information such as scale and base angle in a dialog.
     *
     */
    private void showMapData() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Map Data");
        builder.setMessage("Scale: " + scale
                + "\n" + "Base angle: " + baseAngle);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFingerprintManager() {
        fingerprintManager = new AdminFingerprintManager(this, mapServerClient, mapView, mapName);
        fingerprintCaptureOverlay = new FingerprintCaptureOverlay(this,
                                                                  mapView,
                                                                  (AdminFingerprintManager) fingerprintManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void restoreFingerprintManagerFromBundle(final Bundle savedInstanceState){
        fingerprintManager = new AdminFingerprintManager(this, mapServerClient, mapView, mapName, false);
        final String fingerprintsJson = savedInstanceState.getString(FINGERPRINTS_JSON);
        fingerprintManager.setFingerprintsJson(fingerprintsJson);
        fingerprintCaptureOverlay = new FingerprintCaptureOverlay(this,
                mapView,
                (AdminFingerprintManager) fingerprintManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createMultiLevelFingerprintManager(final Bundle savedInstanceState) {
        throw new IllegalStateException("AdminApp doesn't support multilevel maps!");
    }

    private void toggleTracking() {
        tracking = !tracking;
        if (tracking) {
            fingerprintCaptureOverlay.disableEditMode();
            startScan();
            showToast("tracking: " + tracking);
        } else {
            stopScan();
            userLocator.disablePosition();
            showToast("tracking: " + tracking);
        }
    }

    /**
     * Show or hide way overlay.
     * TODO: This method is buggy and still does not work properly, because the edges are still the same as in the first
     * loaded the same across all maps show later.
     *
     * @param enabled for enabling or disabling the way overlay
     */
    private void setWayOverlayEnabled(final boolean enabled) {
        // If this overlay is added then the snapping-demonstration is activated:
        final Collection<Edge> edges = getWayManager().getEdges();
        showToast("Es wurden " + edges.size() + " Edges hinzugefügt.");
        if (wayOverlay == null) {
            wayOverlay = new WayOverlay(this, getWayManager());
        }
        final List<Overlay> overlays = mapView.getOverlays();
        if (overlays.contains(wayOverlay)) {
            if (!enabled) {
                overlays.remove(wayOverlay);
            }
        } else {
            if (enabled) {
                overlays.add(wayOverlay);
            }
        }

        mapView.invalidate();
    }

    private void uploadFingerprints() {
        ((AdminFingerprintManager) fingerprintManager).uploadFingerprints();
    }


    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MapActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showVersionInformation() {
        final VersionDialog d = new VersionDialog(this);
        final Dialog dialog = d.buildVersionInformationDialog();
        dialog.show();
    }
}
