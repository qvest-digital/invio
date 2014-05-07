package de.tarent.nic.tracker.geopoint;


/**
 * The XYPointFactory is a PointFactory which, surprisingly, creates XYPoints :-) It should be used in all places except
 * on android, where the {@link de.tarent.nic.android.base.position.NicGeoPointFactory} should be used.
 */
public class XYPointFactory extends PointFactory<XYPoint> {

    /**
     * {@inheritDoc}
     */
    @Override
    public XYPoint newPoint() {
        return new XYPoint();
    }

}
