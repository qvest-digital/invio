package de.tarent.nic.android.base.tileprovider;

import de.tarent.nic.android.base.tileprovider.AssetTileProvider;
import de.tarent.nic.android.base.tileprovider.NicTileSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AssetTileProviderTest {

    @Test
    public void testZoomLevels() {
        NicTileSource tileSource = mock(NicTileSource.class);
        when(tileSource.getMaximumZoomLevel()).thenReturn(10);
        when(tileSource.getMinimumZoomLevel()).thenReturn(0);

        // Only required by the super-constructor:
        IRegisterReceiver registerReceiver = mock(IRegisterReceiver.class);

        AssetTileProvider atp = new AssetTileProvider(registerReceiver, tileSource, null);

        assertEquals(atp.getMaximumZoomLevel(), 10);
        assertEquals(atp.getMinimumZoomLevel(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullTileSourceInConstructor() {
        // Only required by the super-constructor:
        IRegisterReceiver registerReceiver = mock(IRegisterReceiver.class);

        AssetTileProvider atp = new AssetTileProvider(registerReceiver, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWrongTileSource() {
        // Only required by the super-constructor:
        IRegisterReceiver registerReceiver = mock(IRegisterReceiver.class);

        NicTileSource goodTileSource = mock(NicTileSource.class);
        ITileSource badTileSource = mock(ITileSource.class);

        AssetTileProvider atp = new AssetTileProvider(registerReceiver, goodTileSource, null);
        atp.setTileSource(badTileSource);
    }

    @Test
    public void testSetNullTileSource() {
        NicTileSource tileSource = mock(NicTileSource.class);

        // Only required by the super-constructor:
        IRegisterReceiver registerReceiver = mock(IRegisterReceiver.class);

        AssetTileProvider atp = new AssetTileProvider(registerReceiver, tileSource, null);

        try {
            atp.setTileSource(null);
            fail("tileSource == null should throw an exception. We don't want NPE later on.");
        } catch (IllegalArgumentException ex) {
            // Fine!
        }
    }

    @Test
    public void testSetTileSource() {
        NicTileSource oldTileSource = mock(NicTileSource.class);
        when(oldTileSource.getMaximumZoomLevel()).thenReturn(7);

        NicTileSource newTileSource = mock(NicTileSource.class);
        when(newTileSource.getMaximumZoomLevel()).thenReturn(42);

        // Only required by the super-constructor:
        IRegisterReceiver registerReceiver = mock(IRegisterReceiver.class);

        AssetTileProvider atp = new AssetTileProvider(registerReceiver, oldTileSource, null);
        assertEquals(atp.getMaximumZoomLevel(), 7);

        atp.setTileSource(newTileSource);
        assertEquals(atp.getMaximumZoomLevel(), 42);
    }

}
