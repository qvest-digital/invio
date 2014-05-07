package de.tarent.nic.android.base.task;

import de.tarent.nic.android.base.TestAbstractMapSelectionActivity;
import de.tarent.nic.android.base.mapmatching.WayManager;
import de.tarent.nic.android.base.wifi.FingerprintManager;
import de.tarent.nic.mapserver.MapServerClient;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osmdroid.views.MapView;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DownloadFingerprintsTaskTest {

    @Mock
    public MapServerClient mapServerClient;

    @Mock
    public WayManager wayManager;

    public String json = "[{\"histogram\":{\"00:1b:8f:8a:82:90\":{\"-93\":0.2,\"-89\":0.2,\"-85\":0.4,\"-82\":0.2},\"00:21:d8:93:c2:31\":{\"-75\":0.2,\"-70\":0.2,\"-63\":0.6},\"00:21:d8:93:c2:30\":{\"-66\":0.8,\"-62\":0.2},\"00:17:0f:e7:e8:80\":{\"-79\":0.2,\"-76\":0.2,\"-75\":0.2,\"-70\":0.2,\"-69\":0.2},\"00:1c:57:41:75:51\":{\"-92\":1.0},\"00:1c:57:41:75:50\":{\"-93\":1.0},\"00:1a:30:31:8a:21\":{\"-95\":0.2,\"-93\":0.8},\"00:17:0f:e7:e8:81\":{\"-82\":0.2,\"-76\":0.2,\"-74\":0.2,\"-70\":0.4},\"00:1a:30:31:8a:20\":{\"-94\":1.0},\"00:1b:8f:8a:82:91\":{\"-89\":0.2,\"-88\":0.4,\"-86\":0.2,\"-81\":0.2},\"84:1b:5e:2d:ef:50\":{\"-89\":0.75,\"-60\":0.25},\"00:21:55:63:41:c0\":{\"-88\":1.0},\"00:21:55:63:41:c1\":{\"-87\":1.0},\"00:21:d8:9c:34:61\":{\"-72\":0.2,\"-71\":0.2,\"-67\":0.4,\"-66\":0.2},\"00:21:d8:9c:34:60\":{\"-72\":0.2,\"-71\":0.2,\"-65\":0.4,\"-64\":0.2},\"00:17:df:2d:6e:01\":{\"-92\":1.0},\"00:1a:a2:bf:5e:c0\":{\"-93\":0.2,\"-90\":0.4,\"-82\":0.4},\"00:17:df:2d:6e:00\":{\"-91\":1.0},\"00:1a:a2:c3:5e:80\":{\"-81\":1.0},\"00:1a:a2:bf:5e:c1\":{\"-94\":0.4,\"-92\":0.6},\"00:21:55:4e:28:91\":{\"-94\":1.0},\"00:21:55:4e:28:90\":{\"-93\":0.2,\"-92\":0.8},\"00:1a:30:31:ae:c1\":{\"-81\":0.4,\"-78\":0.4,\"-77\":0.2},\"00:1a:30:31:ae:c0\":{\"-81\":0.4,\"-78\":0.2,\"-75\":0.2,\"-70\":0.2}},\"id\":\"FP-1\",\"point\":{\"mAltitude\":0,\"mLatitudeE6\":50721512,\"mLongitudeE6\":7061621}},{\"histogram\":{\"00:1b:8f:8a:82:90\":{\"-87\":0.4,\"-83\":0.2,\"-82\":0.4},\"00:21:d8:93:c2:31\":{\"-78\":0.4,\"-77\":0.2,\"-71\":0.2,\"-69\":0.2},\"00:21:d8:93:c2:30\":{\"-73\":1.0},\"00:17:0f:e7:e8:80\":{\"-85\":0.2,\"-83\":0.2,\"-76\":0.4,\"-75\":0.2},\"00:1a:30:31:8a:21\":{\"-93\":1.0},\"00:17:0f:e7:e8:81\":{\"-86\":0.2,\"-85\":0.2,\"-84\":0.2,\"-79\":0.2,\"-77\":0.2},\"00:1a:30:31:8a:20\":{\"-94\":1.0},\"00:1b:8f:8a:82:91\":{\"-89\":0.2,\"-88\":0.2,\"-86\":0.2,\"-82\":0.2,\"-80\":0.2},\"84:1b:5e:2d:ef:50\":{\"-89\":1.0},\"00:21:d8:9c:34:61\":{\"-84\":0.4,\"-81\":0.2,\"-78\":0.2,\"-75\":0.2},\"00:21:d8:9c:34:60\":{\"-84\":0.2,\"-82\":0.4,\"-78\":0.2,\"-73\":0.2},\"00:1a:a2:bf:5e:c0\":{\"-93\":1.0},\"00:1a:a2:c3:5e:80\":{\"-81\":1.0},\"00:1a:a2:bf:5e:c1\":{\"-92\":1.0},\"00:21:55:4e:28:91\":{\"-94\":1.0},\"00:21:55:4e:28:90\":{\"-92\":1.0},\"00:1a:30:31:ae:c1\":{\"-93\":0.2,\"-90\":0.4,\"-89\":0.2,\"-86\":0.2},\"00:1a:30:31:ae:c0\":{\"-92\":0.2,\"-91\":0.2,\"-89\":0.4,\"-86\":0.2}},\"id\":\"FP-2\",\"point\":{\"mAltitude\":0,\"mLatitudeE6\":50721542,\"mLongitudeE6\":7061449}},{\"histogram\":{\"00:1b:8f:8a:82:90\":{\"-69\":0.2,\"-16\":0.4,\"-13\":0.2,\"-2\":0.2},\"00:21:d8:93:c2:31\":{\"-73\":0.2,\"-70\":0.4,\"-69\":0.2,\"-67\":0.2},\"00:21:d8:93:c2:30\":{\"-81\":1.0},\"00:17:0f:e7:e8:80\":{\"-79\":0.2,\"-76\":0.4,\"-75\":0.2,\"-72\":0.2},\"00:17:0f:e7:e8:81\":{\"-82\":0.2,\"-76\":0.2,\"-75\":0.4,\"-72\":0.2},\"00:1b:8f:8a:82:91\":{\"-91\":0.2,\"-90\":0.2,\"-88\":0.2,\"-84\":0.4},\"84:1b:5e:2d:ef:50\":{\"-92\":1.0},\"00:21:d8:9c:34:61\":{\"-82\":0.4,\"-81\":0.2,\"-78\":0.2,\"-76\":0.2},\"00:21:d8:9c:34:60\":{\"-84\":0.2,\"-83\":0.2,\"-82\":0.2,\"-79\":0.2,\"-77\":0.2},\"00:1a:a2:bf:5e:c0\":{\"-93\":1.0},\"00:1a:a2:bf:5e:c1\":{\"-93\":0.8,\"-92\":0.2},\"00:21:55:4e:28:90\":{\"-92\":1.0},\"00:1a:30:31:ae:c1\":{\"-90\":0.2,\"-89\":0.4,\"-86\":0.2,\"-84\":0.2},\"00:1a:30:31:ae:c0\":{\"-91\":0.2,\"-87\":0.4,\"-84\":0.2,\"-83\":0.2}},\"id\":\"FP-3\",\"point\":{\"mAltitude\":0,\"mLatitudeE6\":50721624,\"mLongitudeE6\":7061568}}]";

    public MapView mapViewMock;

    private TestAbstractMapSelectionActivity activity;

    private final String MAP_NAME = "test";

    private FingerprintManager fingerprintManager;

    @Before
    public void setUp() throws Exception {
        /*
            MOCKITO
         */
        MockitoAnnotations.initMocks(this);
        when(mapServerClient.downloadFingerprintsData(MAP_NAME)).thenReturn(json);

        mapViewMock = mock(MapView.class);

        /*
            ROBOLECTRIC
         */
        // We have to make sure that Robolectric knows that it's awaiting an HTTPResponse.
        Robolectric.addPendingHttpResponse(200, "OK");
        // We have to tell Robolectric to not run any background tasks (AsyncTasks) because we do not have an actual
        // activity.
        Robolectric.getBackgroundScheduler().pause();
        // The activity is created with Robolectric.
        activity = Robolectric.buildActivity(TestAbstractMapSelectionActivity.class).create().get();

        /*

         */
        fingerprintManager = new FingerprintManager(activity, mapServerClient, mapViewMock, MAP_NAME);
    }

    // Here we just check if the correct {@link String) is returned when requesting the input stream.
    @Test
    public void testGetInputStream() throws IOException {
        DownloadFingerprintsTask taskToTest = new DownloadFingerprintsTask(fingerprintManager, activity, mapServerClient, "test");

        InputStream result = taskToTest.getInputStream();

        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(result, stringWriter, "UTF-8");
        String resultString = stringWriter.toString();

        assertEquals(json, resultString);
    }

}
