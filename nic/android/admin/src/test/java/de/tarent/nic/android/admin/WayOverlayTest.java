package de.tarent.nic.android.admin;

import android.app.Activity;
import android.view.MotionEvent;
import de.tarent.nic.android.base.mapmatching.WayManager;
import de.tarent.nic.android.base.position.NicGeoPointImpl;
import de.tarent.nic.entities.NicGeoPoint;
import de.tarent.nic.tracker.mapmatching.SimpleWaySnap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.safecanvas.ISafeCanvas;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


@RunWith(RobolectricTestRunner.class)
public class WayOverlayTest {

    Activity activity;

    MapView mapView;

    SimpleWaySnap snapper;

    MapView.Projection projection;

    IGeoPoint point;

    @Before
    public void setup() {
        activity = mock(Activity.class);
        when(activity.getResources()).thenReturn(Robolectric.application.getResources());

        point = new NicGeoPointImpl();
        projection = mock(MapView.Projection.class);
        when(projection.fromPixels(anyInt(), anyInt())).thenReturn(point);

        mapView = mock(MapView.class);
        when(mapView.getProjection()).thenReturn(projection);

        snapper = mock(SimpleWaySnap.class);

    }

    @Test
    public void testNullContent() {
        WayOverlay wayOverlay = new WayOverlay(activity,  null);
        wayOverlay.setSnapper(null);

        // The overlay should handle taps even if it is empty:
        assertTrue(wayOverlay.onSingleTapConfirmed(mock(MotionEvent.class), mapView));
        // And it should calculate the point where the user clicked:
        assertEquals(point, wayOverlay.getOriginalPoint());
        // But not the snapped wayPoint (no edges + no snapper):
        assertNull(wayOverlay.getSnappedPoint());
    }

    @Test
    public void testNullEdges() {
        WayOverlay wayOverlay = new WayOverlay(activity,  null);
        wayOverlay.setSnapper(snapper);

        assertTrue(wayOverlay.onSingleTapConfirmed(mock(MotionEvent.class), mapView));
        // It should snap (that the snapper doesn't do anything without edges is not the problem of the WayOverlay)
        // TODO: it might be problematic that both have their own edges...
        verify(snapper).snap(any(NicGeoPoint.class));
        // It should also trigger a redraw:
        verify(mapView).postInvalidate();
    }

    @Test
    public void testDontDrawShadows() {
        WayOverlay wayOverlay = new WayOverlay(activity,  null);
        wayOverlay.setSnapper(snapper);

        wayOverlay.drawSafe(null, mapView, true);
        verifyZeroInteractions(mapView);
    }

    @Test
    public void testDrawWithoutEdges() {
        WayManager manager = mock(WayManager.class);
        WayOverlay wayOverlay = new WayOverlay(activity,  manager);
        wayOverlay.setSnapper(snapper);
        wayOverlay.drawSafe(mock(ISafeCanvas.class), mapView, false);
        // TODO: verify stuff. "No crash" is not enough!
    }


}
