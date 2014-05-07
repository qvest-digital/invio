package de.tarent.nic.android.base.task;

import de.tarent.nic.android.base.position.NicGeoPointImpl;
import de.tarent.nic.entities.Edge;
import de.tarent.nic.entities.NicGeoPoint;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


/**
 * This test uses the validMapData.osm file which can be found in test/resources. It contains valid osm data such as
 * ways and our map metadata (north angle etc.).
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class OsmParserTest {
    InputStream input;
    OsmParser parser;

    public String wrongMetadataXML = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<osm version='0.6' upload='true' generator='JOSM'>\n" +
            "  <node id='-27' action='modify' visible='true' lat='0.016843411334633067' lon='0.008309416378105573'>\n" +
            "    <tag k='indoor' v='metadata' />\n" +
            "    <tag k='indoor_scale' v='0,a4' />\n" +
            "    <tag k='north_angle' v='3,6,60' />\n" +
            "  </node>\n" +
            "</osm>\n";

    @Before
    public void setUp() throws Exception {

        //Parse the valid osm file with ways and map meta data
        URL url = this.getClass().getResource("/validMapData.osm");
        input = new FileInputStream(url.getFile());
        parser = new NicOsmParser();
    }

    @After
    public void tearDown() throws Exception {
        input.close();
    }

    @Test
    public void testThatNorthAngleIsParsedProperly() throws Exception {
        parser.parse(input);

        //North angle from the testMapData.osm
        int expectedNorthAngle = 60;

        Map<String, Collection> resultMap = parser.getResults();
        List<Integer> northAngleList = (List<Integer>) resultMap.get(OsmParserKeys.NORTH_ANGLE);
        assertTrue(expectedNorthAngle == northAngleList.get(0));
    }

    @Test
    public void testThatIndoorMapScaleIsParsedProperly() throws Exception {
        parser.parse(input);

        //Scale from the testMapData.osm
        float expectedScale = 0.4f;

        Map<String, Collection> resultMap = parser.getResults();
        List<Float> scaleList = (List<Float>) resultMap.get(OsmParserKeys.INDOOR_SCALE);
        assertTrue(expectedScale == scaleList.get(0));
    }

    @Test
    public void testThatEdgesAreParsedProperly() throws Exception {
        parser.parse(input);

        // We need to parse the number-strings here (as in our tested code) because not all of them are valid floats.
        // It would be unfair to treat inherent rounding mechanisms as test failures.
        Map<String, NicGeoPoint> expectedPoints = new HashMap<String, NicGeoPoint>();
        expectedPoints.put("-794", new NicGeoPointImpl(Float.parseFloat("51.25567034941198"), Float.parseFloat("6.757231408520558")));
        expectedPoints.put("-792", new NicGeoPointImpl(Float.parseFloat("51.25498038781923"), Float.parseFloat("6.756741436298513")));
        expectedPoints.put("-791", new NicGeoPointImpl(Float.parseFloat("51.25547869445262"), Float.parseFloat("6.755883984909925")));
        expectedPoints.put("-10", new NicGeoPointImpl(Float.parseFloat("51.25417504040519"), Float.parseFloat("6.748673467159792")));
        expectedPoints.put("-8", new NicGeoPointImpl(Float.parseFloat("51.25400181445516"), Float.parseFloat("6.748673467159792")));
        expectedPoints.put("-6", new NicGeoPointImpl(Float.parseFloat("51.25400823024272"), Float.parseFloat("6.748909240339734")));
        expectedPoints.put("-4", new NicGeoPointImpl(Float.parseFloat("51.25418787193108"), Float.parseFloat("6.748909240339734")));

        Map<String, Collection> resultMap = parser.getResults();
        List<Edge> edges = (List<Edge>) resultMap.get(OsmParserKeys.EDGES);

        assertEquals(7, edges.size());

        //Test the way id=-793
        assertEquals(expectedPoints.get("-791"), edges.get(0).getPointA());
        assertEquals(expectedPoints.get("-792"), edges.get(0).getPointB());
        assertEquals(expectedPoints.get("-792"), edges.get(1).getPointA());
        assertEquals(expectedPoints.get("-794"), edges.get(1).getPointB());
        assertEquals(expectedPoints.get("-794"), edges.get(2).getPointA());
        assertEquals(expectedPoints.get("-791"), edges.get(2).getPointB());

        //Test the way id=-394
        assertEquals(expectedPoints.get("-10"), edges.get(3).getPointA());
        assertEquals(expectedPoints.get("-8"), edges.get(3).getPointB());
        assertEquals(expectedPoints.get("-8"), edges.get(4).getPointA());
        assertEquals(expectedPoints.get("-6"), edges.get(4).getPointB());
        assertEquals(expectedPoints.get("-6"), edges.get(5).getPointA());
        assertEquals(expectedPoints.get("-4"), edges.get(5).getPointB());
        assertEquals(expectedPoints.get("-4"), edges.get(6).getPointA());
        assertEquals(expectedPoints.get("-10"), edges.get(6).getPointB());

    }

    @Test
    public void testThatNorthAngleAndScaleAreNullWhenTheyParsedInWrongFormat() throws IOException {
        InputStream wrongXMLInput = new ByteArrayInputStream(wrongMetadataXML.getBytes("UTF-8"));
        parser.parse(wrongXMLInput);

        Map<String, Collection> resultMap = parser.getResults();
        List<Integer> northAngleList = (List<Integer>) resultMap.get(OsmParserKeys.NORTH_ANGLE);
        assertNull(northAngleList.get(0));

        List<Float> scaleList = (List<Float>) resultMap.get(OsmParserKeys.INDOOR_SCALE);
        assertNull(scaleList.get(0));
    }

}
