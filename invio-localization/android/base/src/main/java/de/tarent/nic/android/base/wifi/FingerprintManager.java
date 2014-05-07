package de.tarent.nic.android.base.wifi;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.tarent.nic.android.base.R;
import de.tarent.nic.android.base.json.NicGeoPointDeserializer;
import de.tarent.nic.android.base.task.CachedDownloadFingerprintsTask;
import de.tarent.nic.entities.Fingerprint;
import de.tarent.nic.entities.NicGeoPoint;
import de.tarent.nic.mapserver.MapServerClient;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.safecanvas.ISafeCanvas;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * The FingerprintManager holds the wifi-fingerprints and the overlay to display them.
 * It is also responsible for uploading and downloading fingerprints to and from the mapserver.
 */
public class FingerprintManager implements ItemizedIconOverlay.OnItemGestureListener<FingerprintItem> {

    protected Activity activity;

    protected ItemizedIconOverlay<FingerprintItem> overlay;

    protected MapView mapView;

    protected String mapName; // NOSONAR: this field is read by the AdminFingerprintManager

    /**
     * The MapServerClient is used by all the tasks that need to talk to the map-server:
     */
    protected MapServerClient mapServerClient;


    /**
     * Construct a new FingerprintManager.
     *
     * @param activity        the parent activity, from which we need the Context and FragmentManager
     * @param mapServerClient the {@link MapServerClient}
     * @param mapView         the {@link MapView}
     * @param mapName         the name of the map, as it is used in the mapserver-URLs
     */
    public FingerprintManager(final Activity activity,
                              final MapServerClient mapServerClient,
                              final MapView mapView,
                              final String mapName) {
        this(activity, mapServerClient, mapView, mapName, true);
    }

    /**
     * Construct a new FingerprintManager.
     *
     * @param activity             the parent activity, from which we need the Context and FragmentManager
     * @param mapServerClient      the {@link MapServerClient}
     * @param mapView              the {@link MapView}
     * @param mapName              the name of the map, as it is used in the mapserver-URLs
     * @param downloadFingerprints whether to download fingerprints-data or not. Can be skipped for multi-maps, because
     *                             for those all the download-tasks are triggered by the MultiLevelFingerprintManager.
     */
    public FingerprintManager(final Activity activity,
                              final MapServerClient mapServerClient,
                              final MapView mapView,
                              final String mapName,
                              final boolean downloadFingerprints) {
        this.activity = activity;
        this.mapView = mapView;
        this.mapName = mapName; // NOSONAR: this field is read by the AdminFingerprintManager
        this.mapServerClient = mapServerClient;
        final Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ips_histo);
        final Drawable icon = new BitmapDrawable(activity.getResources(), bm);
        overlay = createFingerprintOverlay(activity, icon); // NOSONAR: yes, we want subclasses to override this method.
        mapView.getOverlays().add(overlay);

        if (downloadFingerprints) {
            new CachedDownloadFingerprintsTask(this, activity, mapServerClient, mapName).execute();
        }
    }

    /**
     * Remove a FingerprintItem from the overlay.
     *
     * @param item the FingerprintItem we want to delete
     */
    public void removeFingerprintFromOverlay(final FingerprintItem item) {
        overlay.removeItem(item);
        mapView.postInvalidate();
    }

    /**
     * Remove all FingerprintItem from the overlay.
     */
    public void removeAllFingerprintFromOverlay() {
        overlay.removeAllItems();
        mapView.postInvalidate();
    }


    /**
     * Get the list of Fingerprints that this manager holds. Note that this is not a simple getter. It needs to do some
     * work to extract this list from the mapview-overlay.
     *
     * @return the list of Fingerprint-objects.
     */
    public List<Fingerprint> getFingerprints() {
        final List<Fingerprint> fingerprints = new ArrayList<Fingerprint>();
        // Maybe we don't have an active overlay yet:
        if (overlay != null) {
            final int size = overlay.size();
            for (int i = 0; i < size; i++) {
                fingerprints.add(overlay.getItem(i).getFingerprint());
            }
        }
        return fingerprints;
    }


    /**
     * Get a json-string that contains a serialization of the current fingerprints.
     *
     * @return the fingerprints as json
     */
    public String getFingerprintsJson() {
        final Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
        final String json = gson.toJson(getFingerprints());
        return json;
    }


    /**
     * Deserialize a json-string into fingerprints.
     *
     * @param json the fingerprints as json
     */
    public void setFingerprintsJson(final String json) {
        final List<Fingerprint> fingerprints = parseJson(json);
        if (fingerprints != null) {
            for (Fingerprint fingerprint : fingerprints) {
                final FingerprintItem fingerprintItem = new FingerprintItem(fingerprint);
                overlay.addItem(fingerprintItem);
            }
        }
        mapView.postInvalidate();
    }


    /**
     * The current number of fingerprints. TODO: really? How does overlay.size() work?
     *
     * @return the number of fingerprints
     */
    public int size() {
        return overlay.size();
    }

    /**
     * Detach all registered providers
     */
    public void detach() {
        Log.i(this.getClass().getName(), "Fingerprint manager does not need to implement detach, but" +
                "Multilevel must because it needs to detach at least one tile provider.");
    }


    /**
     * Create the overlay that displays the fingerprints, or rather, NOT displays any fingerprints.
     * Overwrite this method if you want your FingerprintManager to actually draw fingerprint-icons!
     *
     * @param activity the activity, used for the context that the DefaultResourceProxyImpl of the overlay needs.
     * @param icon the Drawable that will be used for the icons - if any are drawn.
     * @return the new, invisible ItemizedIconOverlay
     */
    protected ItemizedIconOverlay<FingerprintItem> createFingerprintOverlay(final Activity activity,
                                                                            final Drawable icon) {
        return new ItemizedIconOverlay<FingerprintItem>(new ArrayList<FingerprintItem>(),
                icon,
                this,
                new DefaultResourceProxyImpl(activity)) {
            @Override
            protected void onDrawItem(final ISafeCanvas canvas, final FingerprintItem item,
                                      final Point curScreenCoords, final float aMapOrientation) {
                // Don't draw... this is the base-package, and only the admin-app needs these icons.
            }
        };
    }


    /**
     * Test if we have network (otherwise we don't need to try downloading anything).
     *
     * @return true = online, false = offline
     */
    protected boolean isNetworkAvailable() {
        final ConnectivityManager manager =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = manager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }


    /**
     * Create Fingerprints from json-string.
     *
     * @param json the json that contains the serialized fingerprints
     * @return the List of Fingerprints
     */
    protected List<Fingerprint> parseJson(final String json) {
        // We need this TypeToken-thingy because it is not possible to have a Class-object for a generic type.
        // But we must tell gson what kind of object it is supposed to create. That's how we can do is:
        final Type genericFingerprintArrayListType = new TypeToken<ArrayList<Fingerprint>>() {
        }.getType();
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(NicGeoPoint.class, new NicGeoPointDeserializer());

        final List<Fingerprint> fingerprints = gsonBuilder.create().fromJson(json, genericFingerprintArrayListType);
        return fingerprints;
    }

    /**
     * The normal (kunden-) FingerprintManager doesn't show any items which could be tapped.
     * But the AdminFingerprintManager does.
     * {@inheritDoc}
     */
    @Override
    public boolean onItemSingleTapUp(final int index, final FingerprintItem item) {
        return false;
    }

    /**
     * The normal (kunden-) FingerprintManager doesn't show any items which could be tapped.
     * {@inheritDoc}
     */
    @Override
    public boolean onItemLongPress(final int index, final FingerprintItem item) {
        return false;
    }

}
