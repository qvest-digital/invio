package de.tarent.nic.android.base.position;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import de.tarent.nic.android.base.R;
import de.tarent.nic.android.base.particlefilter.Particle;
import de.tarent.nic.entities.NicGeoPoint;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.safecanvas.ISafeCanvas;
import org.osmdroid.views.safecanvas.SafePaint;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The UserPositionManager holds the {@link UserPositionItem} and
 * {@link org.osmdroid.views.overlay.ItemizedIconOverlay} for the purpose of displaying them.
 */
public class UserPositionManager {

    /**
     * The overlay displays the users position and some additional diagnostic/debug info.
     */
    protected ItemizedIconOverlay<UserPositionItem> overlay;

    private Activity activity;

    private MapView mapView;

    private SortedSet<NicGeoPoint> neighbours = new TreeSet<NicGeoPoint>();

    private SortedSet<NicGeoPoint> outliers = new TreeSet<NicGeoPoint>();

    private SortedSet<NicGeoPoint> neighboursWithOutliers;

    private List<Particle> particles = new ArrayList<Particle>();


    /**
     * Constructs a new UserPositionManager.
     *
     * @param activity the parent {@link android.app.Activity}, from which we need the
     *  {@link android.content.Context} and {@link android.app.FragmentManager}
     * @param mapView the {@link org.osmdroid.views.MapView}
     */
    public UserPositionManager(final Activity activity, final MapView mapView) {
        this.activity = activity;
        this.mapView = mapView;
        final Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), R.drawable.user_position_icon);
        final Drawable icon = new BitmapDrawable(activity.getResources(), bm);
        overlay = new UserPositionOverlay(icon);
        mapView.getOverlays().add(overlay);
    }


    /**
     * Constructs a new UserPositionManager.
     *
     * @param activity the parent {@link android.app.Activity}, from which we need the
     *  {@link android.content.Context} and {@link android.app.FragmentManager}
     * @param mapView the {@link org.osmdroid.views.MapView}
     * @param userPositionItem the {@link UserPositionItem} to be placed on the overlay.
     */
    public UserPositionManager(final Activity activity, final MapView mapView,
                               final UserPositionItem userPositionItem) {
        this(activity, mapView);
        overlay.addItem(userPositionItem);
    }


    /**
     * Set the actual neighbours and calculate the outliers, which are the neighboursWithOutliers minus the neighbours.
     *
     * @param neighbours the neighbouring points; the first one is special because it will be this one which has the
     *                   neighbour-lines attached to it.
     */
    public void setNeighbours(final SortedSet<NicGeoPoint> neighbours) {
        this.neighbours = neighbours;
        if ( neighbours != null ) {
            outliers = new TreeSet<NicGeoPoint>(neighboursWithOutliers);
            outliers.removeAll(neighbours);
        }
    }

    public void setNeighboursWithOutliers(final SortedSet<NicGeoPoint> neighboursWithOutliers) {
        this.neighboursWithOutliers = neighboursWithOutliers;
    }

    public void setParticles(List<Particle> particles) {
        this.particles = new ArrayList(particles);
    }

    /**
     * Exchange the current UserPositionItems with a new ones. The first one will be treated as a special one, because
     * it is this which will have the lines to its fingerprint neighbours. Therefore you should use the first item as
     * the wifi-only-item.
     *
     * @param userPositionItems the new positions
     */
    public void updateItems(final List<UserPositionItem> userPositionItems) {
        overlay.removeAllItems();
        overlay.addItems(userPositionItems);
        mapView.postInvalidate();
    }

    /**
     * This method removes the position-icon, i.e. makes the whole layer invisible.
     */
    public void disablePosition() {
        overlay.removeAllItems();
        mapView.postInvalidate();
    }

    public SortedSet<NicGeoPoint> getNeighbours() {
        return neighbours;
    }

    public SortedSet<NicGeoPoint> getOutliers() {
        return outliers;
    }

    /**
     * Gets center of bounding box. We use the "scrollable area" for this, not what the MapView calls bounding box,
     * because we set the scrollable area ourselves, to the correct value, whereas the bounding box is calculated
     * in some strange way that we do not understand.
     * 
     * @return the center of the bounding box
     */
    public NicGeoPoint getBoundingBoxCenter() {
        final BoundingBoxE6 boundingBox = mapView.getScrollableAreaLimit();
        NicGeoPoint center = null;
        if (boundingBox != null) {
            final IGeoPoint originalCenter = boundingBox.getCenter();
            center = new NicGeoPointImpl(originalCenter);
        }
        return center;
    }

    /**
     * The UserPositionOverlay displays the user's position and some additional diagnostic/debug info.
     * TODO: the ItemizedIconOverlay is probably not the best type of overlay for this usecase. The userposition is not
     *       an immutable item and it doesn't need to be clicked, etc. A custom Overlay might be more fitting.
     */
    public class UserPositionOverlay extends ItemizedIconOverlay<UserPositionItem> {

        /**
         * Construct a new UserPositionOverlay with the specific icon for the user.
         *
         * @param icon some icon (e.g. BitmapDrawable)
         */
        public UserPositionOverlay(final Drawable icon) {
            super(new ArrayList<UserPositionItem>(), icon, null, new DefaultResourceProxyImpl(activity));
        }

        @Override
        protected void drawSafe(final ISafeCanvas canvas, final MapView mapView, final boolean shadow) {
            if (size() > 0) {
                if (shadow) {
                    if (activity.getClass().getCanonicalName().equals("de.tarent.nic.android.admin.MapActivity")) {
                        drawLinesBetweenUserPositionAndNeighbours(Color.BLUE, neighbours, canvas);
                        drawLinesBetweenUserPositionAndNeighbours(Color.RED, outliers, canvas);
                        drawParticles(Color.RED, canvas);
                    }
                } else {
                    super.drawSafe(canvas, mapView, shadow);
                }
            }

        }


        private void drawLinesBetweenUserPositionAndNeighbours(final int color, final SortedSet<NicGeoPoint> neighbours,
                                                               final ISafeCanvas canvas) {
            final MapView.Projection projection = mapView.getProjection();
            final Point userPoint = projection.toMapPixels(getItem(0).getGeoPoint(), null);
            final SafePaint sp = new SafePaint();
            sp.setColor(color);
            sp.setStrokeWidth(2);

            if (neighbours != null && neighbours.size() > 0) {
                for (NicGeoPoint neighbour : neighbours) {
                    final Point fingerprintPoint = projection.toMapPixels((IGeoPoint)neighbour, null);
                    canvas.drawLine(userPoint.x, userPoint.y, fingerprintPoint.x, fingerprintPoint.y, sp);
                }
            }
        }

        private void drawParticles(final int color, final ISafeCanvas canvas) {
            final MapView.Projection projection = mapView.getProjection();
            final SafePaint sp = new SafePaint();
            sp.setColor(color);
            sp.setStrokeWidth(4);

            if (particles != null && particles.size() > 0) {
                for (Particle particle : particles) {
                    final NicGeoPoint geoParticle = new NicGeoPointImpl();
                    geoParticle.setXY(particle.getX(), particle.getY());
                    final Point particlePoint = projection.toMapPixels((IGeoPoint)geoParticle, null);
                    canvas.drawPoint(particlePoint.x, particlePoint.y, sp);
                }
            }
        }
    }

}
