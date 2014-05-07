package de.tarent.nic.android.base.position;

import de.tarent.nic.entities.NicGeoPoint;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * @author Désirée Amling <d.amling@tarent.de>
 */
public class HistoryTest {

    private History history;

    private double delta = 0.2;

    @Before
    public void setup() {
        history = new History(5);
    }

    @Test
    public void testThatAddRemovesTheOldestGeoPoint() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            final NicGeoPoint geoPoint = new NicGeoPointImpl(i, i);
            history.add(geoPoint);
        }

        final NicGeoPoint firstGeoPoint = history.getGeoPoints().get(0);

        final NicGeoPoint geoPoint = new NicGeoPointImpl(5, 5);
        history.add(geoPoint);

        assertFalse(history.getGeoPoints().contains(firstGeoPoint));
    }

    @Test
    public void testThatGetMedianPointReturnsTheCorrectNicGeoPoint() {
        addPointToHistory(5.0, 5.0);
        addPointToHistory(5.0, 5.0);
        addPointToHistory(5.0, 5.0);
        addPointToHistory(6.0, 6.0);
        addPointToHistory(6.0, 6.0);

        assertEquals(5.0, history.getMedianPoint().getX(), delta);
        assertEquals(5.0, history.getMedianPoint().getY(), delta);
    }

    @Test
    public void testThatGetAveragePointReturnsTheCorrectNicGeoPoint() {
        addPointToHistory(2.0, 2.0);
        addPointToHistory(2.0, 2.0);
        addPointToHistory(2.0, 2.0);
        addPointToHistory(6.0, 6.0);
        addPointToHistory(6.0, 6.0);

        assertEquals(3.6, history.getAveragePoint().getX(), delta);
        assertEquals(3.6, history.getAveragePoint().getY(), delta);
    }

    private void addPointToHistory(final double x, final double y) {
        final NicGeoPoint nicGeoPoint = new NicGeoPointImpl();
        nicGeoPoint.setXY(x, y);
        history.add(nicGeoPoint);
    }
}
