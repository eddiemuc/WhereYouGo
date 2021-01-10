/*
 * Copyright 2010, 2011, 2012 mapsforge.org
 * Copyright 2013, 2014 biylda <biylda@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package menion.android.whereyougo.maps.mapsforge.overlay;


import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;


import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.GroupLayer;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.util.MapViewProjection;

import java.util.HashMap;

import menion.android.whereyougo.MainApplication;
import menion.android.whereyougo.maps.mapsforge.TapEventListener;

public class PointListOverlay extends GroupLayer {
    HashMap<LatLong, PointOverlay> hitMap = new HashMap<LatLong, PointOverlay>();
    TapEventListener onTapListener;

    public PointListOverlay() {
        super();
    }

    public synchronized void clear() {
        layers.clear();
        hitMap.clear();
    }

    public synchronized boolean checkItemHit(LatLong geoPoint, MapView mapView) {
        Log.e("litezee", "check hit " + geoPoint.latitude + " " + geoPoint.longitude);
        MapViewProjection projection = mapView.getMapViewProjection();
        Point eventPosition = projection.toPixels(geoPoint);

        // check if the translation to pixel coordinates has failed
        if (eventPosition == null) {
            return false;
        }

        Point checkItemPoint = null;
        for (int i = this.layers.size() - 1; i >= 0; i--) {
            Layer item = layers.get(i);

            if (!(item instanceof PointOverlay)) {
                continue;
            }
            PointOverlay checkOverlayItem = (PointOverlay) item;

            // make sure that the current item has a position
            if (checkOverlayItem.getLatLong() == null) {
                continue;
            }

            checkItemPoint = projection.toPixels(checkOverlayItem.getLatLong());
            // check if the translation to pixel coordinates has failed
            if (checkItemPoint == null) {
                continue;
            }

            // select the correct marker for the item and get the position
            Drawable drawable = new BitmapDrawable(MainApplication.getContext().getResources(), AndroidGraphicFactory.getBitmap(checkOverlayItem.getBitmap()));
            Rect checkMarkerBounds = drawable.getBounds();
            if (checkMarkerBounds.left == checkMarkerBounds.right
                    || checkMarkerBounds.top == checkMarkerBounds.bottom)
                continue;

            // calculate the bounding box of the marker
            double checkLeft = checkItemPoint.x + checkMarkerBounds.left;
            double checkRight = checkItemPoint.x + checkMarkerBounds.right;
            double checkTop = checkItemPoint.y + checkMarkerBounds.top;
            double checkBottom = checkItemPoint.y + checkMarkerBounds.bottom;

            // check if the event position is within the bounds of the marker
            if (checkRight >= eventPosition.x && checkLeft <= eventPosition.x
                    && checkBottom >= eventPosition.y && checkTop <= eventPosition.y) {
                if (onTap(checkOverlayItem)) {
                    hitMap.put(geoPoint, checkOverlayItem);
                    return true;
                }
            }
        }
        return false;
    }

//    public synchronized void onTap(GeoPoint p) {
//        int i = hitMap.remove(p).getId();
//        Log.d("MAP", "tapped " + i);
//    }

    public synchronized boolean onTap(PointOverlay pointOverlay) {
        Log.d("MAP", "tapped bool " + pointOverlay.getId());
        if (onTapListener != null)
            onTapListener.onTap(pointOverlay);
        return true;
    }

    public void registerOnTapEvent(TapEventListener onTapListener) {
        this.onTapListener = onTapListener;
    }

    public void unregisterOnTapEvent() {
        this.onTapListener = null;
    }
}
