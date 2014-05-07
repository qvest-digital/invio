package de.tarent.nic.android.base.tileprovider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTile;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Tests for {@link de.tarent.nic.android.base.tileprovider.NicTileSource}
 *
 * @author Sven Schumann, <s.schumann@tarent.de>
 */
@RunWith(RobolectricTestRunner.class)
public class NicTileSourceTest {
    final String tileName = "name";
    final int minZoom = 0;
    final int maxZoom = 6;
    final String fileEnding = ".png";

    NicTileSource toTest;

    @Before
    public void setup() {
        toTest = spy(new NicTileSource(
                tileName,
                ResourceProxy.string.unknown,
                minZoom,
                maxZoom,
                256,
                fileEnding));
    }

    @Test
    public void getTileRelativeFilenameString() {
        final MapTile tile = new MapTile(1, 13, 12);
        doReturn(tile.getY()).when(toTest).getYinTMSFormat(anyInt(), anyInt());

        final String result = toTest.getTileRelativeFilenameString(tile);

        assertEquals(
                tileName + "/" + tile.getZoomLevel() + "/" +
                        tile.getX() + "/" + toTest.getYinTMSFormat(tile.getZoomLevel(), tile.getY()) + fileEnding,

                result);
    }

    @Test
    public void getY_TMS() {
        assertEquals(0, toTest.getYinTMSFormat(0, 0));
        assertEquals(63, toTest.getYinTMSFormat(6, 0));
    }

}
