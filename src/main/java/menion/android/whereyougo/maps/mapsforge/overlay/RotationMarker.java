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

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.model.BoundingBox;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Marker;

import menion.android.whereyougo.MainApplication;

/**
 * A {@code Marker} draws a {@link Drawable} at a given geographical position.
 */
public class RotationMarker extends Marker {
    float rotation;

    /**
     * @param geoPoint the initial geographical coordinates of this marker (may be null).
     * @param drawable the initial {@code Drawable} of this marker (may be null).
     */
    public RotationMarker(LatLong geoPoint, Drawable drawable) {
        super(geoPoint,  AndroidGraphicFactory.convertToBitmap(drawable), 0, 0);
    }

    private static boolean intersect(Canvas canvas, float left, float top, float right, float bottom) {
        return right >= 0 && left <= canvas.getWidth() && bottom >= 0 && top <= canvas.getHeight();
    }

    @Override
    public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas,
                                     Point canvasPosition) {
        LatLong geoPoint = this.getLatLong();
        Drawable drawable = new BitmapDrawable(MainApplication.getContext().getResources(), AndroidGraphicFactory.getBitmap(getBitmap()));
        if (geoPoint == null || drawable == null) {
            return;
        }

        double latitude = geoPoint.latitude;
        double longitude = geoPoint.longitude;
        int pixelX =
                (int) (MercatorProjection.longitudeToPixelX(longitude, zoomLevel) - canvasPosition.x);
        int pixelY =
                (int) (MercatorProjection.latitudeToPixelY(latitude, zoomLevel) - canvasPosition.y);

        Rect drawableBounds = drawable.copyBounds();
        int left = pixelX + drawableBounds.left;
        int top = pixelY + drawableBounds.top;
        int right = pixelX + drawableBounds.right;
        int bottom = pixelY + drawableBounds.bottom;

        if (!intersect(canvas, left, top, right, bottom)) {
            return;
        }

        int saveCount = AndroidGraphicFactory.getCanvas(canvas).save();
        AndroidGraphicFactory.getCanvas(canvas).rotate(rotation, (float) pixelX, (float) pixelY);
        drawable.setBounds(left, top, right, bottom);
        drawable.draw(AndroidGraphicFactory.getCanvas(canvas));
        drawable.setBounds(drawableBounds);
        AndroidGraphicFactory.getCanvas(canvas).restoreToCount(saveCount);
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

}
