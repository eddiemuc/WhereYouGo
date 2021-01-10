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

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.rotation.SmoothCanvas;
import org.mapsforge.map.layer.overlay.Marker;

import menion.android.whereyougo.MainApplication;

/**
 * A {@code Marker} draws a {@link Drawable} at a given geographical position.
 */
public class LabelMarker extends Marker {
    static Paint labelPaint;
    static Paint labelBgPaint;



    static {
        labelPaint = AndroidGraphicFactory.INSTANCE.createPaint();
        labelPaint.setStyle(Style.STROKE);
        // labelPaint.setTextAlign(Align.CENTER);
        labelBgPaint = AndroidGraphicFactory.INSTANCE.createPaint();
        labelBgPaint.setColor(Color.argb(192, 255, 255, 255));
        labelBgPaint.setStyle(Style.FILL);
        // labelBgPaint.setTextAlign(Align.CENTER);
    }

    protected String label;
    protected String description;
    protected boolean markerVisible = true;
    protected boolean labelVisible = true;

    /**
     * @param geoPoint the initial geographical coordinates of this marker (may be null).
     * @param drawable the initial {@code Drawable} of this marker (may be null).
     */
    public LabelMarker(LatLong geoPoint, Drawable drawable) {
        this(geoPoint, drawable, null, null);
    }

    /**
     * @param geoPoint the initial geographical coordinates of this marker (may be null).
     * @param drawable the initial {@code Drawable} of this marker (may be null).
     * @param label    the initial caption of this marker (may be null).
     */
    public LabelMarker(LatLong geoPoint, Drawable drawable, String label) {
        this(geoPoint, drawable, label, null);
    }

    /**
     * @param geoPoint    the initial geographical coordinates of this marker (may be null).
     * @param drawable    the initial {@code Drawable} of this marker (may be null).
     * @param label       the initial caption of this marker (may be null).
     * @param description the initial description of this marker (may be null).
     */
    public LabelMarker(LatLong geoPoint, Drawable drawable, String label, String description) {
        super(geoPoint, AndroidGraphicFactory.convertToBitmap(drawable), 0, 0);
        this.label = label;
        this.description = description;
    }

    @Override
    public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point canvasPosition) {
        //       @Override
    //public synchronized boolean draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
    //draw(BoundingBox boundingBox,byte zoomLevel, Canvas canvas,Point canvasPosition) {
        if (markerVisible) {
            super.draw(boundingBox, zoomLevel, canvas, canvasPosition);
        }
        if (!labelVisible || label == null) {
            return;
        }

        //draw label
        LatLong geoPoint = getLatLong();
        Drawable drawable = new BitmapDrawable(MainApplication.getContext().getResources(), AndroidGraphicFactory.getBitmap(getBitmap()));

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

        Rect text = new Rect();
        AndroidGraphicFactory.getPaint(labelPaint).getTextBounds(label, 0, label.length(), text);
        int x = (left + right) / 2 - text.width() / 2;
        int y = bottom;
        int margin = 2;
        Rect r =
                new Rect(x - margin, y - margin, x + text.width() + margin, y + text.height() + margin);

        AndroidGraphicFactory.getCanvas(canvas).drawRect(r, AndroidGraphicFactory.getPaint(labelBgPaint));
        AndroidGraphicFactory.getCanvas(canvas).drawRect(r, AndroidGraphicFactory.getPaint(labelPaint));
        canvas.drawText(label, x, y + text.height(), labelPaint);

    }

    private static boolean intersect(Canvas canvas, float left, float top, float right, float bottom) {
        return right >= 0 && left <= canvas.getWidth() && bottom >= 0 && top <= canvas.getHeight();
    }

    /**
     * @return the description of this marker (may be null).
     */
    public synchronized String getDescription() {
        return this.description;
    }

    /**
     * @param description new description of this marker (may be null).
     */
    public synchronized void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the label of this marker (may be null).
     */
    public synchronized String getLabel() {
        return this.label;
    }

    /**
     * @param label new label of this marker (may be null).
     */
    public synchronized void setLabel(String label) {
        this.label = label;
    }

    public boolean isLabelVisible() {
        return labelVisible;
    }

    public void setLabelVisible(boolean labelVisible) {
        this.labelVisible = labelVisible;
    }

    public boolean isMarkerVisible() {
        return markerVisible;
    }

    public void setMarkerVisible(boolean markerVisible) {
        this.markerVisible = markerVisible;
    }


}
