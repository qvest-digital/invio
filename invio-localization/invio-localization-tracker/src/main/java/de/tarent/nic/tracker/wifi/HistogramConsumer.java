package de.tarent.nic.tracker.wifi;

import de.tarent.nic.entities.Histogram;

/**
 * A HistogramConsumer is a class that wants to receive histograms from somewhere.
 */
public interface HistogramConsumer {

    /**
     * Pass the histo, please!
     * @param histogram the Histogram
     */
    void addHistogram(Histogram histogram);

}
