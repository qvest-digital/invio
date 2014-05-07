package de.tarent.nic.entities;

import java.util.HashMap;
import java.util.Map;


/**
 * The Histogram stores the distribution of wifi signal strength levels (over multiple measurements) for a set of
 * access points which are identified by their BSSID.
 * It is as Map<String bssid, Map<Integer dB, Float fraction>> with a name (id).
 * The keySet of this map is the set of all encountered BSSIDs.
 * The values are a map of signal strength level distributions.
 */
public class Histogram extends HashMap<String, Map<Integer, Float>> {

    private String id;


    /**
     * Construct a new, empty and nameless Histogram
     */
    public Histogram() {
        super();
    };

    /**
     * Construct a new, empty Histogram with an ID.
     * @param id the ID (name) of this Histogram.
     */
    public Histogram(final String id) {
        super();
        this.id = id;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
