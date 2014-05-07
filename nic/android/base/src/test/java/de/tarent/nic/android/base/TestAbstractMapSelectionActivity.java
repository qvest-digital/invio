package de.tarent.nic.android.base;

import android.content.Intent;

/**
 * Implementation of the {@link AbstractMapSelectionActivity} so that the class can be fully covered with unit tests.
 *
 * @author Désirée Amling <d.amling@tarent.de>
 */
public class TestAbstractMapSelectionActivity extends AbstractMapSelectionActivity {

    @Override
    public Intent getIntentWithMapName(final String mapName) {
        final Intent intent = new Intent(this, TestAbstractMapSelectionActivity.class);
        intent.putExtra("MapName", mapName);

        return intent;
    }
}
