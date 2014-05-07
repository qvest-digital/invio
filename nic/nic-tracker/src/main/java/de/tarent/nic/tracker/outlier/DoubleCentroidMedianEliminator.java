package de.tarent.nic.tracker.outlier;

import de.tarent.nic.tracker.geopoint.PointFactory;

/**
 * The DoubleCentroidMedianEliminator eliminates all NicGeoPoint's further than the double median
 * distance from the centroid.
 * The generic parameter T is the type of points with which we will work. Needs to be the same for input and output.
 */
public class DoubleCentroidMedianEliminator extends CentroidMedianEliminator implements OutlierEliminator {

    /**
     * Constructor creating an {@link CentroidMedianEliminator} with a direct factor of 2
     * for threshold distance calculation.
     *
     * @param factory is a the PointFactory which is to be used for the creation of new NicGeoPoints
     */
    public DoubleCentroidMedianEliminator(PointFactory factory) {
        super(factory, 2);
    }
}
