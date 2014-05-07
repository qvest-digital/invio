package de.tarent.nic.android.base.position;

import android.app.Activity;
import de.tarent.nic.entities.NicGeoPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JUnit4 Test for the {@link UserPositionManager}.
 */
@Config(manifest = "../base/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class UserPositionManagerTest {

    private Activity activity;
    private MapView mapView;
    private UserPositionItem item;
    private UserPositionManager userPositionManager;

    @Before
    public void setup() {
        activity = mock(Activity.class);
        mapView = mock(MapView.class);
        item = mock(UserPositionItem.class);
        when(activity.getResources()).thenReturn(Robolectric.application.getResources());

        userPositionManager = new UserPositionManager(activity, mapView, item);
    }


    @Test
    public void testConstruction() {
        assertEquals(0, userPositionManager.getNeighbours().size());
        assertEquals(0, userPositionManager.getOutliers().size());
    }

    @Test
    public void testOutlierSet() {
        NicGeoPoint p1 = new NicGeoPointImpl(0, 0);
        NicGeoPoint p2 = new NicGeoPointImpl(1, 1);
        NicGeoPoint p3 = new NicGeoPointImpl(2, 2);
        p3.setDivergence(1);
        NicGeoPoint p4 = new NicGeoPointImpl(3, 3);

        SortedSet<NicGeoPoint> neighboursWithOutliers = new TreeSet<NicGeoPoint>();
        neighboursWithOutliers.add(p1);
        neighboursWithOutliers.add(p2);
        neighboursWithOutliers.add(p3);
        neighboursWithOutliers.add(p4);

        int h1 = p1.hashCode();
        int h2 = p2.hashCode();
        int h3 = p3.hashCode();
        int h4 = p4.hashCode();

        SortedSet<NicGeoPoint> neighbours = new TreeSet<NicGeoPoint>();
        neighbours.add(p2);
        neighbours.add(p3);

        userPositionManager.setNeighboursWithOutliers(neighboursWithOutliers);
        userPositionManager.setNeighbours(neighbours);

        SortedSet<NicGeoPoint> outliers = userPositionManager.getOutliers();
        assertEquals(2, outliers.size());
        assertTrue(outliers.contains(p1));
        assertTrue(outliers.contains(p4));
    }

    @Test
    public void testThatUpdateItemAddsItemToTheOverlay() {
        ItemizedIconOverlay<UserPositionItem> overlay = spy(userPositionManager.overlay);
        userPositionManager.overlay = overlay;

        List<UserPositionItem> items = new ArrayList<UserPositionItem>();
        items.add(item);
        userPositionManager.updateItems(items);

        verify(overlay, times(1)).removeAllItems();
        verify(overlay, times(1)).addItems(any(List.class)); // TODO: really check the contents!
    }

    @Test
    public void testThatDisablePositionRemovesAllItemsFromTheOverlay() {
        ItemizedIconOverlay<UserPositionItem> overlay = spy(userPositionManager.overlay);
        userPositionManager.overlay = overlay;

        userPositionManager.disablePosition();

        verify(overlay, times(1)).removeAllItems();
    }
}
