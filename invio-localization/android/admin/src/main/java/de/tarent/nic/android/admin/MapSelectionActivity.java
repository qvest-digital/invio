package de.tarent.nic.android.admin;

import android.content.Intent;
import de.tarent.nic.android.base.AbstractMapSelectionActivity;

/**
 * The Admin-App implementation of the {@link AbstractMapSelectionActivity}.
 *
 * @author Désirée Amling <d.amling@tarent.de>
 */
public class MapSelectionActivity extends AbstractMapSelectionActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    public Intent getIntentWithMapName(final String mapName) {
        final Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("MapName", mapName);

        return intent;
    }
}
