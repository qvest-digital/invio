package de.tarent.nic.android.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;

/**
 * This class contains methods to generate a version {@link Dialog} and its message.
 *
 * @author Atanas Alexandrov, tarent solutions GmbH
 * @author: Andreas Grau, tarent solutions GmbH, 17.03.14
 */
public class VersionDialog {

    private Activity activity;

    /**
     * Create a new version {@link Dialog} for a specific {@link Activity}.
     *
     * @param activity The {@link Activity} the version {@link Dialog} should be used in.
     */
    public VersionDialog(Activity activity) {
        this.activity = activity;
    }

    /**
     * Build the version {@link Dialog} .
     *
     * @return The version {@link Dialog}.
     */
    public Dialog buildVersionInformationDialog() {
        final String dialogMessage = getVersionMessage();

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder.setTitle(R.string.version_title)
                .setMessage(dialogMessage)
                .setNeutralButton(R.string.close_version_dialog, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        final Dialog d = builder.create();
        return d;
    }

    private String getVersionMessage() {
        final Resources res = activity.getResources();
        final String versionNumber = res.getString(R.string.version);
        final String buildId = res.getString(R.string.build_id);
        return versionNumber + " (" + buildId + ")";
    }
}
