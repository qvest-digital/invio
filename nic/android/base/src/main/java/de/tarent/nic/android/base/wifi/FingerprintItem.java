package de.tarent.nic.android.base.wifi;

import de.tarent.nic.entities.Fingerprint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * The FingerprintItem is an OverlayItem that represents a wifi Fingerprint.
 */
public class FingerprintItem extends OverlayItem {

    private Fingerprint fingerprint;


    /**
     * Create a new FingerprintItem.
     *
     * @param fingerprint the fingerprint represented by this item.
     */
    public FingerprintItem(final Fingerprint fingerprint) {
        super("some id", "blabla<br>foo\nbar", (GeoPoint) fingerprint.getPoint());
        this.fingerprint = fingerprint;
    }


    public String getId() {
        return fingerprint.getId();
    }

    public Fingerprint getFingerprint() {
        return fingerprint;
    }
}
