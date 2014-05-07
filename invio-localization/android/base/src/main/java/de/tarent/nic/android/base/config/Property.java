package de.tarent.nic.android.base.config;

import de.tarent.nic.android.base.R;

/**
 * Eine {@link Property} dient als Referenz. Sie besteht mindestens
 * aus einer Resource-Id und ggf. aus einem Standardwert.
 * <p/>
 * A {@link Property} serves as a reference. It is made up of a resource id and a standard value when necessary.
 *
 * @author Sven Schumann, <s.schumann@tarent.de>
 */
public enum Property {
    MAP_PROVIDER_SCHEMA(R.string.map_provider_url_schema),
    MAP_TILE_SIZE_PIXEL(R.string.map_tile_size_px, "256"),
    MAP_DEFAULT_NAME(R.string.map_default_name),
    MAP_ZOOM_MIN(R.string.map_zoom_min, "0"),
    MAP_ZOOM_MAX(R.string.map_zoom_max, "0"),
    MAP_FILE_ENDING(R.string.map_file_ending, ".jpg");
    private final int resourceId;
    private final String defaultValue;

    private Property(final int resourceId, final String defaultValue) {
        this.resourceId = resourceId;
        this.defaultValue = defaultValue;
    }

    private Property(final int resourceId) {
        this(resourceId, null);
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
