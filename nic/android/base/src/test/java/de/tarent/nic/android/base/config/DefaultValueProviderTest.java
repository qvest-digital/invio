package de.tarent.nic.android.base.config;

import de.tarent.nic.android.base.config.DefaultValueProvider;
import de.tarent.nic.android.base.config.Property;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DefaultValueProviderTest {

    DefaultValueProvider toTest;

    @Before
    public void setup() {
        toTest = new DefaultValueProvider();
    }

    @Test
    public void get_null() {
        assertNull(toTest.getPropertyValue(null));
    }

    @Test
    public void get() {
        final Property p = Property.MAP_PROVIDER_SCHEMA;
        assertEquals(p.getDefaultValue(), toTest.getPropertyValue(p));
    }

    @Test
    public void getInt_null() {
        assertNull(toTest.getPropertyValueAsInt(null));
    }

    @Test
    public void getInt() {
        final Property p = Property.MAP_TILE_SIZE_PIXEL;
        assertTrue(
                Integer.parseInt(p.getDefaultValue()) == toTest.getPropertyValueAsInt(p));
    }

}
