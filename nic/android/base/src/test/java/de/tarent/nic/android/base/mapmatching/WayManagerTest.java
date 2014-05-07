package de.tarent.nic.android.base.mapmatching;

import de.tarent.nic.android.base.task.DownloadMapDataTask;
import de.tarent.nic.android.base.task.DownloadTask;
import de.tarent.nic.android.base.task.OsmParserKeys;
import de.tarent.nic.entities.Edge;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class WayManagerTest {

    @Test
    public void testThatSuccessfulOnDownloadFinishedCallsSetEdges() {
        final WayManager wayManager = new WayManager();
        final WayManager wayManagerSpy = spy(wayManager);

        final DownloadTask task = mock(DownloadTask.class);
        final boolean success = true;
        final Collection<Edge> edges = mock(Set.class);
        final Map<String, Collection> data = new HashMap<String, Collection>();
        data.put(OsmParserKeys.EDGES, edges);

        wayManagerSpy.onDownloadFinished(task, success, data);

        verify(wayManagerSpy, times(1)).setEdges(edges);
    }

    @Test
    public void testThatUnsuccessfulOnDownloadFinishedDoesNotCallSetEdges() {
        final WayManager wayManager = new WayManager();
        final WayManager wayManagerSpy = spy(wayManager);

        final DownloadTask task = mock(DownloadTask.class);
        final boolean success = false;
        final Collection<Edge> edges = mock(Set.class);
        final Map<String, Collection> data = new HashMap<String, Collection>();
        data.put(OsmParserKeys.EDGES, edges);

        wayManagerSpy.onDownloadFinished(task, success, data);

        verify(wayManagerSpy, times(0)).setEdges(edges);
    }

}
