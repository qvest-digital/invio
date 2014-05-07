package de.tarent.nic.android.base.tileprovider;

import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileFileStorageProviderBase;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import java.io.IOException;
import java.io.InputStream;

/**
 * This TileProvider looks into the asset-directory for the map-tiles.
 *
 * @author Sven Schumann, <s.schumann@tarent.de>
 */
public class AssetTileProvider extends MapTileFileStorageProviderBase {
    private NicTileSource tileSource;
    private final AssetManager assetManager;

    /**
     * Custom constructor
     *
     * @param registerReceiver the {@link IRegisterReceiver}
     * @param tileSource       the {@link NicTileSource}
     * @param assetManager     the {@link AssetManager}
     */
    public AssetTileProvider(final IRegisterReceiver registerReceiver, final NicTileSource tileSource,
                             AssetManager assetManager) {
        super(registerReceiver, NUMBER_OF_TILE_FILESYSTEM_THREADS, TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);

        setTileSource(tileSource);
        this.assetManager = assetManager;
    }

    @Override
    public int getMaximumZoomLevel() {
        return tileSource.getMaximumZoomLevel();
    }

    @Override
    public int getMinimumZoomLevel() {
        return tileSource.getMinimumZoomLevel();
    }

    @Override
    protected String getName() {
        return "Assetprovider";
    }

    @Override
    protected String getThreadGroupName() {
        return "asset";
    }

    @Override
    protected Runnable getTileLoader() {
        return new TileLoader();
    }

    @Override
    public boolean getUsesDataConnection() {
        return false;
    }

    @Override
    public final void setTileSource(final ITileSource tileSource) {
        if (tileSource instanceof NicTileSource) {
            this.tileSource = (NicTileSource) tileSource;
        } else {
            throw new IllegalArgumentException("TileSource " + tileSource + " is not supported!");
        }
    }

    /**
     * Private class for loading tiles.
     *
     * @author Sven Schumann, <s.schumann@tarent.de>
     */
    private class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        protected Drawable loadTile(MapTileRequestState req) throws CantContinueException {
            InputStream inputStream;
            try {
                final String filePath = tileSource.getTileRelativeFilenameString(req.getMapTile());

                inputStream = assetManager.open(filePath);
                return new ExpirableBitmapDrawable(BitmapFactory.decodeStream(inputStream));
            } catch (final IOException e) {
                return null;
            }
        }
    }
}
