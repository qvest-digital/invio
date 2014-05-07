package de.tarent.nic.android.base.wifi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import de.tarent.nic.android.base.AbstractMapActivity;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.mapserver.exception.NicException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.osmdroid.views.MapView;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MultiLevelFingerprintManagerTest {

    MultiLevelFingerprintManager manager;

    AbstractMapActivity activity;

    MapServerClient client;

    MapView mapView;

    String fingerprintsJson = "[{\"histogram\":{\"00:1f:3f:12:30:32\":{\"-93\":0.2,\"-91\":0.2,\"-89\":0.2,\"-88\":0.4},\"00:1b:2f:f3:be:8a\":{\"-89\":0.75,\"-88\":0.25},\"bc:05:43:7b:0b:8a\":{\"-92\":0.5,\"-90\":0.5},\"00:0b:3b:58:0e:c0\":{\"-90\":1.0},\"44:32:c8:30:6d:7b\":{\"-88\":0.2,\"-87\":0.8},\"00:23:08:e7:ad:5f\":{\"-57\":0.2,\"-55\":0.8},\"24:65:11:64:1d:37\":{\"-92\":0.2,\"-90\":0.8},\"bc:05:43:12:a2:27\":{\"-87\":0.2,\"-86\":0.2,\"-85\":0.6},\"00:1c:28:56:08:fb\":{\"-92\":0.2,\"-91\":0.2,\"-87\":0.6},\"9c:c7:a6:39:fa:ca\":{\"-89\":0.2,\"-88\":0.4,\"-87\":0.2,\"-86\":0.2},\"74:31:70:1b:c8:97\":{\"-87\":1.0},\"84:1b:5e:75:18:c2\":{\"-93\":0.2,\"-92\":0.6,\"-90\":0.2},\"f0:7d:68:50:ac:ac\":{\"-71\":0.4,\"-70\":0.6},\"00:1c:28:20:62:fa\":{\"-93\":1.0}},\"id\":\"my histo 1\",\"point\":{\"mAltitude\":0,\"mLatitudeE6\":-77823323,\"mLongitudeE6\":-45878906}},{\"histogram\":{\"00:1f:3f:12:30:32\":{\"-91\":0.2,\"-90\":0.2,\"-87\":0.4,\"-85\":0.2},\"00:1b:2f:f3:be:8a\":{\"-89\":0.6,\"-88\":0.4},\"bc:05:43:7b:0b:8a\":{\"-92\":1.0},\"00:0b:3b:58:0e:c0\":{\"-90\":1.0},\"44:32:c8:30:6d:7b\":{\"-90\":0.2,\"-86\":0.8},\"20:2b:c1:f4:c1:71\":{\"-93\":1.0},\"00:23:08:e7:ad:5f\":{\"-58\":0.4,\"-57\":0.6},\"24:65:11:64:1d:37\":{\"-93\":0.2,\"-92\":0.2,\"-91\":0.4,\"-90\":0.2},\"bc:05:43:12:a2:27\":{\"-86\":0.8,\"-84\":0.2},\"00:1c:28:56:08:fb\":{\"-94\":0.2,\"-92\":0.2,\"-91\":0.2,\"-87\":0.4},\"9c:c7:a6:39:fa:ca\":{\"-89\":0.4,\"-87\":0.6},\"84:1b:5e:75:18:c2\":{\"-93\":0.2,\"-92\":0.2,\"-91\":0.2,\"-89\":0.4},\"f0:7d:68:50:ac:ac\":{\"-71\":0.8,\"-70\":0.2},\"00:1c:28:20:62:fa\":{\"-93\":1.0}},\"id\":\"my histo 2\",\"point\":{\"mAltitude\":0,\"mLatitudeE6\":-64997939,\"mLongitudeE6\":-59414062}},{\"histogram\":{\"00:1f:3f:12:30:32\":{\"-93\":0.2,\"-92\":0.4,\"-91\":0.2,\"-88\":0.2},\"00:1b:2f:f3:be:8a\":{\"-89\":1.0},\"bc:05:43:7b:0b:8a\":{\"-90\":1.0},\"44:32:c8:30:6d:7b\":{\"-86\":0.6,\"-85\":0.4},\"20:2b:c1:f4:c1:71\":{\"-93\":1.0},\"00:23:08:e7:ad:5f\":{\"-57\":0.8,\"-56\":0.2},\"24:65:11:64:1d:37\":{\"-89\":1.0},\"bc:05:43:12:a2:27\":{\"-86\":0.6,\"-85\":0.2,\"-84\":0.2},\"00:1c:28:56:08:fb\":{\"-91\":0.33333334,\"-88\":0.33333334,\"-87\":0.33333334},\"9c:c7:a6:39:fa:ca\":{\"-88\":0.2,\"-87\":0.2,\"-86\":0.6},\"70:ca:9b:9a:2a:d0\":{\"-90\":1.0},\"74:31:70:1b:c8:97\":{\"-92\":0.8,\"-88\":0.2},\"84:1b:5e:75:18:c2\":{\"-94\":0.6,\"-91\":0.2,\"-90\":0.2},\"f0:7d:68:50:ac:ac\":{\"-71\":0.2,\"-70\":0.8},\"00:1c:28:20:62:fa\":{\"-93\":0.8,\"-88\":0.2}},\"id\":\"my histo 3\",\"point\":{\"mAltitude\":0,\"mLatitudeE6\":-65585720,\"mLongitudeE6\":-14062500}}]";


    @Before
    public void setup() throws IOException, NicException {
        activity = mock(AbstractMapActivity.class);
        client = mock(MapServerClient.class);
        mapView = mock(MapView.class);

        when(activity.getResources()).thenReturn(Robolectric.application.getResources());
        when(activity.getSystemService(activity.WIFI_SERVICE)).thenReturn(mock(WifiManager.class));
        when(client.downloadFingerprintsData(anyString())).thenReturn(fingerprintsJson);
        when(client.getTilemapresourceXml(anyString())).thenReturn("");

        //manager = new MultiLevelFingerprintManager(activity, client, mapView, "test", true);

        //manager.progressDialog = mock(ProgressDialog.class);

        //manager.createMaps();


/*
        // We need the Runnable that creates the ProgressDialog in order to execute it:
        ArgumentCaptor<Runnable> uiRunnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(activity).runOnUiThread(uiRunnableArgumentCaptor.capture());
        Runnable uiRunnable = uiRunnableArgumentCaptor.getValue();
        // We have no uiThread so we call the Runnable ourselves.
        uiRunnable.run();
        */
    }

    @Test
    @Ignore
    public void testSetValidLevel() {

        manager.setLevel(2);
        assertEquals(2, manager.getLevel());

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);


        verify(activity).runOnUiThread(runnableCaptor.capture());
        List<Runnable> runnables = runnableCaptor.getAllValues();

        int i=0;
    }

    @Test
    @Ignore
    public void testSetInvalidLevel() {
        manager.setLevel(-100);
        assertEquals(2, manager.getLevel());

        manager.setLevel(100);
        assertEquals(2, manager.getLevel());
    }

}
