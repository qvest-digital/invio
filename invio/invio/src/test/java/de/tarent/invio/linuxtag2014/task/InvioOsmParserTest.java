package de.tarent.invio.linuxtag2014.task;

import de.tarent.invio.linuxtag2014.products.Product;
import de.tarent.invio.linuxtag2014.products.ProductItem;
import de.tarent.nic.android.base.position.NicGeoPointImpl;
import de.tarent.nic.android.base.task.OsmParser;
import de.tarent.nic.android.base.task.OsmParserKeys;
import de.tarent.nic.entities.Edge;
import de.tarent.nic.entities.NicGeoPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class InvioOsmParserTest {

    OsmParser parser;

    @Before
    public void setUp() {
        parser = new InvioOsmParser(null);
    }

    // In this test the xml contains only ways and no products.
    @Test
    public void testParseEdges1() throws IOException {
        Map<String, NicGeoPoint> expectedPoints = new HashMap<String, NicGeoPoint>();
        // We need to parse the number-strings here (as in our tested code) because not all of them are valid floats.
        // It would be unfair to treat inherent rounding mechanisms as test failures.
        expectedPoints.put("35", new NicGeoPointImpl(Float.parseFloat("1.532453456344354323"),
                Float.parseFloat("20.452435234346563454")));
        expectedPoints.put("34", new NicGeoPointImpl(Float.parseFloat("0.002245788209717387"),
                Float.parseFloat("-0.011902677514583659")));
        expectedPoints.put("32", new NicGeoPointImpl(Float.parseFloat("-0.015091696598705377"),
                Float.parseFloat("0.030407972367445798")));
        expectedPoints.put("30", new NicGeoPointImpl(Float.parseFloat("-0.037459744679101376"),
                Float.parseFloat("-0.003817899575079665")));
        expectedPoints.put("29", new NicGeoPointImpl(Float.parseFloat("-0.022907039134800522"),
                Float.parseFloat("-0.023585776208137443")));

        URL url = this.getClass().getResource("/waysOnly.osm");
        InputStream input = new FileInputStream(url.getFile());
        parser.parse(input);
        List<Edge> edges = (List<Edge>) parser.getResults().get(OsmParserKeys.EDGES);

        assertEquals(4, edges.size());
        assertEquals(expectedPoints.get("29"), edges.get(0).getPointA());
        assertEquals(expectedPoints.get("30"), edges.get(0).getPointB());
        assertEquals(expectedPoints.get("30"), edges.get(1).getPointA());
        assertEquals(expectedPoints.get("34"), edges.get(1).getPointB());
        assertEquals(expectedPoints.get("34"), edges.get(2).getPointA());
        assertEquals(expectedPoints.get("35"), edges.get(2).getPointB());
        assertEquals(expectedPoints.get("30"), edges.get(3).getPointA());
        assertEquals(expectedPoints.get("32"), edges.get(3).getPointB());
    }

    // In this test the xml contains ways and products which should not interfere with the ways.
    @Test
    public void testParseEdges2() throws IOException {
        Map<String, NicGeoPoint> expectedPoints = new HashMap<String, NicGeoPoint>();
        // We need to parse the number-strings here (as in our tested code) because not all of them are valid floats.
        // It would be unfair to treat inherent rounding mechanisms as test failures.
        expectedPoints.put("20", new NicGeoPointImpl(Float.parseFloat("-1.347472926241651E-4"), Float.parseFloat("-0.0034585138438601574")));
        expectedPoints.put("18", new NicGeoPointImpl(Float.parseFloat("0.004985649820565272"), Float.parseFloat("-0.0027398616165645404")));
        expectedPoints.put("16", new NicGeoPointImpl(Float.parseFloat("0.012711161166014917"), Float.parseFloat("-0.007141606508750195")));
        expectedPoints.put("15", new NicGeoPointImpl(Float.parseFloat("0.007860258711383942"), Float.parseFloat("-0.01828071603183226")));
        expectedPoints.put("13", new NicGeoPointImpl(Float.parseFloat("0.007141606490251521"), Float.parseFloat("0.012531498213467326")));
        expectedPoints.put("11", new NicGeoPointImpl(Float.parseFloat("0.011992508955423628"), Float.parseFloat("0.014417960310118319")));
        expectedPoints.put("9", new NicGeoPointImpl(Float.parseFloat("0.020257009234872305"), Float.parseFloat("0.010285710003168521")));
        expectedPoints.put("6", new NicGeoPointImpl(Float.parseFloat("0.021065492938006643"), Float.parseFloat("-0.005704302054158962")));
        expectedPoints.put("3", new NicGeoPointImpl(Float.parseFloat("0.015406106936998877"), Float.parseFloat("-0.020616335770543018")));

        URL url = this.getClass().getResource("/waysAndProducts.osm");
        InputStream input = new FileInputStream(url.getFile());
        parser.parse(input);
        List<Edge> edges = (List<Edge>) parser.getResults().get(OsmParserKeys.EDGES);

        assertEquals(7, edges.size());
        assertEquals(expectedPoints.get("15"), edges.get(0).getPointA());
        assertEquals(expectedPoints.get("16"), edges.get(0).getPointB());
        assertEquals(expectedPoints.get("16"), edges.get(1).getPointA());
        assertEquals(expectedPoints.get("18"), edges.get(1).getPointB());
        assertEquals(expectedPoints.get("18"), edges.get(2).getPointA());
        assertEquals(expectedPoints.get("20"), edges.get(2).getPointB());
        assertEquals(expectedPoints.get("3"), edges.get(3).getPointA());
        assertEquals(expectedPoints.get("6"), edges.get(3).getPointB());
        assertEquals(expectedPoints.get("6"), edges.get(4).getPointA());
        assertEquals(expectedPoints.get("9"), edges.get(4).getPointB());
        assertEquals(expectedPoints.get("9"), edges.get(5).getPointA());
        assertEquals(expectedPoints.get("11"), edges.get(5).getPointB());
        assertEquals(expectedPoints.get("11"), edges.get(6).getPointA());
        assertEquals(expectedPoints.get("13"), edges.get(6).getPointB());
    }

    @Test
    public void testParseProducts() throws IOException {
        Set<Product> expectedProducts = new HashSet<Product>();
        expectedProducts.add(new Product(4102610005744L, "Vulkanpark-Quelle Classic 0,7L", 49));
        expectedProducts.add(new Product(111122223L, "Rubbellos", 100));
        expectedProducts.add(new Product(1234567890123L, "Brot", 219));

        URL url = this.getClass().getResource("/waysAndProductsAndMetadata.osm");
        InputStream input = new FileInputStream(url.getFile());
        parser.parse(input);
        Collection<Product> products = parser.getResults().get(InvioOsmParserKeys.PRODUCTS);

        assertEquals(expectedProducts.size(), products.size());
        assertTrue(products.containsAll(expectedProducts));
    }

    @Test
    public void testParseProductsReplacesApostrophes() throws IOException {
        final String apostropheProduct = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<osm version='0.6' upload='true' generator='JOSM'>\n" +
                "  <node id='-27' action='modify' visible='true' lat='0.016843411334633067' lon='0.008309416378105573'>\n" +
                "    <tag k='EAN-13' v='4102610005744' />\n" +
                "    <tag k='name' v='McDonald&apos;s' />\n" +
                "    <tag k='price' v='100' />\n" +
                "  </node>\n" +
                "</osm>\n";

        Set<Product> expectedProducts = new HashSet<Product>();
        expectedProducts.add(new Product(4102610005744L, "McDonald's", 100));

        InputStream xmlStream = new ByteArrayInputStream(apostropheProduct.getBytes("UTF-8"));
        parser.parse(xmlStream);
        Collection<Product> products = parser.getResults().get(InvioOsmParserKeys.PRODUCTS);

        final String expectedName = expectedProducts.iterator().next().getName();
        final String actualName = products.iterator().next().getName();

        assertEquals(expectedProducts.size(), products.size());
        assertTrue(products.containsAll(expectedProducts));
        assertEquals(expectedName, actualName);
    }

    @Test
    public void testParseProductItems() throws IOException {

        List<ProductItem> expectedItems = new ArrayList<ProductItem>();
        expectedItems.add(new ProductItem(new Product(4102610005744L, "Vulkanpark-Quelle Classic 0,7L", 49),
                new NicGeoPointImpl(0.02277229185287583, -0.009207731662225094)));
        expectedItems.add(new ProductItem(new Product(111122223L, "Rubbellos", 100),
                new NicGeoPointImpl(0.016843411334633067, 0.008309416378105573)));
        expectedItems.add(new ProductItem(new Product(1234567890123L, "Brot", 219),
                new NicGeoPointImpl(0.010016215366909357, -0.0025601985597406367)));
        expectedItems.add(new ProductItem(new Product(1234567890123L, "Brot", 219),
                new NicGeoPointImpl(0.019538357050917367, -0.01819088450342031)));

        URL url = this.getClass().getResource("/waysAndProducts.osm");
        InputStream input = new FileInputStream(url.getFile());
        parser.parse(input);
        Collection<ProductItem> productItems = parser.getResults().get(InvioOsmParserKeys.PRODUCTITEMS);

        for (ProductItem item : productItems) {
            assertTrue(expectedItems.remove(item));
        }
        assertTrue(expectedItems.isEmpty());
    }

    @Test
    public void testAngleAndScale() throws IOException {
        URL url = this.getClass().getResource("/waysAndProductsAndMetadata.osm");
        InputStream input = new FileInputStream(url.getFile());
        parser.parse(input);
        Collection<Float> scale = parser.getResults().get(InvioOsmParserKeys.INDOOR_SCALE);
        Collection<Integer> angle = parser.getResults().get(InvioOsmParserKeys.NORTH_ANGLE);

        assertEquals(1, scale.size());
        assertEquals(2.55f, scale.toArray()[0]);

        assertEquals(1, angle.size());
        assertEquals(-90, angle.toArray()[0]);
    }

    @Test
    public void testNamespace() throws IOException {
        URL url = this.getClass().getResource("/waysAndProductsAndMetadata.osm");
        InputStream input = new FileInputStream(url.getFile());
        parser.parse(input);
        Collection<Map<String, String>> namespace = parser.getResults().get(InvioOsmParserKeys.NAMESPACE);

        assertEquals(1, namespace.size());

        Map<String, String> namespaceMap = (Map<String, String>)namespace.toArray()[0];
        assertEquals(3, namespaceMap.size());
        assertEquals("tarent AG", namespaceMap.get("namespace_group_name"));
        assertEquals("1. Obergeschoss", namespaceMap.get("namespace_map_name"));
        assertEquals("1", namespaceMap.get("namespace_short_name"));
    }
}
