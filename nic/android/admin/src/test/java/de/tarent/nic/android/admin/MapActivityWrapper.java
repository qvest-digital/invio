package de.tarent.nic.android.admin;

import de.tarent.nic.android.base.wifi.UserLocator;

/**
 * We need this class because we want to mock and spy the UserLocator, but we cannot set it because it's in the
 * AbstractMapActivity and we haven't access to it from our package.
 * Also it's NOT an inner class inside the MapActivityTest because of the strange robolectirc/fest Exception
 * saying "constructor not found with parameters: []", but it works this way.
 */
public class MapActivityWrapper extends MapActivity {
    public void setUserLocator(final UserLocator userLocator){
        this.userLocator = userLocator;
    }
}