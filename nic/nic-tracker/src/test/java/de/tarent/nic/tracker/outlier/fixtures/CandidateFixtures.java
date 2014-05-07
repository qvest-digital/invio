package de.tarent.nic.tracker.outlier.fixtures;

import de.tarent.nic.entities.NicGeoPoint;
import de.tarent.nic.tracker.geopoint.XYPoint;

import java.util.HashSet;
import java.util.Set;

public class CandidateFixtures {

    public static Set<NicGeoPoint> createCandidatesTooSmallAmount() {
        final Set<NicGeoPoint> geoPointsIn = new HashSet<NicGeoPoint>();
        geoPointsIn.add(makeGeoPoint(5, 4));
        geoPointsIn.add(makeGeoPoint(16, 15));
        return geoPointsIn;
    }

    public static Set<NicGeoPoint> createCandidatesTooSmallAmountExpected() {
        final Set<NicGeoPoint> expected = new HashSet<NicGeoPoint>();
        expected.add(makeGeoPoint(5, 4));
        expected.add(makeGeoPoint(16, 15));
        return expected;
    }

    public static Set<NicGeoPoint> createCandidatesWithAnObviousOutlier() {
        final Set<NicGeoPoint> geoPointsIn = new HashSet<NicGeoPoint>();
        geoPointsIn.add(makeGeoPoint(5, 4));
        geoPointsIn.add(makeGeoPoint(7, 6));
        geoPointsIn.add(makeGeoPoint(9, 3));
        geoPointsIn.add(makeGeoPoint(16, 15));
        return geoPointsIn;
    }

    public static Set<NicGeoPoint> createCandidatesWithAnObviousOutlierExpected() {
        final Set<NicGeoPoint> expected = new HashSet<NicGeoPoint>();
        expected.add(makeGeoPoint(5, 4));
        expected.add(makeGeoPoint(7, 6));
        expected.add(makeGeoPoint(9, 3));
        return expected;
    }


    public static Set<NicGeoPoint> createCandidatesWithAnObviousOutlierInTheMiddleOfTheMap() {
        final Set<NicGeoPoint> geoPointsIn = new HashSet<NicGeoPoint>();
        geoPointsIn.add(makeGeoPoint(5, 4));
        geoPointsIn.add(makeGeoPoint(9, 3));
        geoPointsIn.add(makeGeoPoint(16, 15));
        geoPointsIn.add(makeGeoPoint(7, 6));
        return geoPointsIn;
    }

    public static Set<NicGeoPoint> createCandidatesWithAnObviousOutlierInTheMiddleOfTheMapExpected() {
        final Set<NicGeoPoint> expected = new HashSet<NicGeoPoint>();
        expected.add(makeGeoPoint(5, 4));
        expected.add(makeGeoPoint(9, 3));
        expected.add(makeGeoPoint(7, 6));
        return expected;
    }


    public static Set<NicGeoPoint> createCandidatesPlacedInASquare() {
        final Set<NicGeoPoint> geoPointsIn = new HashSet<NicGeoPoint>();
        geoPointsIn.add(makeGeoPoint(0, 0));
        geoPointsIn.add(makeGeoPoint(0, 2));
        geoPointsIn.add(makeGeoPoint(2, 0));
        geoPointsIn.add(makeGeoPoint(2, 2));
        return geoPointsIn;
    }

    public static Set<NicGeoPoint> createCandidatesPlacedInASquareExpected() {
        final Set<NicGeoPoint> expected = new HashSet<NicGeoPoint>();
        expected.add(makeGeoPoint(0, 0));
        expected.add(makeGeoPoint(0, 2));
        expected.add(makeGeoPoint(2, 0));
        expected.add(makeGeoPoint(2, 2));
        return expected;
    }


    public static Set<NicGeoPoint> createCandidatesPlacedInASquareAndOnePlacedInTheMiddle() {
        final Set<NicGeoPoint> geoPointsIn = new HashSet<NicGeoPoint>();
        geoPointsIn.add(makeGeoPoint(0, 0));
        geoPointsIn.add(makeGeoPoint(0, 2));
        geoPointsIn.add(makeGeoPoint(2, 0));
        geoPointsIn.add(makeGeoPoint(2, 2));
        geoPointsIn.add(makeGeoPoint(1, 1));
        return geoPointsIn;
    }

    public static Set<NicGeoPoint> createCandidatesPlacedInASquareAndOnePlacedInTheMiddleExpected() {
        final Set<NicGeoPoint> expected = new HashSet<NicGeoPoint>();
        expected.add(makeGeoPoint(0, 0));
        expected.add(makeGeoPoint(0, 2));
        expected.add(makeGeoPoint(2, 0));
        expected.add(makeGeoPoint(2, 2));
        expected.add(makeGeoPoint(1, 1));
        return expected;
    }


    public static Set<NicGeoPoint> createCandidatesPlacedInASquareAndOnePlacedFarAway() {
        final Set<NicGeoPoint> geoPointsIn = new HashSet<NicGeoPoint>();
        geoPointsIn.add(makeGeoPoint(0, 0));
        geoPointsIn.add(makeGeoPoint(0, 2));
        geoPointsIn.add(makeGeoPoint(2, 0));
        geoPointsIn.add(makeGeoPoint(2, 2));
        geoPointsIn.add(makeGeoPoint(15, 15));
        return geoPointsIn;
    }

    public static Set<NicGeoPoint> createCandidatesPlacedInASquareAndOnePlacedFarAwayExpected() {
        final Set<NicGeoPoint> expected = new HashSet<NicGeoPoint>();
        expected.add(makeGeoPoint(0, 0));
        expected.add(makeGeoPoint(0, 2));
        expected.add(makeGeoPoint(2, 0));
        expected.add(makeGeoPoint(2, 2));
        return expected;
    }


    public static Set<NicGeoPoint> createCandidatesRasterCoordinates() {
        final Set<NicGeoPoint> geoPointsIn = new HashSet<NicGeoPoint>();
        geoPointsIn.add(makeGeoPoint(2, 2));
        geoPointsIn.add(makeGeoPoint(2, 4));
        geoPointsIn.add(makeGeoPoint(4, 2));
        geoPointsIn.add(makeGeoPoint(4, 4));
        geoPointsIn.add(makeGeoPoint(6, 2));
        geoPointsIn.add(makeGeoPoint(6, 4));
        geoPointsIn.add(makeGeoPoint(6, 6));
        geoPointsIn.add(makeGeoPoint(2, 6));
        geoPointsIn.add(makeGeoPoint(4, 6));
        geoPointsIn.add(makeGeoPoint(8, 8));
        return geoPointsIn;
    }

    public static Set<NicGeoPoint> createCandidatesRasterCoordinatesExpected() {
        final Set<NicGeoPoint> expected = new HashSet<NicGeoPoint>();
        expected.add(makeGeoPoint(2, 2));
        expected.add(makeGeoPoint(2, 4));
        expected.add(makeGeoPoint(4, 2));
        expected.add(makeGeoPoint(4, 4));
        expected.add(makeGeoPoint(6, 2));
        expected.add(makeGeoPoint(6, 4));
        expected.add(makeGeoPoint(6, 6));
        expected.add(makeGeoPoint(2, 6));
        expected.add(makeGeoPoint(4, 6));
        return expected;
    }


    public static Set<NicGeoPoint> createCandidatesTest() {
        final Set<NicGeoPoint> geoPointsIn = new HashSet<NicGeoPoint>();
        geoPointsIn.add(makeGeoPoint(11, 2));
        geoPointsIn.add(makeGeoPoint(14, 6));
        geoPointsIn.add(makeGeoPoint(17, 2));
        geoPointsIn.add(makeGeoPoint(14, 3));
        geoPointsIn.add(makeGeoPoint(14, 10));
        return geoPointsIn;
    }

    public static Set<NicGeoPoint> createCandidatesTestExpected() {
        final Set<NicGeoPoint> expected = new HashSet<NicGeoPoint>();
        expected.add(makeGeoPoint(11, 2));
        expected.add(makeGeoPoint(14, 6));
        expected.add(makeGeoPoint(17, 2));
        expected.add(makeGeoPoint(14, 3));
        return expected;
    }


    public static Set<NicGeoPoint> createCandidatesTwoPointsFarAwayFromAnotherTwoPoints() {
        final Set<NicGeoPoint> geoPointsIn = new HashSet<NicGeoPoint>();
        geoPointsIn.add(makeGeoPoint(2, 1));
        geoPointsIn.add(makeGeoPoint(4, 1));
        geoPointsIn.add(makeGeoPoint(6, 15));
        geoPointsIn.add(makeGeoPoint(8, 15));
        return geoPointsIn;
    }

    public static Set<NicGeoPoint> createCandidatesTwoPointsFarAwayFromAnotherTwoPointsExpected() {
        final Set<NicGeoPoint> expected = new HashSet<NicGeoPoint>();
        expected.add(makeGeoPoint(2, 1));
        expected.add(makeGeoPoint(4, 1));
        expected.add(makeGeoPoint(6, 15));
        expected.add(makeGeoPoint(8, 15));
        return expected;
    }


    public static Set<NicGeoPoint> createCandidatesTwoPointsFarAwayFromThreePointsNearToEachOther() {
        final Set<NicGeoPoint> geoPointsIn = new HashSet<NicGeoPoint>();
        geoPointsIn.add(makeGeoPoint(10, 1));
        geoPointsIn.add(makeGeoPoint(11, 1));
        geoPointsIn.add(makeGeoPoint(11, 2));
        geoPointsIn.add(makeGeoPoint(1, 15));
        geoPointsIn.add(makeGeoPoint(20, 20));
        return geoPointsIn;
    }

    public static Set<NicGeoPoint> createCandidatesTwoPointsFarAwayFromThreePointsNearToEachOtherExpected() {
        final Set<NicGeoPoint> expected = new HashSet<NicGeoPoint>();
        expected.add(makeGeoPoint(10, 1));
        expected.add(makeGeoPoint(11, 1));
        expected.add(makeGeoPoint(11, 2));
        return expected;
    }


    public static Set<NicGeoPoint> createCandidatesOutliersFromARealMap() {
        final Set<NicGeoPoint> geoPointsIn = new HashSet<NicGeoPoint>();
        geoPointsIn.add(new XYPoint(50722124, 7061466));
        geoPointsIn.add(new XYPoint(50722124, 7061587));
        geoPointsIn.add(new XYPoint(50722033, 7061589));
        geoPointsIn.add(new XYPoint(50722032, 7061460));
        geoPointsIn.add(new XYPoint(50722087, 7061526));
        geoPointsIn.add(new XYPoint(50721550, 7061636));
        geoPointsIn.add(new XYPoint(50721494, 7061554));
        return geoPointsIn;
    }

    public static Set<NicGeoPoint> createCandidatesOutliersFromARealMapExpected() {
        final Set<NicGeoPoint> expected = new HashSet<NicGeoPoint>();
        expected.add(new XYPoint(50722124, 7061466));
        expected.add(new XYPoint(50722124, 7061587));
        expected.add(new XYPoint(50722033, 7061589));
        expected.add(new XYPoint(50722032, 7061460));
        expected.add(new XYPoint(50722087, 7061526));
        return expected;
    }


    private static NicGeoPoint makeGeoPoint(final double x, final double y) {
        final NicGeoPoint nicGeoPoint = new XYPoint(0, 0);
        nicGeoPoint.setXY(x, y);

        return nicGeoPoint;
    }
}
