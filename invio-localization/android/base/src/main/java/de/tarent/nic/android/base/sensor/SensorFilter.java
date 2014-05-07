package de.tarent.nic.android.base.sensor;


/**
 * Interface for implementing different filters to filter sensor data stored in a Sensor Buffer.
 * The filter works step by step. That means it can use all information that its SensorBuffer provides to produce
 * one new signal value per call. It does not filter the signal, that is stored in the SensorBuffer, in the sense that
 * a whole new signal is created at once.
 *
 * @param <T> type of the buffer, i.e. what kind of objects it stores, to choose a suitable filter
 */
public interface SensorFilter<T> {

    /**
     * Filters the given sensor data. You should not call this method directly, because it depends on the cooperation
     * of the SensorBuffer. Only the SensorBuffer should call it.
     *
     * @param buffer the input data
     * @return the result of the filtering
     */
    T filter(SensorBuffer<T> buffer);

}
