/*
 * Copyright 2010, 2011, 2012 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package menion.android.whereyougo.maps.mapsforge.mapgenerator;

import android.content.Context;
import android.net.Uri;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.TileLayer;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.mapsforge.map.layer.download.tilesource.TileSource;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.IMapViewPosition;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.reader.header.MapFileInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A factory for the internal MapGenerator implementations.
 */
public final class TileLayerGenerator {
    //private static final String MAP_GENERATOR_ATTRIBUTE_NAME = "mapGenerator";

    private TileLayerGenerator() {
        throw new IllegalStateException();
    }

    public static class TileLayerData {
        public final TileLayer<?> tileLayer;
        public final byte minZoom;
        public final byte maxZoom;
        public final String attribution;
        public final MapFileInfo mapFileInfo;

        public TileLayerData(final TileLayer<?> tileLayer, final byte minZoom, final byte maxZoom, final String attribution, final MapFileInfo mapFileInfo) {
            this.tileLayer = tileLayer;
            this.minZoom = minZoom;
            this.maxZoom = maxZoom;
            this.attribution = attribution;
            this.mapFileInfo = mapFileInfo;
        }
    }

    /**
     * @param source the internal MapGenerator implementation.
     * @return a new MapGenerator instance.
     */
    public static TileLayerData createMapGenerator(WhereYouGoMapSource source, Context ctx, Uri mapFile, TileCache tileCache, IMapViewPosition mapPosition) {
        switch (source) {
            case BLANK:
                return null;
            case DATABASE_RENDERER:
                return createOfflineSource(ctx, mapFile, tileCache, mapPosition);
            case OPENSTREETMAP:
                return createOnlineSource(
                        18,
                        "https://a.tile.openstreetmap.org",
                        "\u00a9 OpenStreetMap contributors, CC-BY-SA", tileCache, mapPosition);
            case OPENSTREETMAP_DE:
                return createOnlineSource(
                        18,
                        "https://a.tile.openstreetmap.de",
                        "\u00a9 OpenStreetMap contributors, CC-BY-SA", tileCache, mapPosition);
            case OPENSTREETMAP_CyclOSM:
                return createOnlineSource(
                        18,
                        "https://a.tile-cyclosm.openstreetmap.fr/cyclosm",
                        "Tiles \u00a9 CyclOSM, Openstreetmap France, data \u00a9 OpenStreetMap contributors, ODBL", tileCache, mapPosition);
            case PUBLIC_TRANSPORT_OEPNV:
                return createOnlineSource(
                        18,
                        "https://tile.memomaps.de/tilegen",
                        "\u00a9 OpenStreetMap contributors, CC-BY-SA", tileCache, mapPosition);
            case ESRI_WORLD_STREET_MAP:
                return createOnlineSource(
                        18,
                        "https://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile",
                        "\u00a9 Esri, DeLorme, NAVTEQ, USGS, Intermap, iPC, NRCAN, Esri Japan, METI, Esri China (Hong Kong), Esri (Thailand), TomTom, 2012", tileCache, mapPosition);
            case ESRI_WORLD_IMAGERY:
                return createOnlineSource(
                        18,
                        "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile",
                        "\u00a9 Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community", tileCache, mapPosition);
            default:
                return null;
        }
    }

    private static TileLayerData createOnlineSource(int maxZoom, String host, String attribution, TileCache tileCache, IMapViewPosition pos) {
        final OnlineTileSource ts = new OnlineTileSource(new String[]{host}, -1);
        ts.setZoomLevelMax((byte)maxZoom);

        return new TileLayerData(new TileDownloadLayer(tileCache, pos, ts, AndroidGraphicFactory.INSTANCE), ts.getZoomLevelMin(), ts.getZoomLevelMax(), attribution, null);
    }

    private static TileLayerData createOfflineSource(Context ctx, Uri mapFile, TileCache tileCache, IMapViewPosition pos) {
        try {
            final InputStream is = ctx.getContentResolver().openInputStream(mapFile);
            if (is == null) {
                return null;
            }
            final MapFile mf = new MapFile((FileInputStream) is);
            final String attribution = mf.getMapFileInfo().comment;
            if (mf != null) {
                return new TileLayerData(new TileRendererLayer(tileCache, mf, pos, false, true, false, AndroidGraphicFactory.INSTANCE),
                    mf.getMapFileInfo().zoomLevelMin, mf.getMapFileInfo().zoomLevelMax, attribution, mf.getMapFileInfo());
            }
        } catch(IOException ioe) {

        }
        return null;
    }

}
