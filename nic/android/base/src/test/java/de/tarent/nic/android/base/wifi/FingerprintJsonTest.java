package de.tarent.nic.android.base.wifi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.tarent.nic.android.base.json.NicGeoPointDeserializer;
import de.tarent.nic.android.base.position.NicGeoPointImpl;
import de.tarent.nic.entities.Fingerprint;
import de.tarent.nic.entities.Histogram;
import de.tarent.nic.entities.NicGeoPoint;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * This test shows/tests if we can convert fingerprints to/from json.
 */
public class FingerprintJsonTest {

    // This json contains 3 fingerprints. Some facts about them, that can be checked:
    // - the first one has the id "my histo 1"
    // - the second one has 14 accesspoints
    // - the second one has coordinates -59414062, -64997939
    // - the third one has levels -71 at probability 0.2 for "f0:7d:68:50:ac:ac"
    String json = "[{\"histogram\":{\"00:1f:3f:12:30:32\":{\"-93\":0.2,\"-91\":0.2,\"-89\":0.2,\"-88\":0.4},\"00:1b:2f:f3:be:8a\":{\"-89\":0.75,\"-88\":0.25},\"bc:05:43:7b:0b:8a\":{\"-92\":0.5,\"-90\":0.5},\"00:0b:3b:58:0e:c0\":{\"-90\":1.0},\"44:32:c8:30:6d:7b\":{\"-88\":0.2,\"-87\":0.8},\"00:23:08:e7:ad:5f\":{\"-57\":0.2,\"-55\":0.8},\"24:65:11:64:1d:37\":{\"-92\":0.2,\"-90\":0.8},\"bc:05:43:12:a2:27\":{\"-87\":0.2,\"-86\":0.2,\"-85\":0.6},\"00:1c:28:56:08:fb\":{\"-92\":0.2,\"-91\":0.2,\"-87\":0.6},\"9c:c7:a6:39:fa:ca\":{\"-89\":0.2,\"-88\":0.4,\"-87\":0.2,\"-86\":0.2},\"74:31:70:1b:c8:97\":{\"-87\":1.0},\"84:1b:5e:75:18:c2\":{\"-93\":0.2,\"-92\":0.6,\"-90\":0.2},\"f0:7d:68:50:ac:ac\":{\"-71\":0.4,\"-70\":0.6},\"00:1c:28:20:62:fa\":{\"-93\":1.0}},\"id\":\"my histo 1\",\"point\":{\"mAltitude\":0,\"mLatitudeE6\":-77823323,\"mLongitudeE6\":-45878906}},{\"histogram\":{\"00:1f:3f:12:30:32\":{\"-91\":0.2,\"-90\":0.2,\"-87\":0.4,\"-85\":0.2},\"00:1b:2f:f3:be:8a\":{\"-89\":0.6,\"-88\":0.4},\"bc:05:43:7b:0b:8a\":{\"-92\":1.0},\"00:0b:3b:58:0e:c0\":{\"-90\":1.0},\"44:32:c8:30:6d:7b\":{\"-90\":0.2,\"-86\":0.8},\"20:2b:c1:f4:c1:71\":{\"-93\":1.0},\"00:23:08:e7:ad:5f\":{\"-58\":0.4,\"-57\":0.6},\"24:65:11:64:1d:37\":{\"-93\":0.2,\"-92\":0.2,\"-91\":0.4,\"-90\":0.2},\"bc:05:43:12:a2:27\":{\"-86\":0.8,\"-84\":0.2},\"00:1c:28:56:08:fb\":{\"-94\":0.2,\"-92\":0.2,\"-91\":0.2,\"-87\":0.4},\"9c:c7:a6:39:fa:ca\":{\"-89\":0.4,\"-87\":0.6},\"84:1b:5e:75:18:c2\":{\"-93\":0.2,\"-92\":0.2,\"-91\":0.2,\"-89\":0.4},\"f0:7d:68:50:ac:ac\":{\"-71\":0.8,\"-70\":0.2},\"00:1c:28:20:62:fa\":{\"-93\":1.0}},\"id\":\"my histo 2\",\"point\":{\"mAltitude\":0,\"mLatitudeE6\":-64997939,\"mLongitudeE6\":-59414062}},{\"histogram\":{\"00:1f:3f:12:30:32\":{\"-93\":0.2,\"-92\":0.4,\"-91\":0.2,\"-88\":0.2},\"00:1b:2f:f3:be:8a\":{\"-89\":1.0},\"bc:05:43:7b:0b:8a\":{\"-90\":1.0},\"44:32:c8:30:6d:7b\":{\"-86\":0.6,\"-85\":0.4},\"20:2b:c1:f4:c1:71\":{\"-93\":1.0},\"00:23:08:e7:ad:5f\":{\"-57\":0.8,\"-56\":0.2},\"24:65:11:64:1d:37\":{\"-89\":1.0},\"bc:05:43:12:a2:27\":{\"-86\":0.6,\"-85\":0.2,\"-84\":0.2},\"00:1c:28:56:08:fb\":{\"-91\":0.33333334,\"-88\":0.33333334,\"-87\":0.33333334},\"9c:c7:a6:39:fa:ca\":{\"-88\":0.2,\"-87\":0.2,\"-86\":0.6},\"70:ca:9b:9a:2a:d0\":{\"-90\":1.0},\"74:31:70:1b:c8:97\":{\"-92\":0.8,\"-88\":0.2},\"84:1b:5e:75:18:c2\":{\"-94\":0.6,\"-91\":0.2,\"-90\":0.2},\"f0:7d:68:50:ac:ac\":{\"-71\":0.2,\"-70\":0.8},\"00:1c:28:20:62:fa\":{\"-93\":0.8,\"-88\":0.2}},\"id\":\"my histo 3\",\"point\":{\"mAltitude\":0,\"mLatitudeE6\":-65585720,\"mLongitudeE6\":-14062500}}]";

    // Deserialize a prepared json-string.
    @Test
    public void testDeserialize() {
        // This type is necessary to tell gson about the generic type. Without it, it would only know that it needs
        // to create an ArrayList, not that it's an ArrayList of Fingerprints (see "type erasure").
        Type fooType = new TypeToken<ArrayList<Fingerprint>>() {}.getType();
        // We need to use the GsonBuilder because we need to register a JsonDeserializer for the NicGeoPoint.
        // NicGeoPoint is only an interface, so gson has no way of creating instances for it. That will be the job
        // of our custom JsonDeserializer - which will not be the same as in the App, where it will create NicGeoPointImpl.
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(NicGeoPoint.class, new NicGeoPointDeserializer());

        List<Fingerprint> fingerprints = gsonBuilder.create().fromJson(json, fooType);

        assertEquals(3, fingerprints.size());
        assertEquals("my histo 1", fingerprints.get(0).getId());
        assertEquals(14, fingerprints.get(1).getHistogram().size());
        assertEquals(-64997939, fingerprints.get(1).getPoint().getLatitudeE6());
        assertEquals(-59414062, fingerprints.get(1).getPoint().getLongitudeE6());
        assertEquals(0.2f, fingerprints.get(2).getHistogram().get("f0:7d:68:50:ac:ac").get(-71), 0.000001f);
    }

    // Serialize two simple fingerprints, deserialize them again, and compare the result.
    @Test
    public void testSerialize() {
        List<Fingerprint> fingerprintsIn = setupFingerprints();

        Gson gson = new Gson();
        String json = gson.toJson(fingerprintsIn);

        Type fooType = new TypeToken<ArrayList<Fingerprint>>() {}.getType();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(NicGeoPoint.class, new NicGeoPointDeserializer());

        List<Fingerprint> fingerprintsOut = gsonBuilder.create().fromJson(json, fooType);

        assertEquals(fingerprintsIn.size(), fingerprintsOut.size());
        assertEquals(fingerprintsIn.get(0).getHistogram().keySet(), fingerprintsOut.get(0).getHistogram().keySet());
        assertEquals(fingerprintsIn.get(0).getHistogram().get("00:11:22:33:44:55").get(-31),
                     fingerprintsOut.get(0).getHistogram().get("00:11:22:33:44:55").get(-31));
        assertEquals(fingerprintsIn.get(1).getPoint().getLatitudeE6(), fingerprintsOut.get(1).getPoint().getLatitudeE6());
        assertEquals(fingerprintsIn.get(1).getPoint().getLongitudeE6(), fingerprintsOut.get(1).getPoint().getLongitudeE6());
        assertEquals(fingerprintsIn.get(1).getId(), fingerprintsOut.get(1).getId());
    }


    private List<Fingerprint> setupFingerprints() {
        List<Fingerprint> l = new ArrayList<Fingerprint>();
        Fingerprint f1 = new Fingerprint(new Histogram("h1"), new NicGeoPointImpl(10, 20));
        Map<Integer, Float> m1 = new HashMap<Integer, Float>();
        m1.put(-30, 0.1f);
        m1.put(-31, 0.9f);
        Map<Integer, Float> m2 = new HashMap<Integer, Float>();
        m2.put(-60, 1f);
        f1.getHistogram().put("00:11:22:33:44:55", m1);
        f1.getHistogram().put("a0:a1:a2:a3:a4:a5", m2);
        Fingerprint f2 = new Fingerprint(new Histogram("h2"), new NicGeoPointImpl(33333, -44444));
        Map<Integer, Float> m3 = new HashMap<Integer, Float>();
        m3.put(-77, 0.5f);
        m3.put(-88, 0.5f);
        f2.getHistogram().put("ff:00:ff:00:ff:00", m3);
        l.add(f1);
        l.add(f2);
        return l;
    }
}
