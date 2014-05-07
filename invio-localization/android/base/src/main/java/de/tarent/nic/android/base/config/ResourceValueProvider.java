package de.tarent.nic.android.base.config;

import android.content.Context;

/**
 * This {@link ResourceValueProvider} returns the values from the android resources. The resource ids of each
 * {@link Property} are then used as references.
 * <p/>
 * Dieser {@link ValueProvider} liefert die Werte aus den
 * Android-Resourcen. Dazu wird die Resourcen-Id der {@link Property}
 * als Referenz verwendet.
 *
 * @author Sven Schumann, <s.schumann@tarent.de>
 */
public class ResourceValueProvider extends AbstractValueProvider {
    private final Context context;

    /**
     * Constructor.
     *
     * @param context the {@link Context}
     */
    public ResourceValueProvider(final Context context) {
        this.context = context;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * If the {@link #context} or property are null, return null.
     */
    public String getPropertyValue(final Property property) {
        if (context == null || property == null) {
            return null;
        }
        return context.getResources().getString(property.getResourceId());
    }
}
