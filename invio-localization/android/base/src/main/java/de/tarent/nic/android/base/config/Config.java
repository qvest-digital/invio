package de.tarent.nic.android.base.config;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds all of the {@link ValueProvider}s in a chain. It should always be used as the access point
 * to all of the values.
 * <p/>
 * Dieser {@link ValueProvider} vereinheitlicht alle
 * anderen {@link ValueProvider} zu einer Kette. Diese Klasse
 * sollte als Zugriffspunkt auf Werte genutzt werden!
 *
 * @author Sven Schumann, <s.schumann@tarent.de>
 */
public class Config implements ValueProvider { //NOSONAR - ich kann die klasse wegen den Tests nicht final machen...

    /**
     * The {@link Config} instance.
     */
    private static Config instance;

    /**
     * The order is extremely important. The last element in this list will be the first to be asked. If it does
     * not answer with a value, then the next will be asked.
     * <p/>
     * Die Reihenfolge ist sehr wichtig! Das letzte Element in dieser Liste
     * wird als erstes gefragt. Sollte dieser kein Wert liefern, wird der nächste
     * gefragt usw.
     */
    protected List<ValueProvider> providerChain;

    /**
     * Private constructor to prevent instantiation.
     */
    private Config() {
        initProviderChain();
    }

    /**
     * Gets the {@link #instance}.
     *
     * @return the {@link #instance}
     */
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    /**
     * Adds items to the {@link #providerChain}.
     */
    protected void initProviderChain() {
        providerChain = new ArrayList<ValueProvider>();
        providerChain.add(new DefaultValueProvider());
    }

    /**
     * Adds a {@link ResourceValueProvider} to the {@link #providerChain}.
     *
     * @param context the {@link Context}
     */
    public void setContext(final Context context) {
        initProviderChain();
        providerChain.add(new ResourceValueProvider(context));
    }

    /**
     * {@inheritDoc}
     * <p/>
     * If the value is not null, return the value.
     */
    public String getPropertyValue(final Property property) {
        for (int i = providerChain.size() - 1; i >= 0; i--) {
            final ValueProvider provider = providerChain.get(i);
            final String value = provider.getPropertyValue(property);

            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * If the value is not null, return the value.
     */
    public Integer getPropertyValueAsInt(final Property property) {
        for (int i = providerChain.size() - 1; i >= 0; i--) {
            final ValueProvider provider = providerChain.get(i);
            final Integer value = provider.getPropertyValueAsInt(property);

            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
