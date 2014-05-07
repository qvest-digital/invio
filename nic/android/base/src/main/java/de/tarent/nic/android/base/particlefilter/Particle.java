package de.tarent.nic.android.base.particlefilter;


/**
 *  Class for particle entities representing one state hypothesis used by the particle filter
 */
public class Particle {

    private double x;
    private double y;

    private double importanceWeight;

    /**
     * Constructor for a particle
     * @param x coordinate
     * @param y coordinate
     * @param weight representing the probability for being the right position
     */
    public Particle( double x, double y, float weight) {
        this.x = x;
        this.y = y;
        this.importanceWeight = weight;
    }

    /**
     * Sets the state of the particle object in xy-coordinates
     * @param x coordinate
     * @param y coordinate
     */
    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setWeight(double weight) {
        this.importanceWeight = weight;
    }

    public double getWeight() {
        return importanceWeight;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
