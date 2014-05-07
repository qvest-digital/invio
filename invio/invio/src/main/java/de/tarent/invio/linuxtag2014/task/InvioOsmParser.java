package de.tarent.invio.linuxtag2014.task;

import android.util.Log;
import de.tarent.nic.android.base.position.NicGeoPointImpl;
import de.tarent.nic.android.base.task.OsmParser;
import de.tarent.invio.linuxtag2014.products.Product;
import de.tarent.invio.linuxtag2014.products.ProductItem;
import de.tarent.nic.entities.Edge;
import de.tarent.nic.entities.NicGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * The OsmParser parses the xml that was created by josm and extracts all the objects that we need:
 * - edges
 * - products
 * - productItems
 * - northAngle
 * - indoorScale
 *
 * The parser makes some assumptions, in order to avoid full xml-parsing, which would be bloated and/or hard to test:
 * <br/><br/>
 * - all relevant tags are always on a line of their own (so we can parse it line by line)<br/>
 * - all nodes (shared points) are defined before any ways (so we already know them, when they are referenced)<br/>
 * - all IDs are negative integers (because that's how JOSM seems to do it)<br/>
 * - inside a <way> the <tag>-tag always comes after all the <nd>-tags (we use <tag> as the finish marker for
 *   "this is a valid way and we now have all the points on it".)
 * - nodes that are used for products are never part of an edge (may or may not be important...)
 *
 * Note: keep an eye on the complexity of this parser. Some day it might become too big and will need the be replaced
 *       with a general xml-parser.
 */
public class InvioOsmParser implements OsmParser, InvioOsmParserKeys {

    private static final String TAG = InvioOsmParser.class.getName();

    // With these patterns we can extract the attribute-values from our relevant xml-tags:
    private static final Pattern NODE_PATTERN =
            Pattern.compile("<node id='-(\\d+)'.*? lat='(-?[\\d.E+-]+)' lon='(-?[\\d.E+-]+)'( /)?>");
    private static final Pattern ND_PATTERN =
            Pattern.compile("<nd ref='-(\\d+)' />");
    private static final Pattern PRODUCT_PATTERN =
            Pattern.compile("<tag k='(EAN-13|name|price)' v='(.+)' />");
    private static final Pattern NORTH_ANGLE_PATTERN =
            Pattern.compile("<tag k='north_angle' v='(-?\\d+)' />");
    private static final Pattern INDOOR_SCALE_PATTERN =
            Pattern.compile("<tag k='indoor_scale' v='(\\d.+)' />");
    private static final Pattern NAMESPACE_PATTERN =
            Pattern.compile("tag k='(namespace_group_name|namespace_map_name|namespace_short_name)' v='(.+)' />");

    private List<Edge> edges;
    private List<GeoPoint> wayPoints;
    private Map<String, GeoPoint> points;
    private Set<Product> products;
    private Set<ProductItem> productItems;

    private Integer northAngle;
    private Float indoorScale;
    //Namespace map containing maps group name, name and short name (for the floor button label).
    private Map<String, String> namespace = new HashMap<String, String>();

    private String lastNodeId;

    private String mapShortId;

    /**
     * Constructor.
     */
    public InvioOsmParser(String mapshortId) {
        products = new HashSet<Product>();
        productItems = new HashSet<ProductItem>();
        this.mapShortId = mapshortId;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public Set<ProductItem> getProductItems() {
        return productItems;
    }


    @Override
    public Map<String, Collection> getResults() {
        final Map<String, Collection> result = new HashMap<String, Collection>();

        result.put(EDGES, getEdges());
        result.put(PRODUCTS, getProducts());
        result.put(PRODUCTITEMS, getProductItems());
        result.put(NORTH_ANGLE, getNorthAngle());
        result.put(INDOOR_SCALE, getIndoorScale());
        result.put(NAMESPACE, getNamespaceMap());

        return result;
    }

    /**
     * Parse an osm-xml stream line by line and extract:
     * - the edges that are defined with the tag k='indoor' v='Gang'.
     * - the products (nodes with EAN-13-tags)
     *
     * @param in the xml data
     * @throws java.io.IOException if something goes wrong while reading from the stream
     */
    public void parse(final InputStream in) throws IOException {

        final BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

        // Read the nodes, which come first in the xml, before the ways:
        readNodesAndProducts(br);

        // Then read the ways which contain point lists, from which we can create our edges.
        readWaysToGetEdges(br);

        // Might want to use the IOUtilWrapper here for testing, but it's in the mapserver...
        closeQuietly(in);
        closeQuietly(br);
    }

    /**
     * Read the nodes, which come first in the xml, before the ways. Store them, for use with the ways, and also
     * create products from the nodes which contain them.
     *
     * @param reader the BufferedReader with the xml
     * @throws IOException if there is something wrong with the reader
     */
    private void readNodesAndProducts(final BufferedReader reader) throws IOException {
        points = new HashMap<String, GeoPoint>();
        String line = reader.readLine();

        while ((line != null) && (!line.contains("<way"))) {
            extractNodes(line);
            reader.mark(256);
            line = reader.readLine();
            if (line != null) {
                extractProductAndProductItem(reader, line);
            }
        }
        // We reset the stream to unread the last line, which will probably be the first line of the first way.
        reader.reset();
    }

    private void extractNodes(String line) {
        if (line.contains("k='north_angle'")){
            extractNorth(line);
        } else if (line.contains("k='indoor_scale'")) {
            extractIndoorScale(line);
        } else if (line.contains("k='namespace_")) {
            extractNamespace(line);
        } else {
            extractPoint(line);
        }
    }

    private void extractPoint(final String currentLine) {
        final Matcher m = NODE_PATTERN.matcher(currentLine);
        if (m.find()) {
            addPoint(m);
        }
    }

    private void addPoint(final Matcher m) {
        final float lat = Float.parseFloat(m.group(2));
        final float lon = Float.parseFloat(m.group(3));
        lastNodeId = m.group(1);
        points.put(lastNodeId, new NicGeoPointImpl(lat, lon));
    }

    /**
     * Here we extract the namespace information such as group name, map name and short name.
     *
     * @param line to be parsed
     */
    private void extractNamespace(final String line) {
        final Matcher m = NAMESPACE_PATTERN.matcher(line);
        if (m.find()) {
            final String key = m.group(1);
            final String value = m.group(2);
            namespace.put(key, value);
        }
    }

    private void extractProductAndProductItem(final BufferedReader reader, String currentLine) throws IOException {
        final Matcher m = PRODUCT_PATTERN.matcher(currentLine);
        if (m.find()) {
            // This is totally not robust: When we see one tag we just assume that we will find all three!
            final Product product = getProduct(reader, m);
            products.add(product);
            productItems.add(new ProductItem(product, points.get(lastNodeId)));
        }
    }

    private Product getProduct(final BufferedReader reader, Matcher m) throws IOException {
        final Map<String, String> tags = new HashMap<String, String>();
        tags.put(m.group(1), m.group(2));
        m = PRODUCT_PATTERN.matcher(reader.readLine());
        m.find();
        tags.put(m.group(1), m.group(2));
        m = PRODUCT_PATTERN.matcher(reader.readLine());
        m.find();
        tags.put(m.group(1), m.group(2));

        final Product product = new Product(Long.parseLong(tags.get("EAN-13")),
                                            replaceApostrophes(tags.get("name")),
                                            Integer.parseInt(tags.get("price")));
        product.setMapShortName(mapShortId);

        return product;
    }


    /**
     * Here we put the north angle into the result map.
     * It's a list because that's the convention of the {@link de.tarent.nic.android.base.task.DownloadTask},
     * but of course there's only one north per map.
     *
     * @return northList List with only one element, which is the north angle
     */
    private List<Integer> getNorthAngle() {
        final List<Integer> northList = new ArrayList<Integer>();
        northList.add(northAngle);
        return northList;
    }

    /**
     * Here we put the scale of the indoor map into the result map.
     * It's a list because that's the convention of the {@link de.tarent.nic.android.base.task.DownloadTask},
     * but of course there's only one scale value per indoor map.
     *
     * @return scaleList List with only one element, which is the scale of the indoor map
     */
    private List<Float> getIndoorScale() {
        final List<Float> scaleList = new ArrayList<Float>();
        scaleList.add(indoorScale);
        return scaleList;
    }

    /**
     * Here we put namespace {@link java.util.Map} containing group name, name and short name (floor button name)
     * of the indoor map.
     *
     * @return {@link java.util.List} with only one element, wich is the {@link java.util.Map} containing the
     * namespace information.
     */
    private List<Map<String, String>> getNamespaceMap() {
        final List<Map<String, String>> namespaceList = new ArrayList<Map<String, String>>();
        namespaceList.add(namespace);
        return  namespaceList;
    }

    /**
     * Replaces the &apos; placeholder that JOSM adds to the tags with actual apostrophes.
     *
     * @param string the {@link String} to replace
     * @return the new {@link String} with apostrophe if there was one
     */
    private String replaceApostrophes(final String string) {
        if (string.contains("&apos;")) {
            return string.replace("&apos;", "'");
        }

        return string;
    }

    /**
     * Read the ways which contain point lists, from which we can create our edges.
     * This method might read past the lines that it really needs! If we later want to read on then this needs to
     * be fixed.
     *
     * @param reader the BufferedReader with the xml
     * @throws IOException if there is something wrong with the reader
     */
    private void readWaysToGetEdges(final BufferedReader reader) throws IOException {
        edges = new ArrayList<Edge>();
        String line = reader.readLine();

        while (line != null) {
            parseLine(line, points);
            line = reader.readLine();
        }
    }

    private void parseLine(final String line, final Map<String, GeoPoint> points) {
        if (line.contains("<way ")) {
            wayPoints = new ArrayList<GeoPoint>();
        } else if (line.contains("<tag k='indoor' v='Gang' />")) {
            addEdges();
        } else if (line.contains("<nd ")) {
            extractWayPoint(line, points);
        }
    }

    private void extractNorth(final String line){
        final Matcher m = NORTH_ANGLE_PATTERN.matcher(line);
        if (m.find()) {
            final String angle = m.group(1);
            try {
                northAngle = Integer.valueOf(angle);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Could not parse north angle value: " + angle
                        + " . Exception: " + e.getMessage());
            }
        }
    }

    private void extractIndoorScale(String line) {
        final Matcher m = INDOOR_SCALE_PATTERN.matcher(line);
        if (m.find()) {
            final String scale = m.group(1);
            try {
                indoorScale = Float.valueOf(scale);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Could not parse indoor scale value: " + scale
                        +" . Exception: " + e.getMessage());
            }
        }
    }

    private void extractWayPoint(final String line, final Map<String, GeoPoint> points) {
        final Matcher m = ND_PATTERN.matcher(line);
        if (m.find()) {
            final String id = m.group(1);
            wayPoints.add(points.get(id));
        }
    }

    private void addEdges() {
        final int numberOfEdges = wayPoints.size() - 1;
        for (int i = 0; i < numberOfEdges; i++) {
            edges.add(new Edge((NicGeoPoint)wayPoints.get(i), (NicGeoPoint)wayPoints.get(i + 1)));
        }
    }

}
