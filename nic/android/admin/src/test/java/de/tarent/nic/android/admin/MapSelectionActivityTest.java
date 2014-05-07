package de.tarent.nic.android.admin;

import android.content.Intent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

/**
 * @author Désirée Amling <d.amling@tarent.de>
 */
@Config(manifest = "../admin/AndroidManifest.xml") // We need to tell the test where the Manifest is located.
@RunWith(RobolectricTestRunner.class) // RunWith the RobolectricTestRunner.
public class MapSelectionActivityTest {

    private MapSelectionActivity activity;

    @Before
    public void setup() {
        // We have to make sure that Robolectric knows that it's awaiting an HTTPResponse.
        Robolectric.addPendingHttpResponse(200, "OK");

        // We have to tell Robolectric to not run any background tasks (AsyncTasks) because we do not have an actual
        // activity.
        Robolectric.getBackgroundScheduler().pause();

        // The activity is created with Robolectric.
        activity = Robolectric.buildActivity(MapSelectionActivity.class).create().get();
    }

    /**
     * Normal JUnit 4 is used in the tests.
     */
    @Test
    public void testGetIntentWithMapNameHoldsMapNameAsExtraString() {
        final String testMapName = "testMapName";
        Intent intent = activity.getIntentWithMapName(testMapName);

        assertEquals(intent.getStringExtra("MapName"), testMapName);
    }
}
