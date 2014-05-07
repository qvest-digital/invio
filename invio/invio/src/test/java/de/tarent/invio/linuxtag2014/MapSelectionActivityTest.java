package de.tarent.invio.linuxtag2014;


import android.content.Intent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class MapSelectionActivityTest {

    @Test
    public void testThatGetIntentWithMapNameReturnsIntentWithMapNameAsExtraParameter() {
        final MapSelectionActivity msa = new MapSelectionActivity();
        Intent intent1 = msa.getIntentWithMapName("test1");
        Intent intent2 = msa.getIntentWithMapName("test2");

        assertTrue(intent1.getStringExtra("MapName").equals("test1"));
        assertTrue(intent2.getStringExtra("MapName").equals("test2"));
    }

}
