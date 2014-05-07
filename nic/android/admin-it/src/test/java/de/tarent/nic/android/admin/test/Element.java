package de.tarent.nic.android.admin.test;

import de.tarent.nic.android.admin.R;

/**
 * Ein {@link Element} ist ein Objekt, was in der Android-
 * Application sichtbar ist. (Ähnlich zu einem HTML-Tag)
 * <p/>
 * Alles wird in Android via ID identifiziert. Eine
 * View hat eine Id, ein String hat eine Id und ein
 * Layout hat eine Id! Jedes "Ding" was wir in den Tests
 * anfassen wollen, ist ein {@link Element}! Es beinhaltet
 * die Id (was für wichtig ist für den Zugriff) und ein
 * Namen. Dieser Name dient der Lesbarkeit! Er wird in den
 * Test für entsprechende Meldungen verwendet.
 *
 * @author Sven Schumann, <s.schumann@tarent.de>
 */
public enum Element {

    MAP_VIEW(R.id.osmmapview, "MapView (OSMDroid)");

    private final int resourceId;
    private final String name;

    private Element(int resourceId, String name) {
        this.resourceId = resourceId;
        this.name = name;
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getName() {
        return name;
    }
}
