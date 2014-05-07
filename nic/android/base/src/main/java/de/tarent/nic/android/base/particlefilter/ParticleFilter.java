package de.tarent.nic.android.base.particlefilter;


import android.content.Context;
import android.content.res.Resources;
import de.tarent.nic.android.base.R;
import de.tarent.nic.android.base.position.NicGeoPointImpl;
import de.tarent.nic.android.base.position.UserPositionManager;
import de.tarent.nic.entities.NicGeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *  Particle Filter class for cleverly merging the wifi positions and the dead reckoning delta
 */
public class ParticleFilter {
    UserPositionManager userPositionManager;

    int numParticles;
    float sigmaInit;
    float sigmaAction;
    float sigmaSensor;

    float scale;

    boolean showParticlesEnabled;

    List<Particle> particleList = new ArrayList<Particle>();

    private Random random = new Random();

    /**
     * constructs a new particle filter
     * @param ctx to get resources
     * @param userPositionManager needed for drawing particles
     */
    public ParticleFilter(final Context ctx, UserPositionManager userPositionManager) {
        this.userPositionManager = userPositionManager;

        final Resources res = ctx.getResources();
        numParticles = res.getInteger(R.integer.numberParticles);
        sigmaInit = res.getInteger(R.integer.sigmaInit)/100.f;
        sigmaAction = res.getInteger(R.integer.sigmaAction)/100.f;
        sigmaSensor = res.getInteger(R.integer.sigmaSensor)/100.f;

        showParticlesEnabled = false;

        if (numParticles <= 0) {
            throw new IllegalArgumentException("Number of particles has to be greater than 0!");
        }
    }

    /**
     * Initialize the particle filter by means of a first wifi scan result
     * @param curX wifi position for init
     * @param curY wifi position for init
     */
    public void initialize(double curX, double curY) {

        for (int i = 0; i < numParticles; i++) {
            // take the first wifi position and add gaussian noise to spread the particles around the initial position
            // all particles have the same uniform weight at the beginning
            final double newX = nextGaussian() * sigmaInit * scale + curX;
            final double newY = nextGaussian() * sigmaInit * scale + curY;
            final Particle tmp = new Particle(newX, newY, 1.f/numParticles);

            particleList.add(tmp);
        }
        showParticles();
    }

    /**
     * Gets the delta from dead reckoning and moves the distribution in this direction
     * @param x delta x
     * @param y delta y
     */
    public void updateAction(double x, double y) {
        // propagate position through motion model
        for (Particle particle : particleList) {
            final double val = nextGaussian() * sigmaAction * scale;
            particle.setXY(particle.getX() + x + val, particle.getY() + y + val);
        }
        showParticles();
    }

    /**
     * Calculates importance weights after getting a new sensor measurement
     * @param x new wifi position x
     * @param y new wifi position y
     */
    public void updateSensor(double x, double y) {
        float normalize = 0.f;

        // determine the distance between each particle and the measured wifi-position
        for (int i = 0; i < numParticles; i++) {
            final double sqrX = (x - particleList.get(i).getX()) * (x - particleList.get(i).getX());
            final double sqrY = (y - particleList.get(i).getY()) * (y - particleList.get(i).getY());
            double sqrt = Math.sqrt(sqrX + sqrY);
            // avoid division by zero
            if (sqrt == 0.f) {
                sqrt = 0.000001f;
            }
            // take inverse distance as weight, so near particles have a higher weight
            final double dist = 1.f/sqrt;

            final double weight = dist;
            particleList.get(i).setWeight(weight);

            normalize += weight;
        }
        // normalize the weights , as they represent a probability for each hypothesis being true
        normalizeWeights(normalize);

    }

    private void normalizeWeights(float norm) {
        for (int i = 0; i < numParticles; i++) {
            particleList.get(i).setWeight(particleList.get(i).getWeight() / norm);
        }
    }

    /**
     * Takes the current particle set and resamples it with regard to the particles weights
     */
    public void resampling() {
        final List<Particle> tmpParticleList = new ArrayList<Particle>();

        // generate cumulative distribution function from particle weights
        final double[] cdf = new double[numParticles];

        cdf[0] = particleList.get(0).getWeight();
        for (int i = 1; i < numParticles; i++) {
            cdf[i] = cdf[i-1] + particleList.get(i).getWeight();
        }

        // sample a random value between 0 and 1/numParticles
        final double[] u = new double[numParticles+1];
        do {
            u[0] = nextRandom() * 1.f/numParticles;
        }
        while(!(u[0] > 0));
        int k = 0;

        // draw samples from the cumulative distribution function with a fixed interval of 1/numParticles
        for (int j = 0; j < numParticles; j++) {
            while (u[j] > cdf[k]) {
                k++;
            }
            // add noise to the chosen particles
            final double x = particleList.get(k).getX() + nextGaussian() * sigmaSensor * scale;
            final double y = particleList.get(k).getY() + nextGaussian() * sigmaSensor * scale;
            tmpParticleList.add(new Particle(x, y, 1.f/numParticles));
            u[j+1] = u[j] + 1.f/numParticles;
        }

        // set new resampled particle set
        particleList = tmpParticleList;

        showParticles();
    }

    /**
     * For testing purposes only
     * @return new random value
     */
    protected double nextRandom() {
        return Math.random();
    }

    /**
     * For testing purposes only
     * @return new sample from gaussian with mean 0 and standard deviation 1
     */
    protected double nextGaussian() {
        return random.nextGaussian();
    }

    /**
     * Clears the particle list to start a newly initialized tracking and to clear the drawn particles
     */
    public void clearParticles() {
        particleList.clear();
        showParticles();
    }

    public void setShowParticles(boolean enabled) {
        showParticlesEnabled = enabled;
    }

    public void setNumParticles(int particles) {
        numParticles = particles;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }


    private void showParticles() {
        if (showParticlesEnabled) {
            userPositionManager.setParticles(particleList);
        }
        else {
            final List<Particle> empty = new ArrayList<Particle>();
            userPositionManager.setParticles(empty);
        }
    }

    /**
     * Calculates the actual position of the user by means of taking the mean position of the best particle cluster
     * @return current position of the user
     */
    public NicGeoPoint calculatePosition() {
        final NicGeoPoint currentPosition = new NicGeoPointImpl();
        float meanX = 0.f;
        float meanY = 0.f;

        // assumption: unimodal distribution, therefore approximate position with a gaussian and take mean
        for (int i = 0; i < particleList.size(); i++) {
            meanX += particleList.get(i).getX();
            meanY += particleList.get(i).getY();
        }
        currentPosition.setXY(meanX/numParticles, meanY/numParticles);

        return currentPosition;
    }

}