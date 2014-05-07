package de.tarent.nic.android.base.position;

import de.tarent.nic.tracker.geopoint.PointFactory;

/**
 * The NicGeoPointFactory is a PointFactory which creates instances of NicGeoPointImpl. It is the right factory to use
 * on android, where the points need to be compatible with osmdroid. In other places (e.g. on the server), where we
 * don't want android-dependencies, the {@link de.tarent.nic.tracker.geopoint.XYPointFactory} might be better.
 */
public class NicGeoPointFactory extends PointFactory<NicGeoPointImpl> {

    /**
     * {@inheritDoc}
     */
    @Override
    public NicGeoPointImpl newPoint() {
        return new NicGeoPointImpl();
    }

}
