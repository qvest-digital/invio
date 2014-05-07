package de.tarent.nic.tracker.interpolation;

import de.tarent.nic.entities.NicGeoPoint;
import de.tarent.nic.tracker.geopoint.PointFactory;

import java.util.SortedSet;

/**
 * The MetricInterpolation takes the distances, as calculated by the chosen distance-algorithm, into account.
 * The smaller the distance to a fingerprint, the larger the interpolation-weight of its coordinates.
 * For reasons unknown it works like this:
 * - given three distances:
 *   - d1 = 1
 *   - d2 = 1
 *   - d3 = 4
 * - calculate the distanceSum = 1 + 1 + 4 = 6
 * - calculate the totalDifferenceSum of the differences that each distance has to the distanceSum:
 *   - (6-1) + (6-1) + (6-4) = 5 + 5 + 2 = 12
 *   - that is actually the same as: distanceSum * (size(d)-1)
 * - the weight for each point is: the difference between its own distance and the distanceSum, divided by the
 *   totalDifferenceSum:
 *   - w1 = (6-1) / 12 = 5/12
 *   - w2 = (6-1) / 12 = 5/12
 *   - w3 = (6-4) / 12 = 2/12
 * - since these weights add up to 1, no normalization is required after we have multiplied all coordinates with
 *   their weights.
 * Note that 1 point is a special case that cannot be interpolated like this (would result in 0,0).
 * @param <T> the concrete type of points used in this interpolator.
 */
public class MetricInterpolation<T extends NicGeoPoint> implements PositionInterpolator<T> {

    private PointFactory<T> pointFactory;

    /**
     * Construct a new MetricInterpolation. Can/should be reused multiple times.
     *
     * @param factory is a the PointFactory which is to be used for the creation of new NicGeoPoints
     */
    public MetricInterpolation(final PointFactory<T> factory) {
        this.pointFactory = factory;
    }

    @Override
    public T interpolatePosition(final SortedSet<T> points) {

        T result =  pointFactory.newPoint();

        // If we have fewer than 2 points we don't need to do any real interpolation work:
        final int size = points.size();
        switch (size) {
            case 0: break;
            case 1: final NicGeoPoint singlePoint = points.first();
                    result.setXY(singlePoint.getX(), singlePoint.getY());
                    break;
            default: result = doInterpolatePosition(points);
        }

        return result;
    }


    private T doInterpolatePosition(final SortedSet<T> points) {

        final T result = pointFactory.newPoint();

        // This is the sum over the divergences of all the neighbours.
        float distanceSum = 0;
        for (NicGeoPoint point : points) {
            distanceSum += point.getDivergence();
        }

        // This is the sum over the differences between each distance and the totalDistanceSum.
        final float totalDifferenceSum = distanceSum * (points.size() - 1);

        float x = 0;
        float y = 0;

        for (NicGeoPoint point : points) {
            final double weight = (distanceSum - point.getDivergence()) / totalDifferenceSum;

            x += weight * point.getX();
            y += weight * point.getY();
        }

        result.setXY(x, y);

        return result;
    }

}
