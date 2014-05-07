package de.tarent.nic.android.base.sensor;

/**
 * A SingleWeightFilter is a SensorFilter which can be configured with a single float value.
 * Default bounds for this value are [0,1].
 *
 * @param <T> whatever type the concrete filter class wants to filter.
 */
public abstract class SingleWeightFilter<T> implements SensorFilter<T> {

    protected float weight;

    protected float min = 0f;
    protected float max = 1f;


    public float getWeight() {
        return weight;
    }

    /**
     * Change the value for weight.
     *
     * @param weight new weight
     */
    public void setWeight(float weight) {
        if ((weight < min) || (weight > max)) {
            throw new IllegalArgumentException("Weight has a value range of {"+min+",...,"+max+"}.");
        }
        this.weight = weight;
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

}
