package de.tarent.nic.tracker.outlier;

import de.tarent.nic.entities.NicGeoPoint;

import java.util.Set;

/**
 * An {@link OutlierEliminator} is used to eliminate unrealistic measurements from an amount of data.
 * <p/>
 * Generally all implementations eliminate elements not corresponding to the majority coordinate localization.
 * The outliers might be fingerprints at the other end of the building which, by chance, look similar to
 * fingerprints near our actual position. But they are irrelevant and should not distort our interpolation
 * between the true neighbours.
 * The generic parameter T is the type of points with which we will work. Needs to be the same for input and output.
 * @param <T> the concrete type of points used in the eliminator.
 */
public interface OutlierEliminator<T extends NicGeoPoint> {

    /**
     * Eliminates possible outliers from the given set of coordinates.
     *
     * @param candidates the set of neighbouring points
     * @return reduced Map without outliers
     */
    Set<T> removeOutliers(final Set<T> candidates);

}
