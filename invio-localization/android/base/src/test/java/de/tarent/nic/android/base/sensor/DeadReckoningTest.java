package de.tarent.nic.android.base.sensor;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import de.tarent.nic.android.base.R;
import de.tarent.nic.android.base.task.DownloadMapDataTask;
import de.tarent.nic.android.base.task.DownloadTask;
import de.tarent.nic.android.base.task.OsmParserKeys;
import de.tarent.nic.entities.NicGeoPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@Config(manifest = "../base/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class DeadReckoningTest {

    // The Object under Test:
    DeadReckoning deadReckoning;

    @Mock
    private Activity activity;

    @Mock
    private SensorManager sensorManager;

    @Mock
    private PackageManager packageManager;

    @Mock
    private TimerTask timerTask;

    @Mock
    private SensorBuffer compassBuffer;

    @Before
    public void setUp() {
        initMocks(this);

        when(activity.getSystemService(Activity.SENSOR_SERVICE)).thenReturn(sensorManager);
        when(activity.getPackageManager()).thenReturn(packageManager);
        when(activity.getResources()).thenReturn(Robolectric.application.getResources());

        deadReckoning = new DeadReckoning(activity);

        deadReckoning.onAccuracyChanged(null, 0);

    }

    @Test
    public void testDetectMovement() {
        deadReckoning.valuesLinearAcceleration[0] = 1.7f;
        deadReckoning.valuesLinearAcceleration[1] = 0.f;
        deadReckoning.valuesLinearAcceleration[2] = 0.f;
        deadReckoning.movementDetectionSensitivity = 2.25f;

        assertTrue(deadReckoning.detectMovement());
    }

    @Test
    public void testNoDetectMovement() {
        deadReckoning.valuesLinearAcceleration[0] = 0.3f;
        deadReckoning.valuesLinearAcceleration[1] = 1.2f;
        deadReckoning.valuesLinearAcceleration[2] = 0.8f;
        deadReckoning.movementDetectionSensitivity = 2.25f;

        assertFalse(deadReckoning.detectMovement());
    }

    @Test
    public void testInvalidBaseAngleData() {
        DownloadTask task = mock(DownloadMapDataTask.class);
        Map<String, Collection> data = new HashMap<String, Collection>();

        deadReckoning.onDownloadFinished(task, true, data);
        assertTrue(0 == deadReckoning.baseAngle);

        deadReckoning.onDownloadFinished(task, false, data);
        assertTrue(0 == deadReckoning.baseAngle);

        List<Integer> list = new ArrayList<Integer>();
        data.put(OsmParserKeys.NORTH_ANGLE, list);

        deadReckoning.onDownloadFinished(task, true, data);
        assertTrue(0 == deadReckoning.baseAngle);

        list.add(null);
        data.put(OsmParserKeys.NORTH_ANGLE, list);

        deadReckoning.onDownloadFinished(task, true, data);
        assertTrue(0 == deadReckoning.baseAngle);
    }

    @Test
    public void testCalculateBaseAngle() {
        DownloadTask task = mock(DownloadMapDataTask.class);
        Map<String, Collection> data = new HashMap<String, Collection>();
        List<Integer> list = new ArrayList<Integer>();
        list.add(90);
        data.put(OsmParserKeys.NORTH_ANGLE, list);

        deadReckoning.onDownloadFinished(task, true, data);

        assertEquals(Math.PI/2, deadReckoning.baseAngle, 0.01f);
    }

    @Test
    public void testDeltaNoMovement() {
        deadReckoning.delta[0] = 10.f;
        deadReckoning.delta[1]  = 12.f;
        deadReckoning.moving = false;
        double x = deadReckoning.delta[0];
        double y = deadReckoning.delta[1];
        deadReckoning.calculateDeltaLoop();
        assertTrue(x == deadReckoning.delta[0]);
        assertTrue(y == deadReckoning.delta[1]);
    }

    @Test
    public void testDeltaNoAzimuth() {
        deadReckoning.delta[0] = 10.f;
        deadReckoning.delta[1]  = 12.f;
        deadReckoning.azimuth = null;
        double x = deadReckoning.delta[0];
        double y = deadReckoning.delta[1];
        deadReckoning.calculateDeltaLoop();
        assertTrue(x == deadReckoning.delta[0]);
        assertTrue(y == deadReckoning.delta[1]);
    }

    @Test
    public void testCalculationDelta() {
        deadReckoning.moving = true;
        deadReckoning.azimuth = 1.57f;
        deadReckoning.motionModel = 1.f;
        deadReckoning.timestep = 100;
        deadReckoning.baseAngle = 1.57f;

        deadReckoning.calculateDeltaLoop();

        assertEquals(0.f, deadReckoning.delta[0], 0.02f);
        assertEquals(1.f, deadReckoning.delta[1], 0.02f);
    }

    @Test
    public void testClearDelta() {
        deadReckoning.delta[0] = 10.f;
        deadReckoning.delta[1]  = 12.f;
        deadReckoning.getDelta();
        assertTrue(0.f == deadReckoning.delta[0]);
        assertTrue(0.f == deadReckoning.delta[1]);
    }

    @Test
    public void testStopWithoutStart() {
        deadReckoning.stopSensors();
        deadReckoning.stopSensors();
    }

    @Test
    public void testDoubleStart() {
        deadReckoning.startSensors();
        deadReckoning.startSensors();
    }

    @Test
    public void testTimerCancelled() {
        deadReckoning.startSensors();
        deadReckoning.timerTask = timerTask;
        deadReckoning.stopSensors();

        verify(timerTask).cancel();
    }

    @Test
    public void testDisableCompassFilter() {
        deadReckoning.setCompassFilterEnabled(false);
        assertEquals(1f, ((CompassFilter) deadReckoning.compassBuffer.getFilter()).getWeight());
    }

    @Test
    public void testEnableCompassFilter() {
        deadReckoning.setCompassFilterEnabled(true);
        assertEquals(Robolectric.application.getResources().getInteger(R.integer.compass_low_pass_filter_weight) / 100.f,
                ((CompassFilter) deadReckoning.compassBuffer.getFilter()).getWeight());
    }

    // For this test it is assumed that the orientation of the map image matches the real world perfectly.
    @Test
    public void testCalculationDeltaNoBaseAngle() {
        deadReckoning.moving = true;
        deadReckoning.motionModel = 1.f;
        deadReckoning.timestep = 100;

        deadReckoning.baseAngle = (float)Math.toRadians(0f);

        deadReckoning.azimuth = (float)Math.toRadians(0f);
        deadReckoning.calculateDeltaLoop();
        NicGeoPoint delta = deadReckoning.getDelta();
        assertEquals(0.f, delta.getX(), 0.02f);
        assertEquals(1000.f, delta.getY(), 0.02f);

        deadReckoning.azimuth = (float)Math.toRadians(45f);
        deadReckoning.calculateDeltaLoop();
        delta = deadReckoning.getDelta();
        assertEquals(1000.f / Math.sqrt(2), delta.getX(), 0.02f);
        assertEquals(1000.f / Math.sqrt(2), delta.getY(), 0.02f);

        deadReckoning.azimuth = (float)Math.toRadians(90f);
        deadReckoning.calculateDeltaLoop();
        delta = deadReckoning.getDelta();
        assertEquals(1000.f, delta.getX(), 0.02f);
        assertEquals(0.f, delta.getY(), 0.02f);

        deadReckoning.azimuth = (float)Math.toRadians(180f);
        deadReckoning.calculateDeltaLoop();
        delta = deadReckoning.getDelta();
        assertEquals(0.f, delta.getX(), 0.02f);
        assertEquals(-1000.f, delta.getY(), 0.02f);

        deadReckoning.azimuth = (float)Math.toRadians(270f);
        deadReckoning.calculateDeltaLoop();
        delta = deadReckoning.getDelta();
        assertEquals(-1000.f, delta.getX(), 0.02f);
        assertEquals(0.f, delta.getY(), 0.02f);
    }

    // 45 means that the map-image would have to be rotated 45° clockwise to match the real world.
    @Test
    public void testCalculationDeltaBaseAnglePlus45() {
        deadReckoning.moving = true;
        deadReckoning.motionModel = 1.f;
        deadReckoning.timestep = 100;

        deadReckoning.baseAngle = (float)Math.toRadians(45f);

        deadReckoning.azimuth = (float)Math.toRadians(0f);
        deadReckoning.calculateDeltaLoop();
        NicGeoPoint delta = deadReckoning.getDelta();
        assertEquals(-1000.f / Math.sqrt(2), delta.getX(), 0.02f);
        assertEquals(1000.f / Math.sqrt(2), delta.getY(), 0.02f);

        deadReckoning.azimuth = (float)Math.toRadians(45f);
        deadReckoning.calculateDeltaLoop();
        delta = deadReckoning.getDelta();
        assertEquals(0.f, delta.getX(), 0.02f);
        assertEquals(1000.f, delta.getY(), 0.02f);

        deadReckoning.azimuth = (float)Math.toRadians(90f);
        deadReckoning.calculateDeltaLoop();
        delta = deadReckoning.getDelta();
        assertEquals(1000.f / Math.sqrt(2), delta.getX(), 0.02f);
        assertEquals(1000.f / Math.sqrt(2), delta.getY(), 0.02f);

        deadReckoning.azimuth = (float)Math.toRadians(180f);
        deadReckoning.calculateDeltaLoop();
        delta = deadReckoning.getDelta();
        assertEquals(1000.f / Math.sqrt(2), delta.getX(), 0.02f);
        assertEquals(-1000.f / Math.sqrt(2), delta.getY(), 0.02f);

        deadReckoning.azimuth = (float)Math.toRadians(270f);
        deadReckoning.calculateDeltaLoop();
        delta = deadReckoning.getDelta();
        assertEquals(-1000.f / Math.sqrt(2), delta.getX(), 0.02f);
        assertEquals(-1000.f / Math.sqrt(2), delta.getY(), 0.02f);
    }

    // -45 means that the map-image would have to be rotated 45° counterclockwise to match the real world.
    // This is similar to our tarent-office-maps which where measured to be at -40.
    @Test
    public void testCalculationDeltaBaseAngleMinus45() {
        deadReckoning.moving = true;
        deadReckoning.motionModel = 1.f;
        deadReckoning.timestep = 100;

        deadReckoning.baseAngle = (float)Math.toRadians(-45f);

        deadReckoning.azimuth = (float)Math.toRadians(0f);
        deadReckoning.calculateDeltaLoop();
        NicGeoPoint delta = deadReckoning.getDelta();
        assertEquals(1000.f / Math.sqrt(2), delta.getX(), 0.02f);
        assertEquals(1000.f / Math.sqrt(2), delta.getY(), 0.02f);

        deadReckoning.azimuth = (float)Math.toRadians(45f);
        deadReckoning.calculateDeltaLoop();
        delta = deadReckoning.getDelta();
        assertEquals(1000.f, delta.getX(), 0.02f);
        assertEquals(0.f, delta.getY(), 0.02f);

        deadReckoning.azimuth = (float)Math.toRadians(90f);
        deadReckoning.calculateDeltaLoop();
        delta = deadReckoning.getDelta();
        assertEquals(1000.f / Math.sqrt(2), delta.getX(), 0.02f);
        assertEquals(-1000.f / Math.sqrt(2), delta.getY(), 0.02f);

        deadReckoning.azimuth = (float)Math.toRadians(180f);
        deadReckoning.calculateDeltaLoop();
        delta = deadReckoning.getDelta();
        assertEquals(-1000.f / Math.sqrt(2), delta.getX(), 0.02f);
        assertEquals(-1000.f / Math.sqrt(2), delta.getY(), 0.02f);

        deadReckoning.azimuth = (float)Math.toRadians(270f);
        deadReckoning.calculateDeltaLoop();
        delta = deadReckoning.getDelta();
        assertEquals(-1000.f / Math.sqrt(2), delta.getX(), 0.02f);
        assertEquals(1000.f / Math.sqrt(2), delta.getY(), 0.02f);
    }

    @Test
    public void testAddMultipleDeltasBeforeResetting() {
        deadReckoning.moving = true;
        deadReckoning.motionModel = 1.f;
        deadReckoning.timestep = 100;
        deadReckoning.baseAngle = 0f;

        assertEquals(0.f, deadReckoning.delta[0], 0.002f);
        assertEquals(0.f, deadReckoning.delta[1], 0.002f);

        deadReckoning.azimuth = (float)Math.toRadians(90f);
        deadReckoning.calculateDeltaLoop();
        assertEquals(1.f, deadReckoning.delta[0], 0.002f);
        assertEquals(0.f, deadReckoning.delta[1], 0.002f);

        deadReckoning.azimuth = (float)Math.toRadians(45f);
        deadReckoning.calculateDeltaLoop();
        assertEquals(1.707f, deadReckoning.delta[0], 0.002f);
        assertEquals(0.707f, deadReckoning.delta[1], 0.002f);


        NicGeoPoint delta = deadReckoning.getDelta();
        assertEquals(1707.f, delta.getX(), 0.2f);
        assertEquals(707.f, delta.getY(), 0.2f);

        assertEquals(0.f, deadReckoning.delta[0], 0.002f);
        assertEquals(0.f, deadReckoning.delta[1], 0.002f);
    }

}