package de.tarent.nic.android.base.position;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * The UserPositionItem is an {@link org.osmdroid.views.overlay.OverlayItem} that represents the position of one user.
 */
public class UserPositionItem  extends OverlayItem {

    private GeoPoint geoPoint;

    /**
     * Construct a new UserPositionItem.
     * @param point The users position as a {@link org.osmdroid.util.GeoPoint}
     */
    public UserPositionItem(final GeoPoint point) {
        super("UserPositionItem", "An overlay item for the users position", point);
        geoPoint = point;
    }

    public final GeoPoint getGeoPoint() {
        return geoPoint;
    }
}
