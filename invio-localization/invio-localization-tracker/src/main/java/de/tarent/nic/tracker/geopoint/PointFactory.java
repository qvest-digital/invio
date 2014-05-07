package de.tarent.nic.tracker.geopoint;

import de.tarent.nic.entities.NicGeoPoint;

/**
 * A PointFactory can be used to create new instances of NicGeoPoint. Concrete subclasses can decide which
 * implementation they want to produce. Pass the factory of your choice to a class that needs to create points for
 * you but which should not know about the different NicGeoPoints.
 * This is the solution to the problem that NicGeoPointImpl must extend org.osmdroid.util.GeoPoint, in order to be
 * usable with all of osmdroid, but that we want to keep android-code like that out of the tracker.
 * @param <T> the concrete type that the factory will instantiate.
 */
public abstract class PointFactory<T extends NicGeoPoint> {

    /**
     * Make a new point, with default coordinates (could be (0,0) but don't depend on that!).
     * @return a new instance of an implementation of NicGeoPoint
     */
    public abstract T newPoint();

}
