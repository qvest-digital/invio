package de.tarent.nic.tracker.interpolation;

import de.tarent.nic.entities.NicGeoPoint;

import java.util.SortedSet;

/**
 * A PositionInterpolator is used to interpolate, in space, between several points for which we only now the "distance"
 * in some non-spatial divergence-metric. Because the distance-algorithms need not produce distances that are
 * proportional to the real-space-distance it is not a priori clear, which kind of interpolation produces the best
 * results.
 * The algorithms will usually work by summing up the weighted coordinate-vectors of all neighbours and then
 * renormalizing the end result.
 * @param <T> the concrete type of points used in the interpolator.
 */
public interface PositionInterpolator<T extends NicGeoPoint>  {

    /**
     * Interpolate a new position from a set of positions, with given distances in some abstract distance-metric.
     *
     * @param points the set of points, sorted by their divergence: nearest first, farthest last.
     *               If this set is empty then the result is undefined (either null or a point at 0,0, or whatever).
     * @return the NicGeoPoint that most closely matches the given distances, according to the concrete interpolation-
     *         algorithm.
     */
    T interpolatePosition(SortedSet<T> points);

}
