package de.tarent.nic.tracker.mapmatching;

import de.tarent.nic.entities.Edge;
import de.tarent.nic.entities.NicGeoPoint;
import de.tarent.nic.tracker.geopoint.PointFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * This simple algorithm snaps a {@link NicGeoPoint} onto the nearest edge.
 * If the edges don't change then the same instance should be used to snap all the points, to allow for performance
 * improvements later.
 */
public class SimpleWaySnap {

    /**
     * The edges represent all the valid ways, where the users position may lie.
     */
    private final Collection<Edge> edges;

    /**
     * In edgesByDistance we store a mapping of point-edge-distance to edge.
     * It is automatically sorted by distance, i.e. nearest edge first.
     * Still, it might be interesting to be able to look at several of the top edges, to determine the confidence in
     * the chosen nearest candidate.
     */
    private SortedMap<Float, Edge> edgesByDistance; // NOSONAR - No, this variable should not be local!

    /**
     * The nearestPoints store, for each edge, the point on this edge that was closest to the original point.
     */
    private Map<Edge, NicGeoPoint> nearestPoints;

    private Edge lastEdge;

    private PointFactory pointFactory;

    /**
     * Construct a new SimpleWaySnap.
     * // TODO, performance: do some precalculation on the edges and store it in some child-class, to speed up the math.
     *
     * @param factory is a the PointFactory which is to be used for the creation of new NicGeoPoints
     * @param edges the set of edges to which we will later want to snap our points.
     */
    public SimpleWaySnap(PointFactory factory, final Collection<Edge> edges) {
        this.pointFactory = factory;
        this.edges = edges;
    }


    /**
     * Snap the given point onto the nearest edge. If there are two edges with the same distance the point will snap
     * to the last saved edge (if that is one of those, otherwise it is undefined which edge is chosen).
     *
     * @param point the point which was measured and needs to be snapped to the valid path.
     * @return the nearest point that lies on one of the edges
     */
    public NicGeoPoint snap(NicGeoPoint point) {
        if (edges != null) {
            edgesByDistance = new TreeMap<Float, Edge>();
            nearestPoints = new HashMap<Edge, NicGeoPoint>();

            for (final Edge edge : edges) {
                final float distance = distance(edge, point);
                // If we already have an edge with the same distance we don't need this one, unless it is the lastEdge:
                if ((!edgesByDistance.containsKey(distance)) || (edge == lastEdge)) {
                    edgesByDistance.put(distance, edge);
                }
            }

            lastEdge = edgesByDistance.get(edgesByDistance.firstKey());
            point = nearestPoints.get(lastEdge);
        }
        return point;
    }

    // See: http://de.wikipedia.org/wiki/Geradengleichung#Parameterform
    //      http://www.ina-de-brabandt.de/vektoren/a/abstand-punkt-gerade-lfdpkt.html
    //      http://nibis.ni.schule.de/~lbs-gym/Vektorpdf/AbstandPunktGerade2.pdf
    // TODO: move precalculatable stuff to Edge.
    private float distance(final Edge edge, final NicGeoPoint point) { // NOSONAR this method-length is perfectly fine.
        final double ax = edge.getPointA().getLatitudeE6();
        final double ay = edge.getPointA().getLongitudeE6();
        final double bx = edge.getPointB().getLatitudeE6();
        final double by = edge.getPointB().getLongitudeE6();
        final double cx = point.getLatitudeE6();
        final double cy = point.getLongitudeE6();

        // L is the point on AB that is closest to C.
        double lx;
        double ly;

        // r is the parameter for the point L, if AB is given as "A+r*(B-A)".
        final double r = ((cx-ax)*(bx-ax) + (cy-ay)*(by-ay)) / ((bx-ax)*(bx-ax)+(by-ay)*(by-ay));

        // If L isn't on the line-segment then exchange it for A or B because we are not interested in other points
        // on our hypothetical line:
        if (r < 0) {
            lx = ax;
            ly = ay;
        } else if (r > 1) {
            lx = bx;
            ly = by;
        } else {
            lx = ax + r*(bx-ax);
            ly = ay + r*(by-ay);
        }

        final NicGeoPoint nearestPoint = pointFactory.newPoint();
        nearestPoint.setLatitudeE6((int)lx);
        nearestPoint.setLongitudeE6((int)ly);
        nearestPoints.put(edge, nearestPoint);

        // The distance between C and AB is the length of LC.
        // If we were only interested in the distance-ordering of the edges, and not the actual distances, then we
        // could omit the sqrt.
        final float distance = (float) Math.sqrt((lx-cx)*(lx-cx) + (ly-cy)*(ly-cy));

        return distance;
    }

}
