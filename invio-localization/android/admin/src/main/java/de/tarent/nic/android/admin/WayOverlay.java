package de.tarent.nic.android.admin;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.view.MotionEvent;
import de.tarent.nic.android.base.mapmatching.WayManager;
import de.tarent.nic.android.base.position.NicGeoPointImpl;
import de.tarent.nic.entities.Edge;
import de.tarent.nic.tracker.mapmatching.SimpleWaySnap;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.SafeDrawOverlay;
import org.osmdroid.views.safecanvas.ISafeCanvas;
import org.osmdroid.views.safecanvas.SafePaint;

import java.util.Collection;

/**
 * The WayOverlay shows the edges, representing the valid ways, let's the user click a point and then shows, how this
 * point could be snapped to the nearest edge.
 * It is mainly useful for demonstration/evaluation of mapmatching-strategies.
 */
public class WayOverlay extends SafeDrawOverlay {

    /**
     * The way manager containing edges which should be drawn
     */
    private final WayManager wayManager;

    /**
     * The point where the user clicked.
     */
    private IGeoPoint point = null;

    /**
     * The point to which the user will be snapped.
     */
    private IGeoPoint wayPoint = null;

    private SimpleWaySnap snapper;


    /**
     * Construct a new WayOverlay for a set of edges.
     *
     * @param ctx   the context which our parent-class needs.
     * @param wayManager containing edges which should be drawn
     */
    public WayOverlay(final Context ctx, final WayManager wayManager) {
        super(ctx);
        this.wayManager = wayManager;
    }

    public void setSnapper(SimpleWaySnap snapper) {
        this.snapper = snapper;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * When the user taps, we show him how the tapped point would be snapped to the edges.
     */
    @Override
    public boolean onSingleTapConfirmed(final MotionEvent e, final MapView mapView) {
        final MapView.Projection projection = mapView.getProjection();
        point = projection.fromPixels((int) e.getX(), (int) e.getY());

        if (snapper != null) {
            wayPoint = (IGeoPoint) snapper.snap(new NicGeoPointImpl(point));
        }
        mapView.postInvalidate();

        return true;
    }

    public IGeoPoint getOriginalPoint() {
        return point;
    }

    public IGeoPoint getSnappedPoint() {
        return wayPoint;
    }


    @Override
    protected void drawSafe(final ISafeCanvas canvas,  final MapView mapView, final boolean shadow) {
        if (!shadow) {
            final MapView.Projection projection = mapView.getProjection();
            drawEdges(canvas, projection);
            if ((point != null) && (wayPoint != null)) {
                drawWayPoint(canvas, projection);
            }
        }
    }

    // TODO, performance: don't draw all edges, only those inside the current boundingbox.
    // TODO, performance: don't even do the projection for all edges (and only once per node, please), i.e. the
    //                    clipping should already happen in world-space.

    /**
     * Draw the edges of our way.
     *
     * @param canvas     The current canvas where we are supposed to draw on.
     * @param projection the projection which we can use to transform world-coordinates to screen-coordinates.
     */
    private void drawEdges(final ISafeCanvas canvas, final MapView.Projection projection) {
        final SafePaint paint = new SafePaint();
        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth(5);

        Point p1;
        Point p2;

        final Collection<Edge> edges = wayManager.getEdges();
        if (edges != null) {
            for (Edge edge : edges) {
                p1 = projection.toMapPixels((IGeoPoint) edge.getPointA(), null);
                p2 = projection.toMapPixels((IGeoPoint) edge.getPointB(), null);
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
            }
        }
    }

    private void drawWayPoint(final ISafeCanvas canvas, final MapView.Projection projection) {
        final SafePaint paint = new SafePaint();

        final Point p1 = projection.toMapPixels(wayPoint, null);
        final Point p2 = projection.toMapPixels(point, null);

        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth(1.0f);
        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);

        paint.setStrokeWidth(3.0f);
        canvas.drawPoint(p1.x, p1.y, paint);

        paint.setColor(Color.RED);
        canvas.drawPoint(p2.x, p2.y, paint);
    }

}
