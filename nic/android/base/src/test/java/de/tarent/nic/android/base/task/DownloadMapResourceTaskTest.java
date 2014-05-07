package de.tarent.nic.android.base.task;

import de.tarent.nic.android.base.map.IndoorMap;
import de.tarent.nic.mapserver.MapServerClient;
import de.tarent.nic.mapserver.MapServerClientImpl;
import de.tarent.nic.mapserver.exception.NicException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class DownloadMapResourceTaskTest {

    public MapServerClient clientMock;

    public MapView mapViewMock;

    public IndoorMap indoorMapMock;

    public String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
"        <TileMap version=\"1.0.0\" tilemapservice=\"http://tms.osgeo.org/1.0.0\">"+
"          <Title>temp5035436153991530048.tif</Title>"+
"          <Abstract></Abstract>"+
"          <SRS>EPSG:900913</SRS>"+
"          <BoundingBox minx=\"50.72140993719504\" miny=\"7.06091000000000\" maxx=\"50.72266000000001\" maxy=\"7.06220938704193\"/>"+
"          <Origin x=\"50.72140993719504\" y=\"7.06091000000000\"/>"+
"          <TileFormat width=\"256\" height=\"256\" mime-type=\"image/png\" extension=\"png\"/>"+
"          <TileSets profile=\"mercator\">"+
"            <TileSet href=\"17\" units-per-pixel=\"1.19432856674194\" order=\"17\"/>"+
"            <TileSet href=\"18\" units-per-pixel=\"0.59716428337097\" order=\"18\"/>"+
"            <TileSet href=\"19\" units-per-pixel=\"0.29858214168549\" order=\"19\"/>"+
"            <TileSet href=\"20\" units-per-pixel=\"0.14929107084274\" order=\"20\"/>"+
"            <TileSet href=\"21\" units-per-pixel=\"0.07464553542137\" order=\"21\"/>"+
"          </TileSets>"+
"        </TileMap>";


    @Before
    public void setup() {
        clientMock = mock(MapServerClientImpl.class);
        mapViewMock = mock(MapView.class);
        indoorMapMock = mock(IndoorMap.class);
    }

    @Test
    public void testEmptyXMLMapView() throws NicException, IOException {
        when(clientMock.getTilemapresourceXml("myMap")).thenReturn("");

        DownloadMapResourceTask task = new DownloadMapResourceTask(null, clientMock, mapViewMock, "myMap");
        task.doInBackground();

        verifyZeroInteractions(mapViewMock);
        verifyZeroInteractions(indoorMapMock);
    }

    @Test
    public void testEmptyXMLIndoorMap() throws NicException, IOException {
        when(clientMock.getTilemapresourceXml("myMap")).thenReturn("");

        DownloadMapResourceTask task = new DownloadMapResourceTask(null, clientMock, "myMap", indoorMapMock, null);
        task.doInBackground();

        verifyZeroInteractions(indoorMapMock);
    }

    @Test
    public void testValidXMLIndoorMap() throws NicException, IOException {
        when(clientMock.getTilemapresourceXml("myMap")).thenReturn(xml);

        ArgumentCaptor<BoundingBoxE6> boxCaptor = ArgumentCaptor.forClass(BoundingBoxE6.class);

        DownloadMapResourceTask task = new DownloadMapResourceTask(null, clientMock, "myMap", indoorMapMock, null);
        task.doInBackground();

        int z =17+DownloadMapResourceTask.MIN_ZOOM_LEVEL_PLUS;
        verify(indoorMapMock).setZoomLevels(17+DownloadMapResourceTask.MIN_ZOOM_LEVEL_PLUS, 21);
        BoundingBoxE6 boxIn = new BoundingBoxE6(50722660, 7062209, 50721409, 7060910);
        verify(indoorMapMock).setBoundingBox(boxCaptor.capture());
        BoundingBoxE6 boxOut = boxCaptor.getValue();
        assertEquals(boxIn.getLatNorthE6(), boxOut.getLatNorthE6());
        assertEquals(boxIn.getLonEastE6(), boxOut.getLonEastE6());
        assertEquals(boxIn.getLatSouthE6(), boxOut.getLatSouthE6());
        assertEquals(boxIn.getLonWestE6(), boxOut.getLonWestE6());
    }

}
