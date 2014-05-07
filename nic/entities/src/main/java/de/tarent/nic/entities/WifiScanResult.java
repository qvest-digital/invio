package de.tarent.nic.entities;

/**
 * The WifiScanResult stores the accesspoint-bssid/signal-strength-level pair.
 */
public class WifiScanResult {

    private String bssid;
    private int level;

    /**
     * Construct a new WifiScanResult.
     * @param bssid the BSSID of the accesspoint
     * @param level the measured signal-strength
     */
    public WifiScanResult(String bssid, int level) {
        this.bssid = bssid;
        this.level = level;
    }

    public String getBssid() {
        return bssid;
    }

    public int getLevel() {
        return level;
    }

}
