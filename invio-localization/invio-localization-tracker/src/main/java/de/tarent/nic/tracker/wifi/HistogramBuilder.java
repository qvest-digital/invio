package de.tarent.nic.tracker.wifi;

import de.tarent.nic.entities.Histogram;
import de.tarent.nic.entities.WifiScanResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The HistogramBuilder is used to incrementally build up a Histogram from ScanResults.
 * It counts the measured signal strength levels and converts these numbers into fractions for the final Histogram.
 * Please note that there is no "reset"-method.
 */
public class HistogramBuilder {

    private final String id;

    /**
     * The maximum age in milliseconds that a scanresult my have. Older scans are not included in the final histogram.
     */
    private int maxAge;

    /**
     * In this map we store the scanresults, identified by currentTimeMillis.
     */
    private SortedMap<Long, List<WifiScanResult>> scanResults;


    /**
     * Construct a new, empty HistogramBuilder.
     * @param id the id/name for the new Histogram.
     */
    public HistogramBuilder(final String id) {
        this(id, 0);
    }

    /**
     * Construct a new, empty HistogramBuilder with a time-constraint. Scanresults older than a certain timespan will
     * not be included in the histogram. They will be discarded when they have gotten too old.
     * @param id the id/name for the new Histogram.
     * @param maxAge the maximum age in milliseconds that a scanresult may have for it to be included in the histogram.
     *               0 = ignore the age and just include everything.
     */
    public HistogramBuilder(final String id, final int maxAge) {
        this.id = id;
        this.maxAge = maxAge;
        scanResults = new TreeMap<Long, List<WifiScanResult>>();
    }


    /**
     * Integrate new ScanResults into this Histogram.
     *
     * @param scan the List of ScanResults as supplied by the Android-WifiManager.
     * @return this, for chaining (not that anyone would have multiple ScanResults at the same time).
     */
    public HistogramBuilder addScanResults(final List<WifiScanResult> scan) {
        final List<WifiScanResult> scanCopy = new ArrayList<WifiScanResult>();
        scanCopy.addAll(scan);
        scanResults.put(makeUniqueTime(), scanCopy);

        return this;
    }


    /**
     * Build a new Histogram from the currently collected scanresults.
     * @return the new Histogram
     */
    public Histogram build() {
        final Histogram histogram = new Histogram(id);

        // Make the mapping of bssid to signal-level-count-curve:
        final Map<String, Levels> curves = makeCurves();

        for (Map.Entry<String, Levels> curve : curves.entrySet()) {
            histogram.put(curve.getKey(), curve.getValue().getLevels());
        }
        return histogram;
    }


    /**
     * This method generates a timestamp (in ms), which will be unique as far as the collection of scanresults in this
     * HistogramBuilder-instance is concerned. To achieve this goal the method might wait for one millisecond.
     * That should only happen during testing though, because it is unlikely that any device can scan so fast.
     * @return the current (return-) time in milliseconds
     */
    private long makeUniqueTime() {
        long time = System.currentTimeMillis();
        if (scanResults.containsKey(time)) {
            // Normally this method should not be called that fast. We must wait, because we want to use the current
            // time as the index to our Map.
            try {
                Thread.sleep(1);
                time = System.currentTimeMillis();
            } catch (InterruptedException e) {
                throw new IllegalStateException(
                        "HistogramBuilder.addScanResults must not be called twice during one millisecond!");
            }
        }
        return time;
    }

    /**
     * Add up the individual scan results into histogram-curves.
     * @return the mapping of bssid->curve
     */
    private Map<String, Levels> makeCurves() {
        final long oldestValidTime = (maxAge == 0) ? 0 : System.currentTimeMillis() - maxAge;

        final Map<String, Levels> curves = new HashMap<String, Levels>();
        // We use an iterator because it allows us to remove entries on the fly without mixing up our loop:
        final Iterator<Map.Entry<Long, List<WifiScanResult>>> iterator = scanResults.entrySet().iterator();

        while (iterator.hasNext()) {
            final Map.Entry<Long, List<WifiScanResult>> scan = iterator.next();
            if (scan.getKey() >= oldestValidTime) {
                include(scan.getValue(), curves);
            } else {
                iterator.remove();
            }
        }

        return curves;
    }


    /**
     * Add a list of scaneresults to a map of curves.
     * @param scanResults the NicScanResults
     * @param curves the curves, indexed by bssid
     */
    private void include(List<WifiScanResult> scanResults, Map<String, Levels> curves) {
        for (WifiScanResult accesspoint : scanResults) {
            if (!curves.containsKey(accesspoint.getBssid())) {
                final Levels levels = new Levels();
                curves.put(accesspoint.getBssid(), levels);
            }
            curves.get(accesspoint.getBssid()).add(accesspoint.getLevel());
        }
    }

    /**
     * The internal Levels contain a count of occurrences for each signal strength (for one AP). But that is not
     * interesting for the users of Histogram because they don't care how many measurements were made. They need the
     * distribution among the levels, which is calculated on the fly in getLevels().
     * It is static because its instances do not care about which instance of HistogramBuilder they are created for.
     * They can work for themselves and are only an inner class because nobody, except for the HistogramBuilder, needs
     * to know about them.
     */
    private static class Levels {
        private int count;

        /**
         * The levels map the signal strength to their number of occurrences.
         */
        private final Map<Integer, Integer> levels;

        /**
         * This is the normal constructor for when we want to start collecting data into a fresh, empty curve.
         */
        public Levels() {
            this.count = 0;
            this.levels = new TreeMap<Integer, Integer>();
        }

        /**
         * Add a new measurement and have it counted.
         *
         * @param level the new signal strength that was measured
         */
        public void add(final int level) {
            if (levels.containsKey(level)) {
                levels.put(level, levels.get(level) + 1);
            } else {
                levels.put(level, 1);
            }
            count++;
        }

        /**
         * Convert the counts into fractions according to the total number of measurements.
         *
         * @return the mapping of level to its share.
         */
        public Map<Integer, Float> getLevels() {
            final Map<Integer, Float> result = new TreeMap<Integer, Float>();
            for (Map.Entry<Integer, Integer> level : levels.entrySet()) {
                result.put(level.getKey(), (float) level.getValue() / count);
            }
            return result;
        }

    }

}
