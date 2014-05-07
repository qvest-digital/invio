package de.tarent.nic.android.admin.wifi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.widget.Button;
import de.tarent.nic.android.admin.R;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.util.List;

/**
 * The FingerprintCaptureOverlay will be activated for edit-mode. It will catch the single-tap-events and create
 * a new Fingerprint. Then it will disable edit-mode and remove itself from the list of overlays.
 */
public class FingerprintCaptureOverlay extends Overlay implements DialogInterface.OnDismissListener {

    private Activity activity;

    private AdminFingerprintManager fingerprintManager;

    /**
     * This indicates the progress during fingerprint-capturing. The WifiCapturer will record its progress there and, at
     * the end, call the dismiss-method, which is why this activity wants to be an OnDismissListener.
     */
    private ProgressDialog createFingerprintProgressDialog;

    /**
     * There are two modes: scroll(+zoom)-mode and edit-mode. In edit-mode new fingerprints can be recorded but the map
     * cannot be moved. After a fingerprint is created the mode is automatically switched back to scroll-mode (i.e.
     * editModeEnabled = false).
     */
    private boolean editModeEnabled = false;

    /**
     * The MapView for which this is the capture-overlay.
     */
    private MapView mapView;


    /**
     * Constructor.
     *
     * @param activity the {@link Activity}
     * @param mapView  the MapView for which this will be an overlay.
     * @param manager  the FingerprintManager that is the owner of this overlay.
     */
    public FingerprintCaptureOverlay(final Activity activity, MapView mapView, AdminFingerprintManager manager) {
        super(activity);
        this.activity = activity;
        this.mapView = mapView;
        this.fingerprintManager = manager;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The FingerprintCaptureOverlay doesn't draw anything, it is invisible.
     */
    @Override
    protected void draw(final Canvas c, final MapView osmv, final boolean shadow) {
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The {@link FingerprintCaptureOverlay} depends on the {@link AdminFingerprintManager} to create the fingerprints.
     * Because we want to create the fingerprints using onSingleTapUp, the dependencies are the following:
     * {@link de.tarent.nic.android.admin.MapActivity} ->
     * {@link FingerprintCaptureOverlay} ->
     * {@link AdminFingerprintManager}
     */
    @Override
    public boolean onSingleTapUp(final MotionEvent e, final MapView mapView) {
        final MapView.Projection projection = mapView.getProjection();
        final IGeoPoint point = projection.fromPixels((int) e.getX(), (int) e.getY());

        createFingerprintProgressDialog = buildProgressDialog();
        fingerprintManager.createFingerprint(point, createFingerprintProgressDialog);

        // We don't want to pass on the event to the lower layers, so we declare it as "handled":
        return true;
    }


    /**
     * {@inheritDoc}
     * <p/>
     * We override onScroll only to prevent the other layers from receiving the events while we are in edit mode.
     *
     * @return true, as in "we handled the event, don't pass it on"
     */
    @Override
    public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float f1, final float f2,
                            final MapView v) {
        return true;
    }

    /**
     * Enables the fingerprint edit mode.
     */
    public void enableEditMode() {
        editModeEnabled = true;
        ((Button) activity.findViewById(R.id.create_fingerprint_button)).setText("scroll");
        activateFingerprintCaptureOverlay();
    }

    /**
     * Disables the fingerprint edit mode.
     */
    public void disableEditMode() {
        editModeEnabled = false;
        ((Button) activity.findViewById(R.id.create_fingerprint_button)).setText("edit");
        deactivateFingerprintCaptureOverlay();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        if (dialog.equals(createFingerprintProgressDialog)) {
            disableEditMode();
        }
    }

    public boolean isEditModeEnabled() {
        return editModeEnabled;
    }

    /**
     * Create & activate a new overlay that catches tap-events and reroutes them to the creation of a fingerprint.
     */
    private void activateFingerprintCaptureOverlay() {
        final List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.add(this);
    }

    private void deactivateFingerprintCaptureOverlay() {
        final List<Overlay> listOfOverlays = mapView.getOverlays();
        if (listOfOverlays.contains(this)) {
            listOfOverlays.remove(this);
        }
    }

    private ProgressDialog buildProgressDialog() {
        // prepare for a progress bar dialog
        // TODO: can't we move this to the wifiscanprogress.xml?
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Scanning...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnDismissListener(this);

        return progressDialog;
    }

}
