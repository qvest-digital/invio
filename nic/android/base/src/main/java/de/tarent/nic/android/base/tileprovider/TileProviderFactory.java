package de.tarent.nic.android.base.tileprovider;

import android.content.Context;
import de.tarent.nic.android.base.config.Config;
import de.tarent.nic.android.base.config.ValueProvider;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;

import static de.tarent.nic.android.base.config.Property.MAP_FILE_ENDING;
import static de.tarent.nic.android.base.config.Property.MAP_TILE_SIZE_PIXEL;
import static de.tarent.nic.android.base.config.Property.MAP_ZOOM_MAX;
import static de.tarent.nic.android.base.config.Property.MAP_ZOOM_MIN;

/**
 * This class provides various methods of building tile providers.
 *
 * @author Sven Schumann, <s.schumann@tarent.de>
 */
public class TileProviderFactory {

    private final Context context;
    private final ValueProvider config;

    /**
     * Constructor.
     *
     * @param context {@link Context}
     */
    public TileProviderFactory(final Context context) {
        this(context, Config.getInstance());
    }

    /**
     * Constructor.
     *
     * @param context the {@link Context}
     * @param config  the {@link ValueProvider}
     */
    public TileProviderFactory(final Context context, final ValueProvider config) {
        this.context = context;
        this.config = config;
    }

    /**
     * Build a new TileProvider that looks into the local filesystem for tiles.
     *
     * @param mapName The name of the map which is stored in the assets/&lt;mapName&gt; directory
     * @return the tile provider
     */
    public MapTileProviderBase buildAssetTileProvider(final String mapName) {
        final NicTileSource tileSource = getTileSource(mapName);
        final IRegisterReceiver registerReceiver = new SimpleRegisterReceiver(context);
        final AssetTileProvider assetProvider = new AssetTileProvider(
                registerReceiver, tileSource, context.getAssets()
        );

        final MapTileProviderArray mapTileProviderArray = new MapTileProviderArray(tileSource, registerReceiver,
                new MapTileModuleProviderBase[]{
                        //here you can configure more than one provider...
                        assetProvider
                });
        return mapTileProviderArray;
    }

    /**
     * Build a new TileProvider that download the tiles from a server. These tiles
     * will be stored (cached) in the local file system.
     *
     * @param urlSchema the url schema for the map tiles
     * @param mapName   The name of the requested map.
     * @return the tile provider
     */
    public MapTileProviderBase buildWebTileProvider(final String urlSchema, final String mapName) {
        final ITileSource tileSource = getNicTileSource(urlSchema, mapName);
        final MapTileDownloader downloaderProvider = getMapTileDownloader(tileSource);
        final IRegisterReceiver registerReceiver = new SimpleRegisterReceiver(context);
        final MapTileFilesystemProvider fileSystemProvider =
                new MapTileFilesystemProvider(registerReceiver, tileSource);
        final MapTileProviderArray mapTileProviderArray = new MapTileProviderArray(tileSource, registerReceiver,
                new MapTileModuleProviderBase[]{
                        fileSystemProvider,    //erst wird der cache verwendet
                        downloaderProvider    //...und wenn es nötig ist im netz
                });
        return mapTileProviderArray;
    }

    private MapTileDownloader getMapTileDownloader(final ITileSource tileSource) {
        // Create a file cache modular provider
        //FIXME: Der Cache-Mechanismus speichert die Tiles unter "/osmdroid/<kartenname>". Dies sollte man unterbinden,
        // da es evtl zu Konflikten mit Apps kommt, die auch osmdroid verwenden!
        final TileWriter tileWriter = new TileWriter();
        // Create a download modular tile provider
        final NetworkAvailabliltyCheck networkAvailabliltyCheck = new NetworkAvailabliltyCheck(context);
        //ACHTUNG: Der Downloader funktioniert aktuell nur mit dem FilesystemProvider ordnungsgemäß!! Eigenständig 
        // funkioniert dieser nicht! Es liegt daran, dass die überschriebene Methode "tileLoaded" in der internen
        // TileLoader-Klasse nicht dafür vorgesehen ist. Es funktioniert allerdings, wenn man die überschriebene 
        // Methode nicht überschreibt!
        final MapTileDownloader downloaderProvider =
                new MapTileDownloader(tileSource, tileWriter, networkAvailabliltyCheck);
        return downloaderProvider;
    }


    private NicTileSource getTileSource(final String mapName) {
        final NicTileSource tileSource = new NicTileSource(
                mapName,
                ResourceProxy.string.unknown,
                config.getPropertyValueAsInt(MAP_ZOOM_MIN), //min zoom-level
                config.getPropertyValueAsInt(MAP_ZOOM_MAX), //max zoom-level
                config.getPropertyValueAsInt(MAP_TILE_SIZE_PIXEL),
                config.getPropertyValue(MAP_FILE_ENDING)); //filename-ending
        return tileSource;
    }

    private NicTileSource getNicTileSource(final String urlSchema, final String mapName) {
        final NicTileSource tileSource = new NicTileSource(
                mapName,
                ResourceProxy.string.unknown,
                config.getPropertyValueAsInt(MAP_ZOOM_MIN), //min zoom-level
                config.getPropertyValueAsInt(MAP_ZOOM_MAX), //max zoom-level
                urlSchema,
                config.getPropertyValueAsInt(MAP_TILE_SIZE_PIXEL),
                config.getPropertyValue(MAP_FILE_ENDING)); //filename-ending
        return tileSource;
    }
}
