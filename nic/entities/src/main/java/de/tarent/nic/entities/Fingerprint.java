package de.tarent.nic.entities;


/**
 * A Fingerprint is a wifi Histogram at a specific position.
 */
public class Fingerprint {

    /**
     * The histogram that was measured at some point.
     */
    private Histogram histogram;

    /**
     * The point where the histogram was measured.
     */
    private NicGeoPoint point;

    /**
     * An ID, for display, ordering, etc.
     */
    private String id;

    /**
     * Constructor.
     *
     * @param histogram the {@link Histogram}
     * @param geoPoint the {@link NicGeoPoint}
     */
    public Fingerprint(final Histogram histogram, final NicGeoPoint geoPoint) {
        this.histogram = histogram;
        this.point = geoPoint;
        this.id = histogram.getId();
    }

    public Histogram getHistogram() {
        return histogram;
    }

    public NicGeoPoint getPoint() {
        return point;
    }

    public String getId() {
        return id;
    }


}
