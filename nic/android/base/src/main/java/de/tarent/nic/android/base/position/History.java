package de.tarent.nic.android.base.position;

import de.tarent.nic.entities.NicGeoPoint;
import de.tarent.nic.tracker.geopoint.PointList;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains a sliding-window for the {@link NicGeoPoint}s so that we can keep multiple {@link NicGeoPoint}s up to a
 * configurable number. Once that number is reached, the eldest {@link NicGeoPoint} is removed.
 *
 * @author Désirée Amling <d.amling@tarent.de>
 */
public class History {

    /**
     * The maximum number of {@link NicGeoPoint}s that the History should hold.
     */
    private final int maxGeoPointHistory;

    /**
     * The list of the {@link NicGeoPoint}s that will act as our sliding-window.
     */
    private final List<NicGeoPoint> geoPoints;

    /**
     * Simple constructor that instantiates the {@link #geoPoints} as a {@link LinkedList}.
     *
     * @param maxGeoPointHistory the given {@link #maxGeoPointHistory}
     */
    public History(final int maxGeoPointHistory) {
        this.maxGeoPointHistory = maxGeoPointHistory;
        geoPoints = new LinkedList<NicGeoPoint>();
    }

    /**
     * Adds a {@link NicGeoPoint} to the {@link #geoPoints} history. If the length of the {@link #geoPoints}
     * reaches {@link #maxGeoPointHistory}, then the oldest {@link NicGeoPoint} will be removed.
     *
     * @param geoPoint the {@link NicGeoPoint} to add to the list of {@link #geoPoints}
     */
    public void add(final NicGeoPoint geoPoint) {
        if (geoPoints.size() == maxGeoPointHistory) {
            removeOldestGeoPoint();
        }
        geoPoints.add(geoPoint);
    }

    /**
     * Gets the median {@link NicGeoPoint} from the list of {@link #geoPoints}.
     *
     * @return the median {@link NicGeoPoint}
     */
    public NicGeoPoint getMedianPoint() {
        final PointList pointList = new PointList(new NicGeoPointFactory(), geoPoints);

        return pointList.getMedianPoint();
    }

    /**
     * Gets the average {@link NicGeoPoint} from the list of {@link #geoPoints}.
     *
     * @return the average {@link NicGeoPoint}
     */
    public NicGeoPoint getAveragePoint() {
        final PointList pointList = new PointList(new NicGeoPointFactory(), geoPoints);

        return pointList.getAveragePoint();
    }

    private void removeOldestGeoPoint() {
        geoPoints.remove(0);
    }

    public List<NicGeoPoint> getGeoPoints() {
        return geoPoints;
    }
}
