package de.tarent.nic.android.base.task;

import de.tarent.nic.android.base.mapmatching.WayManager;
import de.tarent.nic.mapserver.MapServerClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO: move all the parser-testing-stuff to a realy parser-unittest!
@RunWith(RobolectricTestRunner.class)
public class DownloadMapDataTaskTest {

    @Mock
    public MapServerClient mapServerClient;

    @Mock
    public WayManager wayManager;


    public String xml = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<osm version='0.6' upload='true' generator='JOSM'>\n" +
            "  <node id='-27' action='modify' visible='true' lat='0.016843411334633067' lon='0.008309416378105573'>\n" +
            "    <tag k='EAN-13' v='0000111122223' />\n" +
            "    <tag k='name' v='Rubbellos' />\n" +
            "    <tag k='price' v='100' />\n" +
            "  </node>\n" +
            "  <node id='-26' action='modify' visible='true' lat='0.010016215366909357' lon='-0.0025601985597406367'>\n" +
            "    <tag k='EAN-13' v='1234567890123' />\n" +
            "    <tag k='name' v='Brot' />\n" +
            "    <tag k='price' v='219' />\n" +
            "  </node>\n" +
            "  <node id='-18' action='modify' visible='true' lat='0.004985649820565272' lon='-0.0027398616165645404' />\n" +
            "  <node id='-20' action='modify' visible='true' lat='-1.347472926241651E-4' lon='-0.0034585138438601574' />\n" +
            "  <node id='-16' action='modify' visible='true' lat='0.012711161166014917' lon='-0.007141606508750195' />\n" +
            "  <node id='-15' action='modify' visible='true' lat='0.007860258711383942' lon='-0.01828071603183226' />\n" +
            "  <node id='-25' action='modify' visible='true' lat='0.02277229185287583' lon='-0.009207731662225094'>\n" +
            "    <tag k='EAN-13' v='4102610005744' />\n" +
            "    <tag k='name' v='Vulkanpark-Quelle Classic 0,7L' />\n" +
            "    <tag k='price' v='49' />\n" +
            "  </node>\n" +
            "  <node id='-24' action='modify' visible='true' lat='0.019538357050917367' lon='-0.01819088450342031'>\n" +
            "    <tag k='EAN-13' v='1234567890123' />\n" +
            "    <tag k='name' v='Brot' />\n" +
            "    <tag k='price' v='219' />\n" +
            "  </node>\n" +
            "  <node id='-13' action='modify' visible='true' lat='0.007141606490251521' lon='0.012531498213467326' />\n" +
            "  <node id='-11' action='modify' visible='true' lat='0.011992508955423628' lon='0.014417960310118319' />\n" +
            "  <node id='-9' action='modify' visible='true' lat='0.020257009234872305' lon='0.010285710003168521' />\n" +
            "  <node id='-6' action='modify' visible='true' lat='0.021065492938006643' lon='-0.005704302054158962' />\n" +
            "  <node id='-3' action='modify' visible='true' lat='0.015406106936998877' lon='-0.020616335770543018' />\n" +
            "  <way id='-17' action='modify' visible='true'>\n" +
            "    <nd ref='-15' />\n" +
            "    <nd ref='-16' />\n" +
            "    <nd ref='-18' />\n" +
            "    <nd ref='-20' />\n" +
            "    <tag k='indoor' v='Gang' />\n" +
            "  </way>\n" +
            "  <way id='-7' action='modify' visible='true'>\n" +
            "    <nd ref='-3' />\n" +
            "    <nd ref='-6' />\n" +
            "    <nd ref='-9' />\n" +
            "    <nd ref='-11' />\n" +
            "    <nd ref='-13' />\n" +
            "    <tag k='indoor' v='Gang' />\n" +
            "  </way>\n" +
            "</osm>\n";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mapServerClient.downloadMapData("test")).thenReturn(xml);
    }

    @Test
    // Here we just check if the correct number of objects (edges, products, productItems) are passed from the
    // background-task to the DownloadListeners.
    // The details of the objects are left to the OsmParserTest.
    public void testDoInBackground() throws IOException {
        OsmParser parser = mock(OsmParser.class);
        DownloadMapDataTask taskToTest = new DownloadMapDataTask(wayManager, mapServerClient, "test", parser);

        Map<String, Collection> results = new HashMap<String, Collection>();
        when(parser.getResults()).thenReturn(results);

        Map<String, Collection> result = taskToTest.doInBackground(null);
        // This will normally be called by the android system, because that's how AsyncTasks work:
        taskToTest.onPostExecute(result);

        // All DownloadTasks will receive all of the data. It is up to each of them what parts to use. Here we use the
        // wayManager only to test for all things.
        verify(wayManager).onDownloadFinished(eq(taskToTest), eq(true), eq(results));
        verify(parser, times(1)).parse(any(InputStream.class));
        /*
        Collection<Edge> edges = (Collection<Edge>)edgesCaptor.getValue().get(DownloadMapDataTask.EDGES);
        assertEquals(7, edges.size());
        Collection<Product> products = (Collection<Product>)edgesCaptor.getValue().get(DownloadMapDataTask.PRODUCTS);
        assertEquals(3, products.size());
        Collection<ProductItem> productItems = (Collection<ProductItem>)edgesCaptor.getValue().get(DownloadMapDataTask.PRODUCTITEMS);
        assertEquals(4, productItems.size());
        */
    }
}
