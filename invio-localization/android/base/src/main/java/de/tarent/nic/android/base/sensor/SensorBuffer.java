package de.tarent.nic.android.base.sensor;


import java.util.ArrayList;
import java.util.List;

/**
 * Sensor Buffer saves the last x sensor measurements in a generic list
 * @param <T> generic type for the sensor buffer
 */

public class SensorBuffer<T> {

    private List<T> buffer;
    private final int size;

    private SensorFilter<T> filter;
    private T filteredValue;

    /**
     * Constructor for an empty Sensor Buffer.
     *
     * @param size size of the buffer
     */
    public SensorBuffer(int size) {
        this.size = size;
        buffer = new ArrayList<T>();
    }

    /**
     * Get the size of the buffer.
     *
     * @return size of buffer
     */
    public int size() {
        return size;
    }

    public SensorFilter<T> getFilter() {
        return filter;
    }

    /**
     * Sets a sensor filter for filtering the sensor data.
     *
     * @param filter the filter e.g. low pass
     */
    public void setFilter(SensorFilter filter) {
        this.filter = filter;
    }

    /**
     * Returns last filtered value.
     *
     * @return filtered value
     */
    public T getFilteredValue() {
        return filteredValue;
    }

    /**
     * Clears the buffer.
     */
    public void clear() {
        buffer.clear();
    }

    /**
     * Returns element of buffer at position i.
     *
     * @param i position of the element
     * @return the element at position i
     */
    public T get(int i) {
        if (i < this.buffer.size()) {
            return buffer.get(i);
        }
        else {
            return null;
        }
    }

    /**
     * Gets the last element added to the buffer.
     *
     * @return last element
     */
    public T getLastElement() {
        return this.buffer.get(this.buffer.size()-1);
    }

    /**
     * Returns the whole buffer.
     *
     * @return the buffer
     */
    public List<T> getBuffer() {
        return buffer;
    }

    /**
     * Sets buffer and takes the newest measurements if buffer is bigger than size.
     *
     * @param buffer new buffer
     */
    public void setBuffer(List<T> buffer) {
        this.buffer = buffer;
        if (buffer.size() > this.size) {
            for (int i = 0; i <= (buffer.size()-this.size); i++) {
                this.buffer.remove(i);
            }
        }
    }

    /**
     * Adds a new measurement to the buffer while keeping the size of the buffer constant.
     *
     * @param data new sensor measurement
     */
    public void addMeasurement(T data) {
        buffer.add(data);
        if (this.buffer.size() > this.size) {
            buffer.remove(0);
        }

        if (filter != null) {
            filteredValue = filter.filter(this);
        }
    }

}
