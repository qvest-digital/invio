package de.tarent.nic.tracker.wifi;

import de.tarent.nic.entities.Histogram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The RouterLevelHistogram has one bin for each access-point (aka router), which contains a single, consolidated
 * value for the signal-strength. How that value is calculated depends on the factory-method used.
 * It is not an entity (and therefore not in the entities-module) because it doesn't need to be transmitted or
 * persisted. It can always be regenerated from a complete (bin-level-) Histogram.
 */
public class RouterLevelHistogram extends HashMap<String, Float> {


    /**
     * Copy constructor. Instantiate a new RouterLevelHistogram that has the same values as an existing one.
     *
     * @param rlh the RouterLevelHistogram that we want to copy.
     */
    public RouterLevelHistogram(final RouterLevelHistogram rlh) {
        super(rlh);
    }

    /**
     * Construct a new, empty RouterLevelHistogram,
     */
    public RouterLevelHistogram() {
        super();
    }


    /**
     * Remove all elements from this histogram that are not present in some other histogram.
     *
     * @param rlh the other histogram, with which this one will be intersected.
     * @return the number of entries/access points that were removed, i.e. were not present in rlh
     */
    public int intersect(final RouterLevelHistogram rlh) {
        final int size = size();
        keySet().retainAll(rlh.keySet());
        return size - size();
    }


    /**
     * Convert a Histogram to a RouterLevelHistogram using the median of the measured signal-strengths for each
     * access point.
     *
     * @param histogram the complete Histogram, containing signal-level-curves/bins.
     * @return the new RouterLevelHistogram: "<BSSID, Median-Strength>"
     */
    public static RouterLevelHistogram makeMedianHistogram(final Histogram histogram) {
        final RouterLevelHistogram rlh = new RouterLevelHistogram();

        for (Map.Entry<String, Map<Integer, Float>> accessPoint : histogram.entrySet()) {
            rlh.put(accessPoint.getKey(), calculateMedianLevel(accessPoint.getValue()));
        }

        return rlh;
    }

    /**
     * Convert a Histogram to a RouterLevelHistogram using the weighted average of the measured signal-strengths for
     * each access point.
     *
     * @param histogram the complete Histogram, containing signal-level-curves/bins.
     * @return the new RouterLevelHistogram: "<BSSID, Average-Strength>"
     */
    public static RouterLevelHistogram makeAverageHistogram(final Histogram histogram) {
        final RouterLevelHistogram rlh = new RouterLevelHistogram();

        for (Map.Entry<String, Map<Integer, Float>> accessPoint : histogram.entrySet()) {
            rlh.put(accessPoint.getKey(), calculateWeightedAverageLevel(accessPoint.getValue()));
        }

        return rlh;
    }


    /**
     * Create a normalized RouterLevelHistogram, i.e. one where all the values sum up to 1.
     * For some algorithms, e.g. Kullback-Leibler, the histogram needs to be normalized as if it were a real
     * probability-distribution (which RouterLevelHistograms are not in any meaningful sense because the bins are all
     * totally independent).
     *
     * @param histogram the not normalized histogram.
     * @return a new and normalized RouterLevelHistogram
     */
    public static RouterLevelHistogram makeNormalizedHistogram(final RouterLevelHistogram histogram) {
        final RouterLevelHistogram rlh = new RouterLevelHistogram();

        if (histogram.size() > 0) {
            float sum = 0;
            for (float value : histogram.values()) {
                sum += value;
            }

            for (Map.Entry<String, Float> accessPoint : histogram.entrySet()) {
                rlh.put(accessPoint.getKey(), accessPoint.getValue() / sum);
            }
        }

        return rlh;
    }


    private static Float calculateMedianLevel(final Map<Integer, Float> levels) {
        final List<Integer> strengths = new ArrayList<Integer>(levels.keySet());
        Collections.sort(strengths);
        float median = 0;
        final int size = strengths.size();
        if ((size % 2) == 1) {
            median = (float)strengths.get(size / 2);
        } else {
            // For an even number of elements the median is the average of the two elements nearest to the middle:
            median = 0.5f * (strengths.get(size / 2) + strengths.get((size-1) / 2));
        }
        return median;
    }

    private static Float calculateWeightedAverageLevel(final Map<Integer, Float> levels) {
        float average = 0;
        for (Map.Entry<Integer, Float> level : levels.entrySet()) {
            final float strength = level.getKey();
            final float probability = level.getValue();
            average += strength * probability;
        }
        return average;
    }

}
