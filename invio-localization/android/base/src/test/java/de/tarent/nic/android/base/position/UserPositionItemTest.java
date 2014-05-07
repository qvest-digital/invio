package de.tarent.nic.android.base.position;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.util.GeoPoint;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@Config(manifest = "../base/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class UserPositionItemTest {

    @Test
    public void testGetGeoPoint() throws Exception {
        GeoPoint point = new GeoPoint(1, 2);
        UserPositionItem item = new UserPositionItem(point);

        assertEquals(point, item.getGeoPoint());
    }

}
