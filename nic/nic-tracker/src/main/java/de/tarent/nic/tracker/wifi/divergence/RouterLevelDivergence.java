package de.tarent.nic.tracker.wifi.divergence;

import de.tarent.nic.tracker.wifi.RouterLevelHistogram;

/**
 * A RouterLevelDivergence is an algorithm that calculates divergences of RouterLevelHistograms,
 * i.e. the distance, according to some abstract metric, between two RouterLevelHistograms p and q. Usually one of
 * them will represent a stored fingerprint and the other will represent a current measurement of wifi-signals.
 * But it is also perfectly alright to calculate divergences between two fingerprints. That can be used to evaluate the
 * quality of a metric, by comparing the calculated divergences with the known spatial positions of the fingerprints.
 */
public interface RouterLevelDivergence {

    /**
     * Initialize the divergence-algorithm with two histograms. All previous results are overwritten. Before this
     * method is called for the first time there are no results and the behaviour of all other methods is undefined.
     * @param p one RouterLevelHistogram
     * @param q the other RouterLevelHistogram
     */
    void init(RouterLevelHistogram p, RouterLevelHistogram q);

    /**
     * Get the divergence of p and q according to this algorithm. The implementation should ensure that this operation
     * is symmetric (i.e. divergence(p, q) == divergence(q, p)), because otherwise it wouldn't be a proper "metric".
     * @return the divergence/difference/distance; 0 for p == q.
     */
    float getDivergence();

    /**
     * Get the confidence that the algorithm has in its divergence-calculation. For example, if we have sparse
     * histograms, where many accesspoints are only present in one of them, then the confidence would be low.
     * If both histograms have data about most or all of the same accesspoints then the confidence would be high.
     * @return the confidence, [0..1]. 0 = pure random guess, 1 = absolute certainty
     */
    float getConfidence();

}
