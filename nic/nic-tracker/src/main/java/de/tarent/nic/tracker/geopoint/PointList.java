package de.tarent.nic.tracker.geopoint;

import de.tarent.nic.entities.NicGeoPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The PointList stores NicGeoPoints and offers some convenience functions for them.
 *
 * The generic parameter T is the type of points with which we will work. Needs to be the same for input and output.
 * @param <T> the concrete type of points in this list.
 */
public class PointList<T extends NicGeoPoint> extends ArrayList<T> {

    /**
     * This is a cached list of all the x coordinates.
     * It has to be reset to null when the contents of this list changes.
     */
    private List<Double> xValues = null;

    /**
     * This is a cached list of all the y coordinates.
     * It has to be reset to null when the contents of this list changes.
     */
    private List<Double> yValues = null;

    private PointFactory<T> pointFactory;


    /**
     * Constructor for an empty PointList.
     *
     * @param factory is a the PointFactory which is to be used for the creation of new NicGeoPoints
     */
    public PointList(PointFactory factory) {
        super();
        this.pointFactory = factory;
    }

    /**
     * Constructor for a PointList pre-filled with points from some other collection.
     *
     * @param factory is a the PointFactory which is to be used for the creation of new NicGeoPoints
     * @param geoPoints the {@link Collection} of {@link NicGeoPoint}s
     */
    public PointList(PointFactory factory, final Collection<T> geoPoints) {
        super(geoPoints);
        this.pointFactory = factory;
    }

    /**
     * Calculate the median of the distances between each list element and a given reference point.
     *
     * @param referencePoint the reference point to calculate the distance to
     * @return the median distance to the reference point as double value
     */
    public double calculateMedianReferencePointDistance(final T referencePoint) {

        final List<Double> referencePointDistances = new ArrayList<Double>();

        // for each element get the distance to the reference point
        for (final T point : this) {
            referencePointDistances.add(point.calculateDistanceTo(referencePoint));
        }

        Collections.sort(referencePointDistances);

        // the median reference point distance is the median of the distances between
        // each element and the reference point
        final double medianReferencePointDistance = getMedianFromSortedList(referencePointDistances);

        return medianReferencePointDistance;
    }


    /**
     * Find a point whose coordinates are the medians of the respective coordinates of all points in the list.
     * This will usually not be one of the points that exist in the list but have coordinates from different points.
     *
     * @return the median point, aka centroid
     */
    public T getMedianPoint() {
        final double x = getMedianX();
        final double y = getMedianY();

        final T geoPoint = pointFactory.newPoint();
        geoPoint.setXY(x, y);

        return geoPoint;
    }

    /**
     * Find a point which coordinates are the average of the respective coordinates of all points in the list.
     *
     * @return the average point
     */
    public T getAveragePoint() {
        final double x = getAverageX();
        final double y = getAverageY();

        final T geoPoint = pointFactory.newPoint();
        geoPoint.setXY(x, y);

        return geoPoint;

    }

    /**
     * Returns the average value of the {@link #xValues}.
     *
     * @return the average value of the {@link #xValues}
     */
    public double getAverageX() {
        return getAverage(getXValues());
    }

    /**
     * Returns the average value of the {@link #yValues}.
     *
     * @return the average value of the {@link #yValues}
     */
    public double getAverageY() {
        return getAverage(getYValues());
    }

    /**
     * Returns the median of the {@link #xValues}.
     *
     * @return the median of the {@link #xValues}
     */
    public double getMedianX() {
        return getMedianFromSortedList(getXValues());
    }

    /**
     * Returns the median of the {@link #yValues}.
     *
     * @return the median of the {@link #yValues}
     */
    public double getMedianY() {
        return getMedianFromSortedList(getYValues());
    }

    /**
     * Gets a pre-sorted list of the {@link #xValues}.
     *
     * @return a sorted list of {@link #xValues}
     */
    public List<Double> getXValues() {
        if (xValues == null) {
            setXYValues();
        }

        return xValues;
    }

    /**
     * Gets a pre-sorted list of the {@link #yValues}.
     *
     * @return a sorted list of {@link #yValues}
     */
    public List<Double> getYValues() {
        if (yValues == null) {
            setXYValues();
        }

        return yValues;
    }

    //--------------------------------------------------------------------------------
    // We override all superclass-methods that change the contents of our list in order
    // to mark it as dirty again.
    //--------------------------------------------------------------------------------
    @Override
    public void clear() {
        clearXYValues();
        super.clear();
    }

    @Override
    public boolean add(final T geoPoint) {
        clearXYValues();
        return super.add(geoPoint);
    }

    @Override
    public void add(int index, T element) {
        clearXYValues();
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        clearXYValues();
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        clearXYValues();
        return super.addAll(index, c);
    }

    @Override
    public T remove(int index) {
        clearXYValues();
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        clearXYValues();
        return super.remove(o);
    }

    @Override
    public T set(int index, T element) {
        clearXYValues();
        return super.set(index, element);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        clearXYValues();
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        clearXYValues();
        return super.retainAll(c);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        clearXYValues();
        super.removeRange(fromIndex, toIndex);
    }
    //--------------------------------------------------------------------------------


    /**
     * Find the median in a sorted list of doubles.
     *
     * @param list the list, which must be sorted
     * @return the median value
     */
    private static double getMedianFromSortedList(final List<Double> list) {
        // The size, integer-divided by 2, is the:
        // a) single center element index in an odd list size
        // b) upper center element index for an even list size
        final int middleIndex = list.size() / 2;

        double median;

        if (list.size() % 2 == 1) {
            // odd list size
            median = list.get(middleIndex);
        } else {
            // even list size
            // TODO: wäre nicht besser, wenn wir es durch 2 dividieren?
            median = 0.5f * (list.get(middleIndex - 1) + list.get(middleIndex));
        }
        return median;
    }

    /**
     * Find the average in a list of doubles.
     *
     * @param list the list of values to calculate an average value
     * @return the average value
     */
    private static double getAverage(final List<Double> list) {
        double sum = 0;
        for(double d : list) {
            sum = sum + d;
        }

        final double result = sum / list.size();

        return result;
    }


    private void setXYValues() {
        xValues = new ArrayList<Double>();
        yValues = new ArrayList<Double>();

        for (final NicGeoPoint geoPoint : this) {
            xValues.add(geoPoint.getX());
            yValues.add(geoPoint.getY());
        }

        Collections.sort(xValues);
        Collections.sort(yValues);
    }

    private void clearXYValues() {
        xValues = null;
        yValues = null;
    }
}
