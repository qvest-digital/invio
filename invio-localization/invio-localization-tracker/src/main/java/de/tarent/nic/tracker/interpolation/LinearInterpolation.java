package de.tarent.nic.tracker.interpolation;

import de.tarent.nic.entities.NicGeoPoint;
import de.tarent.nic.tracker.geopoint.PointFactory;

import java.util.SortedSet;


/**
 * The LinearInterpolation takes all the neighbours, which are ordered by ascending distance, and assigns to each of
 * them a weight, that is the number of neighbours that are farther away. With these weights the target position is
 * interpolated. The individual distances are ignored, i.e. it doesn't matter how much farther off the second neighbour
 * is, compared to the first one. Only the order is important.
 * Example: if we have 4 neighbours, the nearest one will have the weight 4, the second  3, the third 2 and the
 *          last will have weight 1.
 * This is "linear" in the sense that the weights fall off linearly over the all neighbours.
 * The algorithm is simple and might be useful as a reference, to compare other algorithms with, but it is probably not
 * of any practical use in itself.
 * @param <T> the concrete type of points used in this interpolator.
 */
public class LinearInterpolation<T extends NicGeoPoint> implements PositionInterpolator<T> {

    private PointFactory<T> pointFactory;

    /**
     * Construct a new LinearInterpolation. Can/should be reused multiple times.
     *
     * @param factory is a the PointFactory which is to be used for the creation of new NicGeoPoints
     */
    public LinearInterpolation(final PointFactory<T> factory) {
        this.pointFactory = factory;
    }

    @Override
    public T interpolatePosition(final SortedSet<T> points) {

        final T result = pointFactory.newPoint();

        if (points.size() > 0) {
            int weight = points.size();

            // See "Gauss":
            final int normalizationFactor = (weight * (weight + 1)) / 2;

            float x = 0;
            float y = 0;

            for (NicGeoPoint point : points) {
                x += weight * point.getX();
                y += weight * point.getY();
                weight--;
            }

            x = x / normalizationFactor;
            y = y / normalizationFactor;

            result.setXY(x, y);
        }

        return result;
    }

}
