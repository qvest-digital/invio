package de.tarent.nic.android.base.config;

/**
 * An abstract {@link ValueProvider} that implements methods that are the same for most value providers.
 * <p/>
 * Abstrakter {@link ValueProvider} der Methoden implementiert,
 * die für die meisten {@link ValueProvider} gleich sind.
 *
 * @author Sven Schumann, <s.schumann@tarent.de>
 */
public abstract class AbstractValueProvider implements ValueProvider {

    /**
     * {@inheritDoc}
     * <p/>
     * Should there be no value or the value cannot be parsed, return null.
     */
    public Integer getPropertyValueAsInt(final Property property) {
        try {
            final String stringValue = getPropertyValue(property);
            return Integer.parseInt(stringValue);
        } catch (final RuntimeException e) {    //NOSONAR - ja, ich weis was ich tu
            return null;
        }
    }
}
