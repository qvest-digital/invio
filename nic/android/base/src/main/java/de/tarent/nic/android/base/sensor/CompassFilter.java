package de.tarent.nic.android.base.sensor;

/**
 * Implements a simple low pass filter for filtering compass data or periodic angles respectively
 */
public class CompassFilter extends SingleWeightFilter<Float> {


    /**
     * Constructor of CompassFilter class.
     *
     * @param weight initialize with a weight
     *               Note: a weight outside of the valid range will result in an IllegalArgumentException!
     */
    public CompassFilter(float weight) {
        super.setWeight(weight);
    }


    /**
     * Filters incoming sensor data with a low pass filter, 0 is default for no filtered value available.
     *
     * @param buffer the input buffer
     * @return the filtered sensor data
     */
    @Override
    public Float filter(SensorBuffer<Float> buffer) {
        float newMean;
        Float lastMean = buffer.getFilteredValue();
        lastMean = (lastMean != null) ? lastMean : buffer.getLastElement();

        final double newx = Math.cos(lastMean) * (1.0f - weight) + Math.cos(buffer.getLastElement()) * weight;
        final double newy = Math.sin(lastMean) * (1.0f - weight) + Math.sin(buffer.getLastElement()) * weight;
        newMean = (float)(Math.atan2(newy, newx));

        if (newMean < 0) {
            newMean += 2*Math.PI;
        }

        return newMean;
    }

}
