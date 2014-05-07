package de.tarent.nic.android.base.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.internal.runners.model.MultipleFailureException.assertEmpty;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class DownloadTaskTest {

    @Test
    public void testAddRemoveListeners() throws Throwable {
        TestableDownloadTask task = new TestableDownloadTask();

        assertEquals(0, task.getListeners().size());

        DownloadListener listener1 = mock(DownloadListener.class);
        DownloadListener listener2 = mock(DownloadListener.class);

        task.addDownloadListener(listener1);
        assertEquals(listener1, task.getListeners().get(0));

        task.addDownloadListener(listener2);
        assertEquals(2, task.getListeners().size());

        task.removeDownloadListener(listener1);
        assertEquals(listener2, task.getListeners().get(0));
        assertEquals(1, task.getListeners().size());

        task.removeDownloadListener(listener1); // Was already removed!
        assertEquals(1, task.getListeners().size());

        task.removeDownloadListener(listener2);
        assertEquals(0, task.getListeners().size());
    }

    @Test
    public void testCallingOfListeners() {
        TestableDownloadTask task = new TestableDownloadTask();
        DownloadListener listener1 = mock(DownloadListener.class);
        DownloadListener listener2 = mock(DownloadListener.class);
        task.addDownloadListener(listener1);
        task.addDownloadListener(listener2);

        String result = "Win!";
        task.setSuccess(true);
        task.onPostExecute(result);
        verify(listener1).onDownloadFinished(task, true, result);
        verify(listener2).onDownloadFinished(task, true, result);

        task.setSuccess(false);
        task.onPostExecute(null);
        verify(listener1).onDownloadFinished(task, false, null);
        verify(listener2).onDownloadFinished(task, false, null);


    }

    public class TestableDownloadTask extends DownloadTask {
        @Override
        protected Object doInBackground(Object... params) {
            return null;
        }
        public List getListeners() {
            return downloadListeners;
        }
        public void setSuccess(boolean success) {
            this.success = success;
        }
    }
}
