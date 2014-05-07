package de.tarent.nic.android.base.wifi;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.SensorManager;
import de.tarent.nic.android.base.mapmatching.WayManager;
import de.tarent.nic.android.base.particlefilter.ParticleFilter;
import de.tarent.nic.android.base.position.History;
import de.tarent.nic.android.base.position.NicGeoPointImpl;
import de.tarent.nic.android.base.position.UserPositionManager;
import de.tarent.nic.android.base.sensor.DeadReckoning;
import de.tarent.nic.entities.Edge;
import de.tarent.nic.entities.Fingerprint;
import de.tarent.nic.entities.Histogram;
import de.tarent.nic.entities.NicGeoPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


@Config(manifest = "../base/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class UserLocatorTest {

    private UserLocator userLocator;

    private Context context;

    private FingerprintManager fingerprintManager;

    private UserPositionManager userPositionManager;

    private WayManager wayManager;

    private History history;

    private SortedSet<NicGeoPoint> nearestNeighboursLatLong;

    private Set<Edge> edges;

    private Activity activity;

    private SensorManager sensorManager;

    private PackageManager packageManager;

    private DeadReckoning deadReckoning;

    @Before
    public void setup() {
        context = mock(Context.class);
        fingerprintManager = mock(FingerprintManager.class);
        userPositionManager = mock(UserPositionManager.class);
        wayManager = mock(WayManager.class);
        history = mock(History.class);
        activity = mock(Activity.class);
        sensorManager = mock(SensorManager.class);
        packageManager = mock(PackageManager.class);


        when(context.getResources()).thenReturn(Robolectric.application.getResources());

        new UserLocator(context, fingerprintManager, userPositionManager, wayManager);

        userLocator = new UserLocator(context, fingerprintManager, userPositionManager, wayManager, history);

        NicGeoPoint nicGeoPoint1 = new NicGeoPointImpl(0, 0);
        nicGeoPoint1.setDivergence(1);
        NicGeoPoint nicGeoPoint2 = new NicGeoPointImpl(5000000, 5000000);
        nicGeoPoint2.setDivergence(2);
        NicGeoPoint nicGeoPoint3 = new NicGeoPointImpl(4500000, 4500000);
        nicGeoPoint3.setDivergence(3);

        nearestNeighboursLatLong = new TreeSet<NicGeoPoint>();
        nearestNeighboursLatLong.add(nicGeoPoint1);
        nearestNeighboursLatLong.add(nicGeoPoint2);
        nearestNeighboursLatLong.add(nicGeoPoint3);

        edges = new HashSet<Edge>();
        edges.add(new Edge(nicGeoPoint2, nicGeoPoint3));

        userLocator.setMapMatchingMode(-1);
        when(activity.getSystemService(Activity.SENSOR_SERVICE)).thenReturn(sensorManager);
        when(activity.getPackageManager()).thenReturn(packageManager);
        when(activity.getResources()).thenReturn(Robolectric.application.getResources());

        NicGeoPoint center = new NicGeoPointImpl();
        center.setXY(10.f, 12.f);
        when(userPositionManager.getBoundingBoxCenter()).thenReturn(center);

        deadReckoning = new DeadReckoning(activity);

        userLocator.startTracking();
    }

    @Test
    public void testAddHistogramCallsCorrectMethodsFromUserPositionManager() {
        userLocator.setLocalizationMethod(userLocator.LOCALIZATION_MODE_WIFI);
        doNothing().when(userPositionManager).setNeighboursWithOutliers(any(TreeSet.class));
        doNothing().when(userPositionManager).updateItems(any(List.class));
        doNothing().when(userPositionManager).setNeighbours(any(SortedSet.class));

        List<Fingerprint> fingerprints = new ArrayList<Fingerprint>();
        Fingerprint fingerprint = new Fingerprint(mock(Histogram.class), mock(NicGeoPointImpl.class));
        fingerprints.add(fingerprint);

        when(fingerprintManager.getFingerprints()).thenReturn(fingerprints);
        when(wayManager.getEdges()).thenReturn(edges);

        Histogram histogram = new Histogram();

        userLocator.addHistogram(histogram);

        // We need to wait until the localisationTimer has had a chance to process the new position:
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // It is too difficult to know how often these will be called because we cannot know when the timer was started:
        verify(userPositionManager, atLeast(1)).setNeighboursWithOutliers(any(TreeSet.class));
        verify(userPositionManager, atLeast(1)).updateItems(any(List.class));
        verify(userPositionManager, atLeast(1)).getBoundingBoxCenter();
        verifyNoMoreInteractions(userPositionManager);
    }

    @Test
    public void testThatDisablePositionCallsDisablePositionFromUserPositionManager() {
        doNothing().when(userPositionManager).disablePosition();

        userLocator.disablePosition();

        verify(userPositionManager, times(1)).disablePosition();
        // We don't care whether the UserLocator calls this, but it's ok if it wants to:
        verify(userPositionManager, atMost(1)).getBoundingBoxCenter();

        verifyNoMoreInteractions(userPositionManager);
    }

    @Test
    public void testThatRemoveOutliersRemovesNoGeoPointsWhenOutlierModeIsSetToNoDetection() {
        userLocator.setOutlierMode(UserLocator.OUTLIER_MODE_NO_DETECTION);
        UserLocator userLocatorSpy = spy(userLocator);

        SortedSet<NicGeoPoint> expectedNearestNeighbours = new TreeSet<NicGeoPoint>(nearestNeighboursLatLong);

        doReturn(nearestNeighboursLatLong).when(userLocatorSpy).getNearestNeighbours(any(SortedSet.class));
        when(wayManager.getEdges()).thenReturn(edges);

        Histogram histogram = new Histogram();
        userLocatorSpy.addHistogram(histogram);

        assertEquals(expectedNearestNeighbours, nearestNeighboursLatLong);
    }

    @Test
    public void testThatRemoveOutliersRemovesOutlierGeoPointsWhenOutlierModeIsSetToPlasmona() {
        userLocator.setOutlierMode(UserLocator.OUTLIER_MODE_PLASMONA);
        UserLocator userLocatorSpy = spy(userLocator);

        SortedSet<NicGeoPoint> expectedNearestNeighbours = new TreeSet<NicGeoPoint>(nearestNeighboursLatLong);
        expectedNearestNeighbours.remove(expectedNearestNeighbours.first()); // The first is 0,0 which is the outlier.

        doReturn(nearestNeighboursLatLong).when(userLocatorSpy).getNearestNeighbours(any(SortedSet.class));
        when(wayManager.getEdges()).thenReturn(edges);

        Histogram histogram = new Histogram();
        userLocatorSpy.addHistogram(histogram);

        assertEquals(expectedNearestNeighbours, nearestNeighboursLatLong);
    }

    @Test
    public void testThatRemoveOutliersRemovesOutlierGeoPointsWhenOutlierModeIsSetToCME() {
        userLocator.setOutlierMode(UserLocator.OUTLIER_MODE_CME);
        UserLocator userLocatorSpy = spy(userLocator);

        SortedSet<NicGeoPoint> expectedNearestNeighbours = new TreeSet<NicGeoPoint>(nearestNeighboursLatLong);
        expectedNearestNeighbours.remove(expectedNearestNeighbours.first()); // The first is 0,0 which is the outlier.

        doReturn(nearestNeighboursLatLong).when(userLocatorSpy).getNearestNeighbours(any(SortedSet.class));
        when(wayManager.getEdges()).thenReturn(edges);

        Histogram histogram = new Histogram();
        userLocatorSpy.addHistogram(histogram);


        assertEquals(expectedNearestNeighbours, nearestNeighboursLatLong);
    }

    @Test
    public void testThatCalculatePositionFromHistoryDoesNothingWhenStatisticModeIsNoFilter() {
        userLocator = new UserLocator(context, fingerprintManager, userPositionManager, wayManager, history);
        userLocator.startTracking();

        userLocator.setStatisticFilterMode(UserLocator.STATISTIC_FILTER_MODE_NO_FILTER);
        when(wayManager.getEdges()).thenReturn(edges);

        Histogram histogram = new Histogram();
        userLocator.addHistogram(histogram);

        verifyNoMoreInteractions(history);
    }

    @Test
    public void testThatCalculatePositionFromHistoryCallsGetMedianPointWhenStatisticModeIsMedian() {
        userLocator = new UserLocator(context, fingerprintManager, userPositionManager, wayManager, history);
        userLocator.startTracking();

        userLocator.setStatisticFilterMode(UserLocator.STATISTIC_FILTER_MODE_MEDIAN);
        when(wayManager.getEdges()).thenReturn(edges);

        Histogram histogram = new Histogram();
        userLocator.addHistogram(histogram);

        // We need to wait until the localisationTimer has had a chance to process the new position:
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        verify(history, atLeast(1)).add(any(NicGeoPoint.class));
        verify(history, atLeast(1)).getMedianPoint();
        verifyNoMoreInteractions(history);
    }

    @Test
    public void testThatCalculatePositionFromHistoryCallsGetAveragePointWhenStatisticModeIsAverage() {
        userLocator = new UserLocator(context, fingerprintManager, userPositionManager, wayManager, history);
        userLocator.startTracking();

        userLocator.setStatisticFilterMode(UserLocator.STATISTIC_FILTER_MODE_AVERAGE);
        when(wayManager.getEdges()).thenReturn(edges);

        Histogram histogram = new Histogram();
        userLocator.addHistogram(histogram);

        // We need to wait until the localisationTimer has had a chance to process the new position:
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        verify(history, atLeast(1)).add(any(NicGeoPoint.class));
        verify(history, atLeast(1)).getAveragePoint();
        verify(history, atLeast(1)).getMedianPoint();
        verifyNoMoreInteractions(history);
    }

    @Test
    public void testThatCalculateNearestNeighboursReturns2PointsWhen2AreWantedAnd3AreGiven() {
        Context context = mock(Context.class);
        Resources resources = mock(Resources.class);
        when(context.getResources()).thenReturn(resources);
        when(resources.getInteger(any(Integer.class))).thenReturn(2);

        userLocator = new UserLocator(context, fingerprintManager, userPositionManager, wayManager, history);
        userLocator.startTracking();

        SortedSet<NicGeoPoint> nearestNeighboursOut = userLocator.getNearestNeighbours(nearestNeighboursLatLong);
        assertEquals(2, nearestNeighboursOut.size());
    }

    @Test
    public void testThatCalculateNearestNeighboursReturns1PointWhen1IsWantedAnd3AreGiven() {
        Context context = mock(Context.class);
        Resources resources = mock(Resources.class);
        when(context.getResources()).thenReturn(resources);
        when(resources.getInteger(any(Integer.class))).thenReturn(1);

        userLocator = new UserLocator(context, fingerprintManager, userPositionManager, wayManager, history);
        userLocator.startTracking();

        SortedSet<NicGeoPoint> nearestNeighboursOut = userLocator.getNearestNeighbours(nearestNeighboursLatLong);
        assertEquals(1, nearestNeighboursOut.size());
    }

    @Test
    public void testAddRemoveSensorCollector() {
        userLocator.addSensorCollector(deadReckoning);
        assertTrue(1 == userLocator.sensorCollectors.size());

        userLocator.addSensorCollector(deadReckoning);
        assertTrue(1 == userLocator.sensorCollectors.size());

        userLocator.addSensorCollector(deadReckoning);
        assertTrue(1 == userLocator.sensorCollectors.size());

        userLocator.removeSensorCollector(deadReckoning);
        userLocator.removeSensorCollector(deadReckoning);

        assertTrue(0 == userLocator.sensorCollectors.size());
    }

    @Test
    public void testStartStopTracking() {
        userLocator.stopTracking();
        DeadReckoning deadReckoning = mock(DeadReckoning.class);
        userLocator.addSensorCollector(deadReckoning);
        userLocator.startTracking();
        userLocator.startTracking();

        // startSensors() is intentionally executed twice, once from addSensorCollector(deadReckoning) and again
        // form startTracking()
        verify(deadReckoning).startSensors();

        userLocator.stopTracking();
        userLocator.stopTracking();

        verify(deadReckoning).stopSensors();
    }

    @Test
    public void testAddDelta() {
        NicGeoPoint current = new NicGeoPointImpl();
        NicGeoPoint delta = new NicGeoPointImpl();
        current.setXY(0.f, 0.f);
        delta.setXY(0.3f * 1000f, 0.86f * 1000f);
        userLocator.setScale(1.8f);
        userLocator.addDelta(current, delta);
        assertEquals(0.54f, current.getX(), 0.25f);
        assertEquals(1.548f, current.getY(), 0.25f);
    }

    @Test
    public void testAddNoDelta() {
        NicGeoPoint current = new NicGeoPointImpl();
        NicGeoPoint delta = new NicGeoPointImpl();
        current.setXY(1.f, 1.f);
        delta.setXY(0.f, 0.f);
        userLocator.setScale(1.8f);

        userLocator.addDelta(current, delta);
        assertEquals(1.f, current.getX(), 0.15f);
        assertEquals(1.f, current.getY(), 0.15f);

    }

    @Test
    public void testLocalizationLoopWithDefault() {
        NicGeoPoint delta = new NicGeoPointImpl();
        delta.setXY(1.f * 1000f, 0.f * 1000f);

        DeadReckoning deadReckoning = mock(DeadReckoning.class);
        when(deadReckoning.getDelta()).thenReturn(delta);
        userLocator.addSensorCollector(deadReckoning);
        userLocator.setScale(1.f);
        userLocator.setLocalizationMethod(userLocator.LOCALIZATION_MODE_DEADRECKONING);

        userLocator.localisationLoop();

        assertEquals(11.f, userLocator.currentPoint.getX(), 0.25f);
        assertEquals(12.f, userLocator.currentPoint.getY(), 0.25f);

    }

   @Test
    public void testLocalizationLoopWithPf() {
        userLocator.setLocalizationMethod(userLocator.LOCALIZATION_MODE_PARTICLEFILTER);

        NicGeoPoint nextWifi = new NicGeoPointImpl();

        userLocator.localisationLoop();

        nextWifi.setXY(1.f, 1.f);
        userLocator.nextWifiPoint = nextWifi;
        userLocator.localisationLoop();
        assertNull(userLocator.nextWifiPoint);

        // no nextWifiPoint set
        NicGeoPoint delta = new NicGeoPointImpl();
        delta.setXY(20.f, 20.f);
        DeadReckoning deadReckoning = mock(DeadReckoning.class);
        when(deadReckoning.getDelta()).thenReturn(delta);
        userLocator.addSensorCollector(deadReckoning);

        NicGeoPoint curr = userLocator.currentPoint;
        userLocator.localisationLoop();
        verify(deadReckoning, atLeast(1)).getDelta();

        // new sensor measurement
        nextWifi.setXY(4.f, 5.f);
        userLocator.nextWifiPoint = nextWifi;
        userLocator.localisationLoop();

        assertNull(userLocator.nextWifiPoint);
    }

    @Test
    public void testNoMapMatchingType() {
        NicGeoPoint test = new NicGeoPointImpl();

        assertTrue(test == userLocator.calculateWaySnappedPoint(test));
    }

    @Test
    public void testNoWayManager() {
        NicGeoPoint test = new NicGeoPointImpl();
        test.setXY(10.f, 12.f);
        userLocator = new UserLocator(context, fingerprintManager, userPositionManager, null, history);
        NicGeoPoint result = userLocator.calculateSimpleWaySnapPoint(test);

        assertEquals(10.f, result.getX(), 0.15f);
        assertEquals(12.f, result.getY(), 0.15f);
    }

    class ParticleFilterTestable extends ParticleFilter {
        public double nextRandomValue = 0.5f;
        public double nextGaussianValue = 0.05f;

        ParticleFilterTestable() {
            super(activity, userPositionManager);
        }

        protected double nextRandom() {
            return nextRandomValue;
        }

        protected double nextGaussian() {
            return nextGaussianValue;
        }
    }
}
