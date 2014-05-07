package de.tarent.nic.tracker.wifi.divergence;

import de.tarent.nic.tracker.wifi.RouterLevelHistogram;

import java.util.Map;


/**
 * This KL-implementation compares only those accesspoint-bins which are present in both histograms. The confidence is
 * reduced, if many single accesspoints have to be removed from the histograms.
 *
 * See http://en.wikipedia.org/wiki/Kullback–Leibler_divergence
 * See http://stats.stackexchange.com/questions/60619/how-to-calculate-kullback-leibler-divergence-distance
 */
public class KullbackLeibler implements RouterLevelDivergence {


    private float symmetricalDivergence;

    private float confidence;

    /**
     * Initialized means that all the data has been reset/deleted and the next histogram can be processed.
     */
    private boolean initialized = false;


    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final RouterLevelHistogram p, final RouterLevelHistogram q) {
        reset();

        // Special case: if at least one histogram is empty then we can't compare anything and as a consequence we
        // have no confidence. But we don't consider this a quantifiable divergence either.
        if ((p.size() > 0) && (q.size() > 0)) {
            // The histograms have to be normalized because:
            // "The K–L divergence is only defined if P and Q both sum to 1".
            // That means we can't work with our arbitrary signal levels which are, to make matters worse, all negative.

            // First we remove all the access points which are present in only one of the histograms:
            final RouterLevelHistogram cutP = new RouterLevelHistogram(p);
            cutP.intersect(q);
            final RouterLevelHistogram cutQ = new RouterLevelHistogram(q);
            cutQ.intersect(p);
            // TODO: unittest for empty intersection

            // Then we normalize with the remaining entries:
            final RouterLevelHistogram normalP = RouterLevelHistogram.makeNormalizedHistogram(cutP);
            final RouterLevelHistogram normalQ = RouterLevelHistogram.makeNormalizedHistogram(cutQ);

            // TODO: maybe we should divide the symmetricalDivergence by the confidence, in order to punish fingerprints
            //       with fewer matches. Or normalize by the size of normalP, or p or q, or p+q, or something like that?
            symmetricalDivergence = directionalDivergence(normalP, normalQ) + directionalDivergence(normalQ, normalP);
            // We now make this the average-divergence among all the shared accesspoints, because otherwise histograms
            // with many matches would be punished (when we just add up the divergence of each match):
            symmetricalDivergence /= normalP.size();
            confidence = ((float)(normalP.size() + normalQ.size())) / (p.size() + q.size());
        }

        initialized = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getDivergence() {
        if (!initialized) {
            throw new IllegalStateException("There is no data. Did you forget to call init?");
        }
        return symmetricalDivergence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getConfidence() {
        if (!initialized) {
            throw new IllegalStateException("There is no data. Did you forget to call init?");
        }
        return confidence;
    }


    private void reset() {
        initialized = false;
        symmetricalDivergence = 0;
        confidence = 0;
    }

    private float directionalDivergence(final RouterLevelHistogram p, final RouterLevelHistogram q) {
        float divergence = 0;
        for (final Map.Entry<String, Float> accessPoint : p.entrySet()) {
            final String bssId = accessPoint.getKey();
            final float pLevel = accessPoint.getValue();
            // If there is no measurement for this access point in the other histogram at all we can't calculate
            // the divergence. Maybe an access point was switched on/off between the creation of the two histograms.
            // But maybe it is really undetectably weak. We can't be sure and this will reduce our confidence.
            if (q.containsKey(bssId)) {
                final float qLevel = q.get(bssId);
                // If the level is the same in both histograms we have here a multiplication with log(1), which is 0.
                // That means we don't add anything to the divergence for this access point.
                divergence += Math.log(pLevel/qLevel) * pLevel;
            }
        }
        return divergence;
    }

}
