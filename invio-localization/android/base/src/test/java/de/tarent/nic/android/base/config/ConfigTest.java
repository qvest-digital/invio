package de.tarent.nic.android.base.config;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class ConfigTest {

    Config toTest;

    @Before
    public void setup() {
        toTest = spy(Config.getInstance());

        toTest.initProviderChain();
        testProviderChain();
    }

    public void testProviderChain() {
        assertTrue(toTest.providerChain.size() >= 1);
        assertTrue(toTest.providerChain.get(0) instanceof DefaultValueProvider);
    }

    @Test
    public void setContext() {
        toTest.setContext(mock(Context.class));

        testProviderChain();
        assertTrue(toTest.providerChain.size() >= 2);
        assertTrue(toTest.providerChain.get(1) instanceof ResourceValueProvider);
    }

    @Test
    public void get_null() {
        assertNull(toTest.getPropertyValue(null));
    }

    @Test
    public void get_chainLogic() {
        final String first = "first";
        final String second = "second";

        toTest.providerChain.clear();
        toTest.providerChain.add(new AbstractValueProvider() {
            public String getPropertyValue(Property property) {
                return first;    //ich komm nicht zum zuge...
            }
        });
        toTest.providerChain.add(new AbstractValueProvider() {
            public String getPropertyValue(Property property) {
                return second;    //der erste der etwas liefert gewinnt...
            }
        });
        toTest.providerChain.add(new AbstractValueProvider() {
            public String getPropertyValue(Property property) {
                return null;    //liefert null (deswegen wird der nächste versucht)
            }
        });

        assertEquals(second, toTest.getPropertyValue(Property.MAP_PROVIDER_SCHEMA));
    }

    @Test
    public void getInt_chainLogic() {
        toTest.providerChain.clear();

        //gleicher aufbau wie get_chainLogic()
        for (int i = 0; i < 3; i++) {
            toTest.providerChain.add(mock(ValueProvider.class));
            if (i != 2) {
                doReturn(i).when(toTest.providerChain.get(i)).getPropertyValueAsInt(any(Property.class));
            } else {
                doReturn(null).when(toTest.providerChain.get(i)).getPropertyValueAsInt(any(Property.class));
            }
        }

        assertTrue(1 == toTest.getPropertyValueAsInt(Property.MAP_PROVIDER_SCHEMA));
    }

    @Test
    public void get_emptyChain() {
        toTest.providerChain.clear();

        assertNull(toTest.getPropertyValue(null));
    }

    @Test
    public void getInt_emptyChain() {
        toTest.providerChain.clear();

        assertNull(toTest.getPropertyValueAsInt(null));
    }
}
