package de.tarent.nic.android.base.wifi;

import android.content.Context;
import android.util.Log;
import de.tarent.nic.android.base.R;
import de.tarent.nic.android.base.mapmatching.WayManager;
import de.tarent.nic.android.base.particlefilter.ParticleFilter;
import de.tarent.nic.android.base.position.History;
import de.tarent.nic.android.base.position.NicGeoPointFactory;
import de.tarent.nic.android.base.position.NicGeoPointImpl;
import de.tarent.nic.android.base.position.UserPositionItem;
import de.tarent.nic.android.base.position.UserPositionManager;
import de.tarent.nic.android.base.sensor.DeadReckoning;
import de.tarent.nic.android.base.sensor.SensorCollector;
import de.tarent.nic.entities.Edge;
import de.tarent.nic.entities.Fingerprint;
import de.tarent.nic.entities.Histogram;
import de.tarent.nic.entities.NicGeoPoint;
import de.tarent.nic.tracker.interpolation.PositionInterpolator;
import de.tarent.nic.tracker.interpolation.ReciprocalInterpolation;
import de.tarent.nic.tracker.mapmatching.SimpleWaySnap;
import de.tarent.nic.tracker.outlier.CentroidMedianEliminator;
import de.tarent.nic.tracker.outlier.OutlierEliminator;
import de.tarent.nic.tracker.outlier.PlasmonaOutlierEliminator;
import de.tarent.nic.tracker.wifi.HistogramConsumer;
import de.tarent.nic.tracker.wifi.RouterLevelHistogram;
import de.tarent.nic.tracker.wifi.divergence.KullbackLeibler;
import de.tarent.nic.tracker.wifi.divergence.RouterLevelDivergence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;


/**
 * The UserLocator calculates the position of our user based on the histograms that it consumes, the fingerprints
 * it gets from the FingerprintManager, and the movement-delta it gets from the DeadReckoning.
 */
public class UserLocator implements HistogramConsumer {

    public static final String TAG = UserLocator.class.getCanonicalName();

    public static final int OUTLIER_MODE_NO_DETECTION = 1;
    public static final int OUTLIER_MODE_CME = 2;
    public static final int OUTLIER_MODE_PLASMONA = 3;

    public static final int STATISTIC_FILTER_MODE_NO_FILTER = 1;
    public static final int STATISTIC_FILTER_MODE_MEDIAN = 2;
    public static final int STATISTIC_FILTER_MODE_AVERAGE = 3;

    public static final int MAP_MATCHING_MODE_NONE = 1;
    public static final int MAP_MATCHING_MODE_SIMPLE_WAY_SNAP = 2;

    public static final int LOCALIZATION_MODE_PARTICLEFILTER = 1;
    public static final int LOCALIZATION_MODE_WIFI = 2;
    public static final int LOCALIZATION_MODE_DEADRECKONING = 3;

    /**
     * This is the scale of the map. Usually maps are not 1:1 in size, so the coordinates are not the real ones.
     * Our movement-prediction needs to know the real distances. We store it here as "scale:1". It is static so that
     * we can set it from outside, potentially before the UserLocator has been constructed. We get this info from
     * a DownloadTask, therefore we cannot know precisely when it will be available.
     */
    private static float scale;

    /**
     * The {@link History} in which a set number of historical {@link NicGeoPoint}s will be stored.
     */
    protected final History history;

    NicGeoPoint currentPoint;
    // This is the next result from the wifi-tracking only.
    NicGeoPoint nextWifiPoint;


    Set<SensorCollector> sensorCollectors = new HashSet<SensorCollector>();

    /**
     * The userPositionManager is responsible for displaying the position that we find here.
     * Package-private for better unittesting.
     */
    UserPositionManager userPositionManager;

    private FingerprintManager fingerprintManager;

    private WayManager wayManager;

    private Context ctx;

    private int outlierMode;

    private int statisticFilterMode;

    private int mapMatchingMode;

    private int localizationMode;

    // This was the last wifi-tracking result, stored here for display purposes.
    private NicGeoPoint lastWifiPoint;

    private final Object wifiPointLock = new Object();

    private SortedSet<NicGeoPoint> lastNeighbours;

    private boolean isTracking;

    private boolean pfInit;

    private Timer localisationTimer;

    private DeadReckoning deadReckoning;

    private WifiCapturer wifiCapturer;

    private ParticleFilter particleFilter;


    /**
     * Construct a new UserLocator.
     *
     * @param ctx the Context (from which we can get the resources).
     * @param fingerprintManager where we get our saved fingerprints
     * @param userPositionManager where we shall push our position
     * @param wayManager where we get our saved ways
     */
    public UserLocator(final Context ctx,
                       final FingerprintManager fingerprintManager,
                       final UserPositionManager userPositionManager,
                       final WayManager wayManager) {
        this(ctx,
             fingerprintManager,
             userPositionManager,
             wayManager,
             new History(ctx.getResources().getInteger(R.integer.tracker_history_size)));
    }
    /**
     * Construct a new UserLocator.
     *
     * @param ctx the Context (from which we can get the resources).
     * @param fingerprintManager where we get our saved fingerprints
     * @param userPositionManager where we shall push our position
     * @param wayManager where we get our saved ways
     * @param history where the list of recent {@link NicGeoPoint}s are stored
     */
    public UserLocator(final Context ctx,
                       final FingerprintManager fingerprintManager,
                       final UserPositionManager userPositionManager,
                       final WayManager wayManager,
                       final History history) {
        this.ctx = ctx;
        this.fingerprintManager = fingerprintManager;
        this.userPositionManager = userPositionManager;
        this.wayManager = wayManager;
        this.history = history;

        particleFilter = new ParticleFilter(ctx, userPositionManager);
        this.localizationMode = ctx.getResources().getInteger(R.integer.localizationMode);
    }


    /**
     * Set the scale of the map, where the user is to be localised. Important for movement-prediction, because maps
     * are usually not 1:1.
     * @param scale the scale as in "scale:1".
     */
    public static void setScale(float scale) {
        UserLocator.scale = scale;
    }

    public ParticleFilter getParticleFilter() {
        return particleFilter;
    }

    /**
     * Creates new particle filter to reset localization with newest wifi position
     */
    public void resetParticleFilter() {
        pfInit = false;
        particleFilter.clearParticles();
    }

    /**
     * Add a SensorCollector which will then be started and stopped together with this UserLocator.
     * The SensorCollectors are stored as a Set, so adding the same one twice has no effect.
     * @param collector the new SensorCollector
     */
    public void addSensorCollector(SensorCollector collector) {
        sensorCollectors.add(collector);

        if (collector instanceof DeadReckoning) {
            deadReckoning = (DeadReckoning)collector;
        }  else if (collector instanceof WifiCapturer) {
            wifiCapturer = (WifiCapturer) collector;
        }
    }

    /**
     * Remove a SensorCollector from the Set of collectors. In addition, IF the collector is DeadReckoning or
     * WifiCapturer it is stopped (although it is unclear, whether that is a good idea or not).
     *
     * @param collector the SensorCollector that you don't want to use anymore
     */
    public void removeSensorCollector(SensorCollector collector) {
        sensorCollectors.remove(collector);

        if (collector == deadReckoning) { // NOSONAR we don't use equals because we really mean that specific instance.
            deadReckoning.stopSensors();
            deadReckoning = null;
            Log.d(TAG, "DeadReckoning is now disabled");
        } else if (collector == wifiCapturer) { // NOSONAR we don't use equals because we really mean that specific
                                                  // instance.
            wifiCapturer.stopSensors();
            wifiCapturer = null;
            Log.d(TAG, "Wifi capturer is now disabled");
        }

    }

    /**
     * Start the tracking process. This will start all the registered SensorCollectors and fire up a timer that will
     * make the UserLocator process the sensor results and update UserPositionManager periodically.
     * It's safe (but useless) to call this method multiple times.
     */
    public void startTracking() {
        if (!isTracking) {
            for (final SensorCollector collector : sensorCollectors) {
                collector.startSensors();
            }
            startTimer();

            isTracking = true;
        }
    }

    /**
     * Stop the tracking process. No more updates for the UserPositionManager.
     * Will also stop all registered SensorCollectors.
     * It's safe (but useless) to call this method multiple times.
     */
    public void stopTracking() {
        if (isTracking) {
            localisationTimer.cancel();

            for (final SensorCollector collector : sensorCollectors) {
                collector.stopSensors();
            }
            Log.e(TAG, "STOP TRACKING USER LOCATOR!");
            isTracking = false;

            userPositionManager.disablePosition();
        }
    }


    /**
     * This method is called by our timer to try and update the user's position with any new sensor data that is
     * available.
     * This loop cannot be executed before startTracking was called, of course.
     */
    public void localisationLoop() {
        if (isTracking) {
            // If we don't have any position, from a hypothetical last run, we need to start somewhere:
            if (currentPoint == null) {
                initPosition();
            }
            synchronized (wifiPointLock) {
                if (localizationMode == LOCALIZATION_MODE_PARTICLEFILTER) {
                    trackWithParticleFilter();
                }
                else {
                    trackWithoutParticleFilter();
                }

                if (currentPoint != null) {
                    updatePosition(currentPoint);
                }
            }
        } else {
            Log.w(TAG, "The localisationLoop has been called while tracking is disabled. "+
                       "That makes no sense and is probably a bug.");
        }
    }

    private void trackWithParticleFilter() { //NOSONAR
        if (nextWifiPoint != null) {
            if (!pfInit) {
                if (scale > 0.f) {
                    particleFilter.setScale(scale);
                    particleFilter.initialize(nextWifiPoint.getX(), nextWifiPoint.getY());
                    currentPoint = particleFilter.calculatePosition();
                    lastWifiPoint = new NicGeoPointImpl(currentPoint.getLatitudeE6(), currentPoint.getLongitudeE6());
                    pfInit = true;
                }
            } else {
                // Ok, we have a new result from the wifi-tracking, let's just use it:
                particleFilter.updateSensor(nextWifiPoint.getX(), nextWifiPoint.getY());
                particleFilter.resampling();
                currentPoint = particleFilter.calculatePosition();
                lastWifiPoint = new NicGeoPointImpl(currentPoint.getLatitudeE6(), currentPoint.getLongitudeE6());
            }
            nextWifiPoint = null;
        } else {
            if ((deadReckoning != null) && (currentPoint != null)) {
                final NicGeoPoint normDelta = deadReckoning.getDelta();
                if (normDelta.getX() != 0.f && normDelta.getY() != 0.f) {
                    particleFilter.updateAction(normDelta.getX()*scale/1000.f, normDelta.getY()*scale/1000.f);
                }
                currentPoint = particleFilter.calculatePosition();
            }
        }
    }

    private void trackWithoutParticleFilter() {
        if (nextWifiPoint != null) {
            // Ok, we have a new result from the wifi-tracking, let's just use it:
            currentPoint = nextWifiPoint;
            lastWifiPoint = new NicGeoPointImpl(currentPoint.getLatitudeE6(), currentPoint.getLongitudeE6());
            nextWifiPoint = null;
        } else {
            // No new wifi-position, we will try to approximate a new position by using our movement data:
            if ((deadReckoning != null) && (currentPoint != null)) {
                final NicGeoPoint normDelta = deadReckoning.getDelta();
                currentPoint = addDelta(currentPoint, normDelta);
            }
        }
    }

    @Override
    public void addHistogram(final Histogram histogram) {
        // TODO: wenn wir permanente (etagen-)autodetection haben wollten dann waere das ungefaehr hier, aber eigentlich
        //       ist das bloed, um die initialen daten zu setzen, daher passiert aktuell noch nix.
        final SortedSet<NicGeoPoint> divergenceSet = calculateDivergences(histogram);

        final SortedSet<NicGeoPoint> nextNeighbours = getNearestNeighbours(divergenceSet);
        userPositionManager.setNeighboursWithOutliers(new TreeSet<NicGeoPoint>(nextNeighbours));

        removeOutliers(nextNeighbours);

        // TODO: make the interpolator configurable:
        final PositionInterpolator interpolator = new ReciprocalInterpolation(new NicGeoPointFactory());
        final NicGeoPoint point = interpolator.interpolatePosition(nextNeighbours);
        synchronized (wifiPointLock) {
            nextWifiPoint = point;
        }
        // We need a copy of the neighbours so that they can be used for drawing, while outliers are removed, etc. in
        // the next iteration:
        lastNeighbours = new TreeSet<NicGeoPoint>(nextNeighbours);
    }



    /**
     * Stop showing the position of the user (until a new position becomes available).
     */
    public void disablePosition() {
        userPositionManager.disablePosition();
    }

    public void setOutlierMode(final int outlierMode) {
        this.outlierMode = outlierMode;
    }

    public void setStatisticFilterMode(final int statisticFilterMode) {
        this.statisticFilterMode = statisticFilterMode;
    }

    public void setMapMatchingMode(final int mapMatchingMode) {
        this.mapMatchingMode = mapMatchingMode;
    }

    /**
     * Enable or disable low-pass filter in {@link DeadReckoning}. Remember that we do not have getter for
     * DeadReckoning because we control its functionality here so we have to do it that way.
     *
     * @param enabled for enabling the compass filter
     */
    public void setCompassFilterEnabled(final boolean enabled) {
        if(deadReckoning != null) {
            deadReckoning.setCompassFilterEnabled(enabled);
        }
    }

    public void setLocalizationMethod(final int localizationMode) {
        this.localizationMode = localizationMode;
    }

    /**
     * Gets a set number of nearestNeighbours from the complete divergenceSet given. Because it is a {@link SortedSet}
     * only those closest to the point are taken.
     *
     * -> yes, that's correct. It's more of a "cut off all the neighbours farther away than the first N".
     * @param divergenceSet the complete set of neighbours
     * @return the nearest neighbours
     */
    protected SortedSet<NicGeoPoint> getNearestNeighbours(final SortedSet<NicGeoPoint> divergenceSet) {

        final SortedSet<NicGeoPoint> nearestNeighbours = new TreeSet<NicGeoPoint>();

        // TODO: find a direct way to get the nearest X entries, instead of looping:
        int neighbourCount = ctx.getResources().getInteger(R.integer.tracker_num_neighbours);
        for (final NicGeoPoint point : divergenceSet) {
            nearestNeighbours.add(point);
            if (--neighbourCount == 0) {
                break;
            }
        }
        return nearestNeighbours;
    }

    private void startTimer() {
        localisationTimer = new Timer("localisationTimer");
        localisationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                localisationLoop();
            }
        }, 100, ctx.getResources().getInteger(R.integer.localisationUpdateRate));
    }

    // Try to set the currentPoint to some sensible default-position.
    private void initPosition() {
        // In the beginning, before we have real sensor-data to determine a position, we just take the center of the
        // map as an initial position.
        final NicGeoPoint center = userPositionManager.getBoundingBoxCenter();
        // It might happen that the localisation loop is started before the map-resources have been downloaded.
        // In that case the center of the real boundingbox is unknown and we will be left with the default
        // (0,0) which is located in the sea and cannot be the center of any reasonable map.
        if ((center != null) && ((center.getLatitudeE6() != 0) || (center.getLongitudeE6() != 0))) {
            currentPoint = center;
        }
    }

    private SortedSet<NicGeoPoint> calculateDivergences(final Histogram histogram) {
        final SortedSet<NicGeoPoint> divergenceSet = new TreeSet<NicGeoPoint>();

        final List<Fingerprint> fingerprints = fingerprintManager.getFingerprints();
        // TODO: make divergence-algorithm configurable (when we have more than this one...):
        final RouterLevelDivergence divergenceAlgorithm = new KullbackLeibler();
        // TODO: make histogram-type configurable:
        final RouterLevelHistogram rlh = RouterLevelHistogram.makeMedianHistogram(histogram);

        for (final Fingerprint fingerprint : fingerprints) {
            // TODO: make histogram-type configurable:
            final RouterLevelHistogram fingerprintRlh =
                    RouterLevelHistogram.makeMedianHistogram(fingerprint.getHistogram());
            divergenceAlgorithm.init(rlh, fingerprintRlh);
            final NicGeoPoint point = fingerprint.getPoint();
            point.setDivergence(divergenceAlgorithm.getDivergence());
            divergenceSet.add(point);
        }
        return divergenceSet;
    }

    /**
     * Detect and remove outliers (see {@link OutlierEliminator} interface for further details) from the possible
     * candidates set.
     * DEFAULT algorithm is CentroidMedianEliminator
     *
     * @param nearestNeighbours set of fingerprints, already containing possible nearest neighbours
     */
    private void removeOutliers(final SortedSet<NicGeoPoint> nearestNeighbours) {
        OutlierEliminator eliminator;
        switch (outlierMode){
            case OUTLIER_MODE_NO_DETECTION:
                return;
            case OUTLIER_MODE_PLASMONA:
                eliminator = new PlasmonaOutlierEliminator(new NicGeoPointFactory());
                break;
            case OUTLIER_MODE_CME:
            default:
                final int threshold = ctx.getResources().getInteger(R.integer.tracker_outlier_medianThresholdFactor);
                eliminator = new CentroidMedianEliminator(new NicGeoPointFactory(), threshold);
                break;
        }
        eliminator.removeOutliers(nearestNeighbours);
    }


    /**
     * Calculates median or average GeoPoint from n amounts of GeoPoints.
     * DEFAULT: median
     *
     * @param point GeoPoint as the result of interpolation
     * @return the new user position from the history
     */
    NicGeoPoint calculatePositionFromHistory(final NicGeoPoint point) {
        switch (statisticFilterMode) {
            case STATISTIC_FILTER_MODE_NO_FILTER:
                return point;
            case STATISTIC_FILTER_MODE_AVERAGE:
                history.add(point);
                return history.getAveragePoint();
            default:
                history.add(point);
                return history.getMedianPoint();
        }
    }

    /**
     * In case some indoor ways are present, the point will snap to the nearest way.
     *
     * @param point NicGeoPoint interpolated position
     * @return NicGeoPoint point snapped to the nearest way
     */
    NicGeoPoint calculateWaySnappedPoint(final NicGeoPoint point) { //NOSONAR - Will be used after Euroshop
        switch (mapMatchingMode) {
            case MAP_MATCHING_MODE_NONE:
                return point;
            case MAP_MATCHING_MODE_SIMPLE_WAY_SNAP:
            default:
                return calculateSimpleWaySnapPoint(point);
        }
    }

    /**
     * In case some indoor ways are present, the point will snap to the nearest way.
     *
     * @param point NicGeoPoint interpolated position
     * @return NicGeoPoint point snapped to the nearest way
     */
    NicGeoPoint calculateSimpleWaySnapPoint(final NicGeoPoint point) {
        if (wayManager != null) {
            final Collection<Edge> edges = wayManager.getEdges();
            if (edges != null && !edges.isEmpty()) {
                final SimpleWaySnap sws = new SimpleWaySnap(new NicGeoPointFactory(), edges);
                return sws.snap(point);
            }
        } else {
            Log.e(TAG, "MapMatchingMode is MAP_MATCHING_MODE_SIMPLE_WAY_SNAP but not WayManager is defined!");
        }
        return point;
    }

    /**
     * Adds the movement difference obtained by dead reckoning to the current position depending on the map-scale
     *
     * @param currentPoint current position
     * @param normDelta dead reckoning delta
     * @return estimate for new position, including the dead reckoning delta
     */
    NicGeoPoint addDelta(NicGeoPoint currentPoint, NicGeoPoint normDelta) {
        // The delta is now kilo-delta, as a workaround for the low GeoPoint-E2-resolution!
        final double x = currentPoint.getX() + normDelta.getX() * scale / 1000f;
        final double y = currentPoint.getY() + normDelta.getY() * scale / 1000f;
        currentPoint.setXY(x, y);
        return currentPoint;
    }

    // This is the update-stage where some additional algorithms are applied, after we have incorporated all the
    // sensor-data.
    private void updatePosition(final NicGeoPoint point) {
        final NicGeoPoint waySnappedPoint = calculateWaySnappedPoint(point);

        final NicGeoPointImpl statisticFilterResultPoint =
                (NicGeoPointImpl) calculatePositionFromHistory(waySnappedPoint);

        final UserPositionItem position = new UserPositionItem(statisticFilterResultPoint);
        final List<UserPositionItem> pointList = new ArrayList<UserPositionItem>();
        pointList.add(position);
        userPositionManager.updateItems(pointList);


        // If this is the admin-app (sorry for the ugly hack) then we draw the lines between neighbours for debugging,
        // but only in case wifi is on (because dead reckoning has no neighbours).
        // Maybe we should subclass the UserLocator for the admin-app.
        if (ctx.getClass().getCanonicalName().equals("de.tarent.nic.android.admin.MapActivity") &&
                (lastWifiPoint != null)) {
            // TODO: maybe we should just clear the lastNeighbours once, when we don't have the wifiCapturer?
            if (sensorCollectors.contains(wifiCapturer)) {
                userPositionManager.setNeighbours(lastNeighbours);
            } else {
                userPositionManager.setNeighboursWithOutliers(new TreeSet<NicGeoPoint>());
                userPositionManager.setNeighbours(new TreeSet<NicGeoPoint>());
            }
        }
    }
}