package de.tarent.nic.android.base.config;

/**
 * This {@link ValueProvider} simply returns the default value of the {@link Property} which are configured in the
 * {@link Property} enum.
 * <p/>
 * Dieser {@link ValueProvider} liefert ausschließlich die
 * Standard-Werte einer {@link Property}. Die Standard-Werte
 * werden in der {@link Property} selbst konfiguriert.
 *
 * @author Sven Schumann, <s.schumann@tarent.de>
 */
public class DefaultValueProvider extends AbstractValueProvider {

    /**
     * {@inheritDoc}
     * <p/>
     * Should there be no property, return null. Else return the {@link Property} default value.
     */
    public String getPropertyValue(final Property property) {
        if (property == null) {
            return null;
        }
        return property.getDefaultValue();
    }
}
