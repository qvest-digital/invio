package de.tarent.nic.tracker.outlier;

import de.tarent.nic.entities.NicGeoPoint;
import de.tarent.nic.tracker.geopoint.PointFactory;
import de.tarent.nic.tracker.geopoint.PointList;

import java.util.Iterator;
import java.util.Set;

/**
 * The CentroidMedianEliminator eliminates all {@link de.tarent.nic.entities.NicGeoPoint}s further than the calculated
 * threshold distance with the given direct factor from the centroid.
 * The generic parameter T is the type of points with which we will work. Needs to be the same for input and output.
 * @param <T> the concrete type of points used in this eliminator.
 */
public class CentroidMedianEliminator<T extends NicGeoPoint> implements OutlierEliminator<T> {

    private double medianThresholdFactor;

    private PointFactory<T> pointFactory;


    /**
     * Constructor creating an {@link CentroidMedianEliminator} with the given direct factor
     * for threshold distance calculation.
     *
     * @param factory is a the PointFactory which is to be used for the creation of new NicGeoPoints
     * @param medianThresholdFactor the direct factor for threshold distance calculation
     */
    public CentroidMedianEliminator(PointFactory factory, final double medianThresholdFactor) {
        this.pointFactory = factory;
        this.medianThresholdFactor = medianThresholdFactor;
    }

    @Override
    public Set<T> removeOutliers(Set<T> candidates) {

        final PointList<T> candidateList = new PointList<T>(pointFactory, candidates);

        // Eliminator not applicable on two or less candidates.
        if (candidateList.size() <= 2) {
            return candidates;
        }

        // 1. Get the centroid.
        // The centroid is the virtual center coordinate from a given amount of coordinates. It is composed of the
        // independent median coordinates for each dimension.
        final T centroid = candidateList.getMedianPoint();

        // 2. Calculate the median of the distances between each candidate and the centroid.
        final double medianCentroidDistance = candidateList.calculateMedianReferencePointDistance(centroid);

        // 3. Eliminate all coordinates further then the threshold distance.
        final double thresholdDistance = medianThresholdFactor * medianCentroidDistance;

        final Iterator<T> it = candidates.iterator();
        while (it.hasNext()) {
            final NicGeoPoint point = it.next();
            // TODO: we should not calculate all the distances a second time. Things  like sqrt might be slow. Instead,
            //       the outlierHelper could supply us with his list of distances in some way.
            if (point.calculateDistanceTo(centroid) > thresholdDistance) {
                it.remove();
            }
        }

        return candidates;
    }
}
