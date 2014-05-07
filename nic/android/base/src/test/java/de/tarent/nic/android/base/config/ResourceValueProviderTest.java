package de.tarent.nic.android.base.config;

import android.content.Context;
import android.content.res.Resources;
import de.tarent.nic.android.base.config.Property;
import de.tarent.nic.android.base.config.ResourceValueProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ResourceValueProviderTest {

    Context mockContext;
    ResourceValueProvider toTest;

    @Before
    public void setup() {
        mockContext = mock(Context.class);
        toTest = spy(new ResourceValueProvider(mockContext));
    }

    @Test
    public void get_ctxIsNull() {
        final Property p = Property.MAP_PROVIDER_SCHEMA;
        assertNull(new ResourceValueProvider(null).getPropertyValue(p));
    }

    @Test
    public void get_null() {
        assertNull(toTest.getPropertyValue(null));
    }

    @Test
    public void get() {
        Resources mockResources = mock(Resources.class);

        doAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                //return the first argument!
                return String.valueOf(invocation.getArguments()[0]);
            }
        }).when(mockResources).getString(anyInt());
        doReturn(mockResources).when(mockContext).getResources();

        final Property p = Property.MAP_PROVIDER_SCHEMA;
        assertEquals(String.valueOf(p.getResourceId()), toTest.getPropertyValue(p));
    }

    @Test
    public void getInt_null() {
        assertNull(toTest.getPropertyValueAsInt(null));
    }

    @Test
    public void getInt() {
        Resources mockResources = mock(Resources.class);

        doReturn("13").when(mockResources).getString(anyInt());
        doReturn(mockResources).when(mockContext).getResources();

        final Property p = Property.MAP_TILE_SIZE_PIXEL;
        assertTrue(13 == toTest.getPropertyValueAsInt(p));
        verify(mockResources).getString(eq(p.getResourceId()));
    }

}
