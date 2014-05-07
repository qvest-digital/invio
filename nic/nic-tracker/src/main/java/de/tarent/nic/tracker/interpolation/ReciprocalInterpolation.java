package de.tarent.nic.tracker.interpolation;

import de.tarent.nic.entities.NicGeoPoint;
import de.tarent.nic.tracker.geopoint.PointFactory;

import java.util.SortedSet;


/**
 * The ReciprocalInterpolation interpolates the position with weights which are the reciprocal values of the distances.
 * It thereby treats the distances as being 1:1 equivalent (i.e. proportional) to spatial distances. Depending on
 * the distance-metric that might or might not be a good assumption.
 * Example:
 * - given three distances:
 *   - d1 = 3
 *   - d2 = 6
 *   - d3 = 7
 * - then the weights of the corresponding points are:
 *   - w1 = 1/3
 *   - w2 = 1/6
 *   - w3 = 1/7
 * That means, if the divergence to P1 is only half the divergence to P2 then the weight of P1 is double that of P2.
 * If the distance-metric were the real spatial distance then this algorithm would be trivialized to "if a point is
 * twice as far away then it is only half as near [compared to some other point]" ;-)
 * The generic parameter T is the type of points with which we will work. Needs to be the same for input and output.
 * @param <T> the concrete type of points used in this interpolator.
 */
public class ReciprocalInterpolation<T extends NicGeoPoint> implements PositionInterpolator<T> {

    private PointFactory<T> pointFactory;

    /**
     * Construct a new ReciprocalInterpolation. Can/should be reused multiple times.
     *
     * @param factory is a the PointFactory which is to be used for the creation of new NicGeoPoints
     */
    public ReciprocalInterpolation(final PointFactory<T> factory) {
        this.pointFactory = factory;
    }

    @Override
    public T interpolatePosition(final SortedSet<T> points) {

        if (points.size() == 0) {
            return pointFactory.newPoint();
        }

        float normalizationFactor = 0;

        float x = 0;
        float y = 0;

        for (final T point : points) {

            // If a point has a divergence of 0.0, then we assume that the users position equals the position of this
            // specific Fingerprint and we position the User exactly there.
            if (point.getDivergence() == 0.0) {
                return point;
            }

            final double weight = 1 / point.getDivergence();
            normalizationFactor += weight;

            x += weight * point.getX();
            y += weight * point.getY();
        }

        x = x / normalizationFactor;
        y = y / normalizationFactor;

        return getResultingPoint(x, y);
    }

    private T getResultingPoint(final float x, final float y) {
        final T result = pointFactory.newPoint();
        result.setXY(x, y);

        return result;
    }

}
