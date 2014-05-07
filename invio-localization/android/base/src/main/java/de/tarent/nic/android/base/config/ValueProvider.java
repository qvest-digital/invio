package de.tarent.nic.android.base.config;

/**
 * The {@link ValueProvider} returns a value based on the {@link Property}.
 *
 * @author Sven Schumann, <s.schumann@tarent.de>
 */
public interface ValueProvider {

    /**
     * Returns the value of the {@link Property}.
     *
     * @param property the {@link Property}
     * @return the {@link Property} value
     */
    String getPropertyValue(final Property property);

    /**
     * Returns the value of the {@link Property} as an {@link Integer}.
     *
     * @param property the {@link Property}
     * @return the {@link Property} value
     */
    Integer getPropertyValueAsInt(final Property property);
}
