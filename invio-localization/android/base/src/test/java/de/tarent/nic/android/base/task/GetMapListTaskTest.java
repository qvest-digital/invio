package de.tarent.nic.android.base.task;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class GetMapListTaskTest {

    @Test
    public void testMakeMultiLevelMaps() {
        GetMapListTask task = new GetMapListTask(null, null, null);

        List<String> mapList = new ArrayList<String>();
        mapList.add("Meine Karte");
        mapList.add("tarent-floor-1");
        mapList.add("tarent-floor-3");
        mapList.add("30c3-floor-4");
        mapList.add("tarent-floor-2");
        mapList.add("30c3-floor-2");
        mapList.add("30c3-floor-3");
        mapList.add("30c3-floor-0");
        mapList.add("dance-floor"); // This one is missing a floor-number => it's not a multimap-level
        mapList.add("30c3-floor-1");
        mapList.add("foo-floor-9"); // Single levels should not appear as multimaps

        List<String> expectedMapList = new ArrayList<String>(mapList);
        expectedMapList.add("30c3 [0, 1, 2, 3, 4]");
        expectedMapList.add("tarent [1, 2, 3]");

        task.makeMultilevelMaps(mapList);

        assertEquals(expectedMapList.size(), mapList.size());
        assertTrue(mapList.containsAll(expectedMapList));
    }

    @Test
    public void testEmptyMapList() {
        GetMapListTask task = new GetMapListTask(null, null, null);

        List<String> mapList = new ArrayList<String>();

        task.makeMultilevelMaps(mapList);

        assertEquals(0, mapList.size());
    }

    @Test
    public void testNoMultiLevels() {
        GetMapListTask task = new GetMapListTask(null, null, null);

        List<String> mapList = new ArrayList<String>();
        mapList.add("Meine Karte");
        mapList.add("dance-floor");

        List<String> expectedMapList = new ArrayList<String>(mapList);

        task.makeMultilevelMaps(mapList);

        assertEquals(expectedMapList.size(), mapList.size());
        assertTrue(mapList.containsAll(expectedMapList));
    }
}
