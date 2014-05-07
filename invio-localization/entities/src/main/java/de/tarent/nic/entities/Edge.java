package de.tarent.nic.entities;

/**
 * An edge represents a straight line between two {@link NicGeoPoint}'s points.
 *
 * @author Atanas Alexandrov, tarent solutions GmbH
 */
public class Edge {

    private final NicGeoPoint pointA;
    private final NicGeoPoint pointB;

    /**
     * Create an Edge defined by the given points A and B.
     *
     * @param pointA the startingpoint of the edge
     * @param pointB the endpoint of the edge
     */
    public Edge(final NicGeoPoint pointA, final NicGeoPoint pointB) {
        this.pointA = pointA;
        this.pointB = pointB;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        final Edge edge = (Edge) o;
        if ((pointA != null) ? (!pointA.equals(edge.pointA)) : (edge.pointA != null)) {
            return false;
        }
        if ((pointB != null) ? (!pointB.equals(edge.pointB)) : (edge.pointB != null)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = (pointA != null) ? pointA.hashCode() : 0;
        result = 31 * result + ((pointB != null) ? pointB.hashCode() : 0);
        return result;
    }


    public NicGeoPoint getPointA() {
        return pointA;
    }

    public NicGeoPoint getPointB() {
        return pointB;
    }

}