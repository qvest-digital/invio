package de.tarent.nic.android.base.sensor;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import de.tarent.nic.android.base.R;
import de.tarent.nic.android.base.position.NicGeoPointImpl;
import de.tarent.nic.android.base.task.DownloadListener;
import de.tarent.nic.android.base.task.DownloadTask;
import de.tarent.nic.android.base.task.OsmParserKeys;
import de.tarent.nic.entities.NicGeoPoint;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * This class collects different sensor data to detect movement and its direction and then calculates the
 * delta vector from the previously determined position.
 */
public class DeadReckoning implements SensorEventListener, SensorCollector, DownloadListener<Map<String, Collection>> {
    private static final String TAG = DeadReckoning.class.getCanonicalName();

    Float azimuth;
    boolean moving = false;
    double baseAngle;

    int timestep;
    float motionModel;
    float movementDetectionSensitivity;

    boolean hasGyro;

    float[] delta = new float[2];

    final SensorManager sensorManager;

    float[] valuesLinearAcceleration = new float[3];
    float[] valuesAccelerometer = new float[3];
    float[] valuesMagneticField = new float[3];

    final float[] matrixR = new float[9];
    final float[] matrixI = new float[9];
    final float[] matrixValues = new float[3];

    TimerTask timerTask;
    Timer accumulateDeltaTimer;

    SensorBuffer<Float> compassBuffer = new SensorBuffer<Float>(5);
    private SensorBuffer<float[]> accBuffer = new SensorBuffer<float[]>(5);
    private SensorBuffer<float[]> linearAccBuffer = new SensorBuffer<float[]>(5);
    private SensorBuffer<float[]> magnBuffer = new SensorBuffer<float[]>(5);

    private SingleWeightFilter compassFilter;

    private float compassFilterWeight;

    private final Object mutex = new Object();


    /**
     * Constructs a new {@link de.tarent.nic.android.base.sensor.DeadReckoning} instance.
     *
     * @param activity calling this class in order to get access to the sensor service
     */
    public DeadReckoning(final Activity activity) {
        sensorManager = (SensorManager) activity.getSystemService(activity.SENSOR_SERVICE);

        final Resources res = activity.getResources();

        initFilters(res);

        timestep = res.getInteger(R.integer.accumulateDeltaUpdateRate);
        motionModel = res.getInteger(R.integer.motionModel);
        movementDetectionSensitivity = res.getInteger(R.integer.movement_detection_sensitivity) / 100f;

        // We take the square of the sensitivity so that we don't have to calculate a sqrt later:
        movementDetectionSensitivity = movementDetectionSensitivity * movementDetectionSensitivity;

        final PackageManager packageManager = activity.getPackageManager();
        hasGyro = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        if (!hasGyro) {
            // This should let us at least detect acceleration that adds to gravity, but smarter detection would really
            // be necessary...
            movementDetectionSensitivity += 120;
        }
    }


    /**
     * Starts listening to following sensors in order to use the dead reckoning:
     * <ul>
     *     <li>Sensor.TYPE_ACCELEROMETE. Die Deltas werden R</li>
     *     <li>Sensor.TYPE_MAGNETIC_FIELD</li>
     *     <li>Sensor.TYPE_LINEAR_ACCELERATION</li>
     * </ul>
     *
     * Note, that TYPE_LINEAR_ACCELERATION may not be present on every device. TYPE_ACCELEROMETER will be substituted
     * in those cases.
     *
     * Here we also start up the timer that will periodically produce the movement-deltas.
     */
    public void startSensors() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);

        // Create delta timer once
        if(accumulateDeltaTimer == null){
            accumulateDeltaTimer = new Timer("accumulateDeltaTimer");
        }

        // Because the timer task was probably canceled in the stopSensors() method or event does not exist yet
        // we must create a new instance of it in both cases.
        // TODO: what about the old timer? Should we really rely on someone to always call stopSensors?
        timerTask = new TimerTask() {
            @Override
            public void run() {
                calculateDeltaLoop();
            }
        };

        accumulateDeltaTimer.schedule(timerTask, 10, timestep);
    }

    /**
     * Stop listening to the sensors and unregister them. Also stop the timer that produces the movement-deltas.
     */
    public void stopSensors() {
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));

        if (timerTask != null) {
            timerTask.cancel();
        }
    }

    @Override
    public void onDownloadFinished(DownloadTask task, boolean success, Map<String, Collection> data) {
        if(success){
            final List<Integer> northAngleList = (List<Integer>) data.get(OsmParserKeys.NORTH_ANGLE);
            if ((northAngleList != null) && (!northAngleList.isEmpty()) && (northAngleList.get(0) != null)) {
                setBaseAngle(northAngleList.get(0));
            }
        } else {
            Log.e(TAG, "North angle download signals failure, so probably direction will be completely wrong.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSensorChanged(final SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, valuesAccelerometer, 0, event.values.length);
                valuesAccelerometer = filterSensorData(event.values, accBuffer);
                if (!hasGyro) {
                    // In that case we misuse the values from the accelerometer as if they came from
                    // the linear accelerometer, which we don't have:
                    valuesLinearAcceleration = filterSensorData(event.values, linearAccBuffer);
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                valuesMagneticField = filterSensorData(event.values, magnBuffer);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                valuesLinearAcceleration = filterSensorData(event.values, linearAccBuffer);
                break;
            default:
                return;
        }

        synchronized (mutex) {
            azimuth = calculateAzimuth();
            moving = detectMovement();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        // We don't need to implement it.
    }

    /**
     * Detects if the device is moving depending on linear acceleration sensor data.
     * @return true if device is moving
     */
    boolean detectMovement() {
        final float x = valuesLinearAcceleration[0];
        final float y = valuesLinearAcceleration[1];
        final float z = valuesLinearAcceleration[2];

        // We compare the square of the length and sensitivity, because we only need to know whether it's greater or not
        final float vectorLength = x*x + y*y + z*z;
        final boolean isMoving = (vectorLength >= movementDetectionSensitivity);

        return isMoving;
    }

    /**
     * Calculates the azimuth with north=0, east=pi/2, south=pi and west=3/2pi
     *
     * @return azimuth [rad]
     */
     Float calculateAzimuth(){
        // The azimuth will stay null if we can't get a proper reading from the SensorManager:
        Float azimuth = null;

        if (SensorManager.getRotationMatrix(matrixR, matrixI, valuesAccelerometer, valuesMagneticField)) {

            SensorManager.getOrientation(matrixR, matrixValues);

            // 0: azimuth, 1: roll, 2: pitch
            // filter azimuth
            float value = matrixValues[0];
            if (value < 0) {
                value += 2*Math.PI;
            }

            azimuth = filterSensorData(value, compassBuffer);
        }

        return azimuth;
    }

    /**
     * Calculates difference in position and angle by means of measurements and motion model
     */
    void calculateDeltaLoop() {
        synchronized (mutex) {
            double distance;

            // If not moving in a clear direction, don't set/change delta:
            if (moving && (azimuth != null)) {
                // Motion model in cm/s, timestep in ms:
                distance = motionModel * timestep/100.f;

                // switched due to different coordinate system of compass and polar coordinates
                delta[0] += distance * Math.sin(azimuth - baseAngle);
                delta[1] += distance * Math.cos(azimuth - baseAngle);
            }
        }
    }

    /**
     * Returns accumulated delta and clears it for new accumulation.
     *
     * @return delta in terms of a directional vector
     */
    public NicGeoPoint getDelta() {
        final NicGeoPoint tmpDelta = new NicGeoPointImpl();

        synchronized (mutex) {
            tmpDelta.setXY(delta[0]*1000, delta[1]*1000);
            delta[0] = 0;
            delta[1] = 0;
        }
        return tmpDelta;
    }

    /**
     * Toggle the compass low pass filter.
     *
     * @param useLowPassFilter true, if low pass filter will be enabled
     */
    public void setCompassFilterEnabled(boolean useLowPassFilter) {
        if (useLowPassFilter) {
            compassFilter.setWeight(compassFilterWeight);
            Log.d(TAG, "Low pass filter is enabled.");
        } else {
            // With a filter-weight of 1 the newest value will be used to 100%, i.e. it will not filter anything 
            compassFilter.setWeight(1f);
            Log.d(TAG, "Low pass filter is disabled.");
        }
    }

    public void setBaseAngle(final double baseAngle) {
        this.baseAngle = Math.toRadians(baseAngle);
    }


    private void initFilters(Resources res) {
        final float magnFilterWeight = res.getInteger(R.integer.magnetometer_low_pass_filter_weight) / 100.f;
        final float accFilterWeight = res.getInteger(R.integer.accelerometer_low_pass_filter_weight) / 100.f;
        compassFilterWeight = res.getInteger(R.integer.compass_low_pass_filter_weight) / 100.f;

        final LowPassFilter accFilter = new LowPassFilter(accFilterWeight);
        accBuffer.setFilter(accFilter);
        linearAccBuffer.setFilter(accFilter);

        compassFilter = new CompassFilter(compassFilterWeight);
        compassBuffer.setFilter(compassFilter);

        magnBuffer.setFilter(new LowPassFilter(magnFilterWeight));
    }


    private <T> T filterSensorData(T values, SensorBuffer<T> buffer) {
        buffer.addMeasurement(values);

        return buffer.getFilteredValue();
    }
}
