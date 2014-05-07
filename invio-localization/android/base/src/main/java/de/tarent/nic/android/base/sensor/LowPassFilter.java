package de.tarent.nic.android.base.sensor;

/**
 * Implements a simple low pass filter for filtering multidimensional sensor data.
 */
public class LowPassFilter extends SingleWeightFilter<float[]> {


    /**
     * Constructor of LowPassFilter class.
     *
     * @param weight initialize with a weight
     *               Note: a weight outside of the valid range will result in an IllegalArgumentException!
     */
    public LowPassFilter(float weight) {
        super.setWeight(weight);
    }


    /**
     * Filters incoming sensor data with a low pass filter, 0 is default for no filtered value available.
     *
     * @param buffer the input buffer
     * @return the filtered sensor data
     */
    @Override
    public float[] filter(SensorBuffer<float[]> buffer) {
        float[] lastMean = buffer.getFilteredValue();
        lastMean = (lastMean != null) ? lastMean : new float[buffer.getLastElement().length];
        final float[] newMean = new float[lastMean.length];
        for (int i = 0; i < newMean.length; i++) {
            newMean[i] = lastMean[i] * (1.0f - weight) + buffer.getLastElement()[i] * weight;
        }

        return newMean;
    }

}
