package de.tarent.nic.android.base.sensor;

/**
 * A SensorCollector collects data from a sensor. It needs to be started and stopped, so that it can [un-]register
 * any listeners that it needs.
 */
public interface SensorCollector {

    /**
     * Start the sensors.
     */
    void startSensors();

    /**
     * Stop the sensors and clean up any listeners, timers, etc.
     */
    void stopSensors();

}