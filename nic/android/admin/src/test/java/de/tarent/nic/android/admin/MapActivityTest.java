package de.tarent.nic.android.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import de.tarent.nic.android.base.particlefilter.ParticleFilter;
import de.tarent.nic.android.base.wifi.UserLocator;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPreferenceManager;
import org.robolectric.tester.org.apache.http.RequestMatcher;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@Config(manifest = "../admin/AndroidManifest.xml", shadows = {CustomShadowZoomButtonsController.class})
@RunWith(RobolectricTestRunner.class)
public class MapActivityTest {

    // Extended MapActivity because we need to set our mock UserLocator to it. For explanation see the TestMapActivity
    // class description.
    private MapActivityWrapper activity;

    // In several tests we must verify that some methods of UserLocator were called
    private UserLocator userLocator;

    // Some tests are
    private SharedPreferences sharedPreferences;


    private Resources resources;

    @Before
    public void setup() {

        resources = Robolectric.application.getResources();

        // During the creation of the test activity (MapActivity) some http-requests are sent to get the
        // tilemapresource.xml, the fingerprints and other data from the map server. Robolectric gives us the
        //  ability to fake the responses to these requests and put our data into the response body.

        // Respond to the tilemapdatasource.xml request with real data
        Robolectric.addHttpResponseRule(getTileMapResourceRequestMatcher(), getTileMapResourceResponse());

        // This line is required because Robolectric will inform us about unexpected requests via System.err
        Robolectric.addHttpResponseRule(getGeneralMapDataMatcher(), getGeneralMapDataResponse());

        // For any other unexpected requests response with 400 BAD REQUEST
        Robolectric.setDefaultHttpResponse(400, "Bad Request");

        // Create an intent and put the map name into its extras, but remember that we fake the responses so
        // it will not call the real server anyway and the map name can be whatever.
        Intent intent = new Intent(Robolectric.getShadowApplication().getApplicationContext(), MapActivity.class);
        intent.putExtra("MapName", "foo");

        //Create the activity and set our mock UserLocator to it
        activity = Robolectric.buildActivity(MapActivityWrapper.class).withIntent(intent).create().get();
        userLocator = mock(UserLocator.class);
        activity.setUserLocator(userLocator);

        sharedPreferences = ShadowPreferenceManager.getDefaultSharedPreferences(Robolectric.application.getApplicationContext());
        sharedPreferences.edit().clear().commit();

        ParticleFilter pf = mock(ParticleFilter.class);
        when(userLocator.getParticleFilter()).thenReturn(pf);
    }


    @Test
    public void testSettings_Localization_ON_Default_ParticleFilter() {
        // The default algorithm is ParticleFilter, so we don't need to set it in the ShadowPreferenceManager
        activity.onResume();
        verify(userLocator, times(1)).setLocalizationMethod(eq(UserLocator.LOCALIZATION_MODE_PARTICLEFILTER));
        verify(userLocator, never()).setLocalizationMethod(eq(UserLocator.LOCALIZATION_MODE_WIFI));
        verify(userLocator, never()).setLocalizationMethod(eq(UserLocator.LOCALIZATION_MODE_DEADRECKONING));
    }

    @Test
    public void testSettings_Localization_ON_Wifi() {
        // Set the localization to Wifi
        String localizationListKey = resources.getString(R.string.key_pref_localizationMethodList);
        String wifiAlgKey = resources.getString(R.string.localizationMethodWifi);
        sharedPreferences.edit().putString(localizationListKey, wifiAlgKey).commit();
        activity.onResume();
        verify(userLocator, times(1)).setLocalizationMethod(eq(UserLocator.LOCALIZATION_MODE_WIFI));
        verify(userLocator, never()).setLocalizationMethod(eq(UserLocator.LOCALIZATION_MODE_PARTICLEFILTER));
        verify(userLocator, never()).setLocalizationMethod(eq(UserLocator.LOCALIZATION_MODE_DEADRECKONING));
    }

    @Test
    public void testSettings_Localization_ON_DeadReckoning() {
        // Set the localization to DeadReckoning
        String localizationListKey = resources.getString(R.string.key_pref_localizationMethodList);
        String deadreckoningAlgKey = resources.getString(R.string.localizationMethodDeadreckoning);
        sharedPreferences.edit().putString(localizationListKey, deadreckoningAlgKey).commit();
        activity.onResume();
        verify(userLocator, times(1)).setLocalizationMethod(eq(UserLocator.LOCALIZATION_MODE_DEADRECKONING));
        verify(userLocator, never()).setLocalizationMethod(eq(UserLocator.LOCALIZATION_MODE_PARTICLEFILTER));
        verify(userLocator, never()).setLocalizationMethod(eq(UserLocator.LOCALIZATION_MODE_WIFI));
    }

    @Test
    public void testSettings_MapMatching_ON_OFF() {
        String prefMapMatchingToggle = resources.getString(R.string.key_pref_mapMatching_toggle);

        sharedPreferences.edit().putBoolean(prefMapMatchingToggle, true).commit();
        activity.onResume();
        verify(userLocator, times(1)).setMapMatchingMode(eq(UserLocator.MAP_MATCHING_MODE_SIMPLE_WAY_SNAP));
        verify(userLocator, never()).setMapMatchingMode(eq(UserLocator.MAP_MATCHING_MODE_NONE));

        sharedPreferences.edit().putBoolean(prefMapMatchingToggle, false).commit();
        activity.onResume();
        verify(userLocator, times(1)).setMapMatchingMode(eq(UserLocator.MAP_MATCHING_MODE_NONE));
    }

    @Test
    public void testSettings_WayOverlay_ON_OFF() {
        String prefShowWayOverlay = resources.getString(R.string.key_pref_mapMatching_showWayOverlay);

        sharedPreferences.edit().putBoolean(prefShowWayOverlay, true).commit();
        activity.onResume();
        assertTrue(activity.getMapView().getOverlays().contains(activity.wayOverlay));

        sharedPreferences.edit().putBoolean(prefShowWayOverlay, false).commit();
        activity.onResume();
        assertFalse(activity.getMapView().getOverlays().contains(activity.wayOverlay));
    }

    @Test
    public void testSettings_OutlierDetection_ON_Default_CME2() {
        String prefOutlierKey = resources.getString(R.string.key_pref_outlierEnabled);
        sharedPreferences.edit().putBoolean(prefOutlierKey, true).commit();

        // The default algorithm is CME, so we don't need to set it in the ShadowPreferenceManager
        activity.onResume();
        verify(userLocator, times(1)).setOutlierMode(eq(UserLocator.OUTLIER_MODE_CME));
        verify(userLocator, never()).setOutlierMode(eq(UserLocator.OUTLIER_MODE_PLASMONA));
        verify(userLocator, never()).setOutlierMode(eq(UserLocator.OUTLIER_MODE_NO_DETECTION));
    }

    @Test
    public void testSettings_OutlierDetection_ON_Plasmona() {
        String prefOutlierKey = resources.getString(R.string.key_pref_outlierEnabled);

        sharedPreferences.edit().putBoolean(prefOutlierKey, true).commit();
        // Set the algorithm to plasmona
        String outlierListKey = resources.getString(R.string.key_pref_outlierAlgorithmList);
        String plasmonaAlgKey = resources.getString(R.string.outlierAlgorithmPlasmona);
        sharedPreferences.edit().putString(outlierListKey, plasmonaAlgKey).commit();
        activity.onResume();
        verify(userLocator, times(1)).setOutlierMode(eq(UserLocator.OUTLIER_MODE_PLASMONA));
        verify(userLocator, never()).setOutlierMode(eq(UserLocator.OUTLIER_MODE_CME));
        verify(userLocator, never()).setOutlierMode(eq(UserLocator.OUTLIER_MODE_NO_DETECTION));
    }

    @Test
    public void testSettings_OutlierDetection_OFF() {
        // If preferences key does not exist SharedPreferences will return a default value for this key, which is false
        // in our case, so we don't need to set it here.
        activity.onResume();
        verify(userLocator, times(1)).setOutlierMode(eq(UserLocator.OUTLIER_MODE_NO_DETECTION));
        verify(userLocator, never()).setOutlierMode(eq(UserLocator.OUTLIER_MODE_CME));
        verify(userLocator, never()).setOutlierMode(eq(UserLocator.OUTLIER_MODE_PLASMONA));
    }

    @Test
    public void testSettings_StatisticFilter_ON_Default_Median() {
        String prefFilterKey = resources.getString(R.string.key_pref_filteringEnabled);
        sharedPreferences.edit().putBoolean(prefFilterKey, true).commit();

        // The default filter is median, so we don't need to set it in the ShadowPreferenceManager
        activity.onResume();
        verify(userLocator, times(1)).setStatisticFilterMode(eq(UserLocator.STATISTIC_FILTER_MODE_MEDIAN));
        verify(userLocator, never()).setStatisticFilterMode(eq(UserLocator.STATISTIC_FILTER_MODE_AVERAGE));
        verify(userLocator, never()).setStatisticFilterMode(eq(UserLocator.STATISTIC_FILTER_MODE_NO_FILTER));
    }

    @Test
    public void testSettings_StatisticFilter_ON_Average() {
        String prefFilterKey = resources.getString(R.string.key_pref_filteringEnabled);

        sharedPreferences.edit().putBoolean(prefFilterKey, true).commit();
        // Set the algorithm to plasmona
        String filterListKey = resources.getString(R.string.key_pref_filterAlgorithmList);
        String averageKey = resources.getString(R.string.filterAlgorithmAverage);
        sharedPreferences.edit().putString(filterListKey, averageKey).commit();
        activity.onResume();
        verify(userLocator, times(1)).setStatisticFilterMode(eq(UserLocator.STATISTIC_FILTER_MODE_AVERAGE));
        verify(userLocator, never()).setStatisticFilterMode(eq(UserLocator.STATISTIC_FILTER_MODE_MEDIAN));
        verify(userLocator, never()).setStatisticFilterMode(eq(UserLocator.STATISTIC_FILTER_MODE_NO_FILTER));
    }

    @Test
    public void testSettings_StatisticFilter_OFF() {
        // If preferences key does not exist SharedPreferences will return a default value for this key, which is false
        // in our case, so we don't need to set it here.
        activity.onResume();
        verify(userLocator, times(1)).setStatisticFilterMode(eq(UserLocator.STATISTIC_FILTER_MODE_NO_FILTER));
        verify(userLocator, never()).setStatisticFilterMode(eq(UserLocator.STATISTIC_FILTER_MODE_AVERAGE));
        verify(userLocator, never()).setStatisticFilterMode(eq(UserLocator.STATISTIC_FILTER_MODE_MEDIAN));
    }


    @Test
    public void testSettings_LowPassFilter_ON() {
        String prefLowPassFilterKey = resources.getString(R.string.key_pref_deadReckoning_lowPassToggle);

        sharedPreferences.edit().putBoolean(prefLowPassFilterKey, true).commit();
        activity.onResume();
        verify(userLocator, times(1)).setCompassFilterEnabled(eq(true));
        verify(userLocator, never()).setCompassFilterEnabled(eq(false));
    }

    @Test
    public void testSettings_LowPassFilter_OFF() {
        activity.onResume();
        verify(userLocator, times(1)).setCompassFilterEnabled(eq(false));
        verify(userLocator, never()).setCompassFilterEnabled(eq(true));
    }

    @Test
    public void testSettings_ShowParticles_ON() {
        String prefShowParticlesKey = resources.getString(R.string.key_pref_showparticles);

        sharedPreferences.edit().putBoolean(prefShowParticlesKey, true).commit();
        activity.onResume();
        verify(userLocator.getParticleFilter(), times(1)).setShowParticles(eq(true));
        verify(userLocator.getParticleFilter(), never()).setShowParticles(eq(false));
    }

    @Test
    public void testSettings_ShowParticles_OFF() {
        activity.onResume();
        verify(userLocator.getParticleFilter(), times(1)).setShowParticles(eq(false));
        verify(userLocator.getParticleFilter(), never()).setShowParticles(eq(true));
    }


    /**
     * Get the specific URI RequestMatcher for the "tilemapresource.xml" HttpRequest.
     *
     * @return RequestMatcher returning true if the URI ends with "tilemapresource.xml" or false otherwise
     */
    private RequestMatcher getTileMapResourceRequestMatcher() {
        RequestMatcher tileMapResourceRequestMatcher = new RequestMatcher() {
            @Override
            public boolean matches(HttpRequest request) {
                if (request.getRequestLine().getUri().endsWith("tilemapresource.xml")) {
                    return true;
                }

                return false;
            }
        };

        return tileMapResourceRequestMatcher;
    }

    /**
     * Create HttpResponse with status 200 and the real "tilemapresource.xml" in the response body.
     *
     * @return HttpResponse containing "tilemapresource.xml"
     */
    private HttpResponse getTileMapResourceResponse() {
        ProtocolVersion httpProtocolVersion = new ProtocolVersion("HTTP", 1, 1);
        HttpResponse tileMapResourceResponse =
                new BasicHttpResponse(httpProtocolVersion, 200, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "\t<TileMap version=\"1.0.0\" tilemapservice=\"http://tms.osgeo.org/1.0.0\">\n" +
                        "\t  <Title>tarentTest1OG.tif</Title>\n" +
                        "\t  <Abstract></Abstract>\n" +
                        "\t  <SRS>EPSG:900913</SRS>\n" +
                        "\t  <BoundingBox minx=\"50.72138595889053\" miny=\"7.06105290000001\" maxx=\"50.72245270000001\" maxy=\"7.06215692919631\"/>\n" +
                        "\t  <Origin x=\"50.72138595889053\" y=\"7.06105290000001\"/>\n" +
                        "\t  <TileFormat width=\"256\" height=\"256\" mime-type=\"image/png\" extension=\"png\"/>\n" +
                        "\t  <TileSets profile=\"mercator\">\n" +
                        "\t    <TileSet href=\"17\" units-per-pixel=\"1.19432856674194\" order=\"17\"/>\n" +
                        "\t    <TileSet href=\"18\" units-per-pixel=\"0.59716428337097\" order=\"18\"/>\n" +
                        "\t    <TileSet href=\"19\" units-per-pixel=\"0.29858214168549\" order=\"19\"/>\n" +
                        "\t    <TileSet href=\"20\" units-per-pixel=\"0.14929107084274\" order=\"20\"/>\n" +
                        "\t    <TileSet href=\"21\" units-per-pixel=\"0.07464553542137\" order=\"21\"/>\n" +
                        "\t  </TileSets>\n" +
                        "\t</TileMap>\n");

        return tileMapResourceResponse;
    }

    private RequestMatcher getGeneralMapDataMatcher() {
        RequestMatcher generalMapDataMatcher = new RequestMatcher() {
            @Override
            public boolean matches(HttpRequest request) {
                String uri = request.getRequestLine().getUri();
                if (uri.endsWith(".osm") || uri.endsWith(".json")) {
                    return true;
                }

                return false;
            }
        };

        return generalMapDataMatcher;
    }

    private HttpResponse getGeneralMapDataResponse() {
        ProtocolVersion httpProtocolVersion = new ProtocolVersion("HTTP", 1, 1);
        HttpResponse generalMapDataResponse =
                new BasicHttpResponse(httpProtocolVersion, 400, "BAD REQUEST");

        return generalMapDataResponse;
    }
}
