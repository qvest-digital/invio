package de.tarent.nic.android.admin.wifi;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import de.tarent.nic.android.base.AbstractMapActivity;
import de.tarent.nic.android.base.position.NicGeoPointImpl;
import de.tarent.nic.android.base.wifi.FingerprintItem;
import de.tarent.nic.android.base.wifi.FingerprintManager;
import de.tarent.nic.android.base.wifi.WifiCapturer;
import de.tarent.nic.entities.Fingerprint;
import de.tarent.nic.entities.Histogram;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.tracker.wifi.HistogramConsumer;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;

import java.util.ArrayList;


/**
 * The FingerprintManager holds the wifi-fingerprints and the overlay to display them.
 * It is also responsible for uploading and downloading fingerprints to and from the mapserver.
 */
public class AdminFingerprintManager extends FingerprintManager implements HistogramConsumer {

    /**
     * This is the point where the user wants to create the next fingerprint. It has to be stored somewhere because
     * the actual creation is triggered later, asynchronously by the wifiCapturer.
     */
    private IGeoPoint fingerprintLocation;


    /**
     * Construct a new FingerprintManager.
     *
     * @param activity the parent activity, from which we need the Context and FragmentManager
     * @param mapServerClient the {@link de.tarent.nic.mapserver.MapServerClient}
     * @param mapView  the {@link MapView}
     * @param mapName  the name of the map, as it is used in the mapserver-URLs
     */
    public AdminFingerprintManager(final AbstractMapActivity activity,
                                   final MapServerClient mapServerClient,
                                   final MapView mapView, String mapName) {
        super(activity, mapServerClient, mapView, mapName);
    }

    /**
     * Construct a new FingerprintManager with possibility not to download the fingerprints.
     *
     * @param activity the parent activity, from which we need the Context and FragmentManager
     * @param mapServerClient the {@link de.tarent.nic.mapserver.MapServerClient}
     * @param mapView  the {@link MapView}
     * @param mapName  the name of the map, as it is used in the mapserver-URLs
     * @param downloadFingerprints flag in case we want to construct without downloading the fingerprints
     */
    public AdminFingerprintManager(final AbstractMapActivity activity,
                                   final MapServerClient mapServerClient,
                                   final MapView mapView, String mapName,
                                   final boolean downloadFingerprints) {
        super(activity, mapServerClient, mapView, mapName, downloadFingerprints);
    }

    /**
     * {@inheritDoc}
     */
    @Override // NOSONAR - This method cannot be shortened any more than it already has been.
    public boolean onItemSingleTapUp(final int index, final FingerprintItem item) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // This is recommended by http://developer.android.com/reference/android/app/DialogFragment.html
                // ...we do need a FragmentTransaction to show the dialog, it seems.
                final FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
                final Fragment prev = activity.getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                final DialogFragment dialog = new FingerprintDialog(AdminFingerprintManager.this, item);
                dialog.show(ft, "dialog");
            }
        });
        return true;
    }


    /**
     * Creates a fingerprint by using the users finger position.
     *
     * @param point          the {@link IGeoPoint}
     * @param progressDialog the {@link ProgressDialog}
     */
    public void createFingerprint(final IGeoPoint point, final ProgressDialog progressDialog) {
        fingerprintLocation = point;
        final ProgressDialog createFingerprintProgressDialog = progressDialog;
        final WifiCapturer wifiCapturer = new WifiCapturer(activity);
        wifiCapturer.makeFingerprint(createFingerprintProgressDialog, this);

        // and now we wait for the WifiCapturer to do its work, ending with the call to our addHistogram(Histogram).
    }

    /**
     * {@inheritDoc}
     * Add a new FingerprintItem to the overlay. It will be visible immediately at the position that was saved when
     * the user initially clicked on the FingerprintCaptureOverlay.
     */
    @Override
    public void addHistogram(Histogram histogram) {
        // TODO: what does the size of the overlay really mean? We don't care what was drawn, only what exists...
        histogram.setId("FP-" + (overlay.size() + 1));
        final Fingerprint fingerprint = new Fingerprint(histogram, new NicGeoPointImpl(fingerprintLocation));
        final FingerprintItem fingerprintItem = new FingerprintItem(fingerprint);
        overlay.addItem(fingerprintItem);
        mapView.postInvalidate();
    }

    /**
     * Upload the current list of Fingerprints to the mapserver. The old list on the server will be overwritten.
     */
    public void uploadFingerprints() {
        if (isNetworkAvailable()) {
            // Upload the fingerprints in the background:
            // TODO: we need a timeout here as well. Maybe, now that we're back at httpcomponents 4.0.1, we can
            //       have a "normal" setTimeout-method in our client-lib.
            new UploadFingerprintsTask(this, activity, mapServerClient, mapName).execute();
        } else {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, "No network: upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Create an overerlay to display icons for the fingerprints. In the admin-app we want to see the fingerprints,
     * so we need a different overlay than what we use in the customer-apps. This one here simply works like a normal
     * ItemizedIconOverlay, letting it draw its icons as it was meant to be.
     *
     * @param activity the activity, used for the context that the DefaultResourceProxyImpl of the overlay needs.
     * @param icon the Drawable that will be used for the icons.
     * @return the new ItemizedIconOverlay
     */
    protected ItemizedIconOverlay<FingerprintItem> createFingerprintOverlay(final Activity activity,
                                                                            final Drawable icon) {
        return new ItemizedIconOverlay<FingerprintItem>(new ArrayList<FingerprintItem>(),
                icon,
                this,
                new DefaultResourceProxyImpl(activity));
    }

}
