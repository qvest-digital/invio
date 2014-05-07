package de.tarent.nic.android.admin.wifi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import de.tarent.nic.android.admin.R;
import de.tarent.nic.android.base.wifi.FingerprintItem;

import java.util.Map;

/**
 * The FingerprintDialog shows a data stored in a fingerprint and allows the user to delete the fingerprint.
 */
public class FingerprintDialog extends DialogFragment {

    /**
     * We need the manager if the user wants to remove the fingerprint.
     */
    private AdminFingerprintManager manager;

    /**
     * The dialog belongs to one specific fingerprint.
     */
    private FingerprintItem item;


    /**
     * Construct a new FingerprintDialog.
     *
     * @param manager the FingerprintManager which manages the fingerprint.
     * @param item    the item that is to be shown in this dialog.
     */
    public FingerprintDialog(final AdminFingerprintManager manager, final FingerprintItem item) {
        this.manager = manager;
        this.item = item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final String message = buildMessage();
        final String title = buildTitle();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.dialog_fingerprint_button_ok, createOkListener())
                .setNegativeButton(R.string.dialog_fingerprint_button_delete, createDeleteListener());
        return builder.create();
    }


    /**
     * This listener is called when the user clicks "delete" and it removes the fingerprint from the manager without
     * any further confirmation.
     *
     * @return the OnClickListener
     */
    private DialogInterface.OnClickListener createDeleteListener() {
        return new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                manager.removeFingerprintFromOverlay(item);
            }
        };
    }

    /**
     * This listener is called when the user clicks "OK" but it doesn't do anything. Just close the dialog.
     *
     * @return the OnClickListener
     */
    private DialogInterface.OnClickListener createOkListener() {
        return new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                // Dismissed...
            }
        };
    }

    /**
     * The title of the dialog shows the ID of the fingerprint and its coordinates.
     *
     * @return the title string
     */
    private String buildTitle() {
        final String title = item.getId() + ": " +
                item.getFingerprint().getPoint().getLatitudeE6() + ", " +
                item.getFingerprint().getPoint().getLongitudeE6();
        item.getFingerprint().getPoint();
        return title;
    }

    /**
     * The message is the body of the dialog. It shows the list of access points and their signal strength levels
     * at this point.
     *
     * @return the message body
     */
    private String buildMessage() {
        final StringBuilder message = new StringBuilder();
        for (final String bssid : item.getFingerprint().getHistogram().keySet()) {
            message.append(bssid).append("\n");
            final Map<Integer, Float> levels = item.getFingerprint().getHistogram().get(bssid);
            for (final Map.Entry<Integer, Float> level : levels.entrySet()) {
                message.append("  ").append(level.getKey()).append("dB: ").append(level.getValue()).append("\n");
            }
        }
        return message.toString();
    }

}
