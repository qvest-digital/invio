package de.tarent.nic.android.base;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.TestCase.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * @author Désirée Amling <d.amling@tarent.de>
 */
@RunWith(RobolectricTestRunner.class) // RunWith the RobolectricTestRunner.
public class AbstractMapSelectionActivityTest {

    private TestAbstractMapSelectionActivity activity;

    @Before
    public void setup() {
        // We have to make sure that Robolectric knows that it's awaiting an HTTPResponse.
        Robolectric.addPendingHttpResponse(200, "OK");

        // We have to tell Robolectric to not run any background tasks (AsyncTasks) because we do not have an actual
        // activity.
        Robolectric.getBackgroundScheduler().pause();

        // The activity is created with Robolectric.
        activity = Robolectric.buildActivity(TestAbstractMapSelectionActivity.class).create().get();
    }

    @Test
    public void testOnListItemClickStartsNicActivity() {
        final AbstractMapSelectionActivity activitySpy = spy(activity);
        doNothing().when(activitySpy).startActivity(any(Intent.class));

        final ListView listView = mock(ListView.class);
        final TextView view = mock(TextView.class);

        when(view.getText()).thenReturn("test");

        activitySpy.onListItemClick(listView, view, 0, 0L);

        verify(activitySpy, times(1)).startActivity(any(Intent.class));
    }

    @Test
    public void testIsNetWorkAvailableReturnsFalseWhenNoNetworkIsAvailable() {
        final AbstractMapSelectionActivity activitySpy = spy(activity);
        final ConnectivityManager manager = mock(ConnectivityManager.class);
        final NetworkInfo networkInfo = mock(NetworkInfo.class);

        when(activitySpy.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        when(manager.getActiveNetworkInfo()).thenReturn(networkInfo);
        when(networkInfo.isConnected()).thenReturn(false);

        assertFalse(activitySpy.isNetworkAvailable());
    }

    @Test
    public void testIsNetWorkAvailableReturnsFalseWhenNetworkInfoIsNull() {
        final AbstractMapSelectionActivity activitySpy = spy(activity);
        final ConnectivityManager manager = mock(ConnectivityManager.class);
        final NetworkInfo networkInfo = null;

        when(activitySpy.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        when(manager.getActiveNetworkInfo()).thenReturn(networkInfo);

        assertFalse(activitySpy.isNetworkAvailable());
    }

    @Test
    public void testOnCreateDoesNotShowProgressDialogWhenNoNetworkIsAvailable() {
        final AbstractMapSelectionActivity activitySpy = spy(activity);
        final ConnectivityManager manager = mock(ConnectivityManager.class);
        final NetworkInfo networkInfo = mock(NetworkInfo.class);

        when(activitySpy.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        when(manager.getActiveNetworkInfo()).thenReturn(networkInfo);
        when(networkInfo.isConnected()).thenReturn(false);

        activitySpy.onCreate(new Bundle());

        verify(activitySpy, times(0)).showProgressDialog();
    }
}
