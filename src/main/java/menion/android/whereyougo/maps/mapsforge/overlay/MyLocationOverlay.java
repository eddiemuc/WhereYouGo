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

import android.content.Context;


import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;

public class MyLocationOverlay extends Layer implements LocationListener {

    private static final int UPDATE_DISTANCE = 0;
    private static final int UPDATE_INTERVAL = 1000;
    final Circle circle;
    final LocationManager locationManager;
    final MapView mapView;
    final Marker marker;
    boolean centerAtNextFix;
    Location lastLocation;
    boolean myLocationEnabled;
    boolean snapToLocationEnabled;

    /**
     * Constructs a new {@code MyLocationOverlay} with the given drawable and the default circle
     * paints.
     *
     * @param context  a reference to the application context.
     * @param mapView  the {@code MapView} on which the location will be displayed.
     * @param marker   a drawable to display at the current location (might be null).
     */
    public MyLocationOverlay(Context context, MapView mapView, Marker marker) {
        this(context, mapView, marker, getDefaultCircleFill(), getDefaultCircleStroke());
    }

    /**
     * Constructs a new {@code MyLocationOverlay} with the given drawable and circle paints.
     *
     * @param context      a reference to the application context.
     * @param mapView      the {@code MapView} on which the location will be displayed.
     * @param marker       a drawable to display at the current location (might be null).
     * @param circleFill   the {@code Paint} used to fill the circle that represents the current
     *                     location (might be null).
     * @param circleStroke the {@code Paint} used to stroke the circle that represents the current
     *                     location (might be null).
     */
    public MyLocationOverlay(Context context, MapView mapView, Marker marker, Paint circleFill,
                             Paint circleStroke) {
        this.mapView = mapView;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.marker = marker;
        this.circle = new Circle(null, 0, circleFill, circleStroke);
    }

    private static Paint getDefaultCircleFill() {
        return getPaint(Style.FILL, Color.BLUE, 48);
    }

    private static Paint getDefaultCircleStroke() {
        Paint paint = getPaint(Style.STROKE, Color.BLUE, 128);
        paint.setStrokeWidth(2);
        return paint;
    }

    private static Paint getPaint(Style style, Color color, int alpha) {
        Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
        paint.setStyle(style);
        paint.setColor(color);
        AndroidGraphicFactory.INSTANCE.getPaint(paint).setAlpha(alpha);
        return paint;
    }

    /**
     * @param location the location whose geographical coordinates should be converted.
     * @return a new GeoPoint with the geographical coordinates taken from the given location.
     */
    public static LatLong locationToGeoPoint(Location location) {
        return new LatLong(location.getLatitude(), location.getLongitude());
    }

    /**
     * Stops the receiving of location updates. Has no effect if location updates are already
     * disabled.
     */
    public synchronized void disableMyLocation() {
        if (this.myLocationEnabled) {
            this.myLocationEnabled = false;
            this.locationManager.removeUpdates(this);
            this.mapView.getLayerManager().redrawLayers();
        }
    }

    @Override
    public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeft) {
        if (!this.myLocationEnabled) {
            return;
        }

        double canvasPixelLeft =
                MercatorProjection.longitudeToPixelX(boundingBox.minLongitude, zoomLevel);
        double canvasPixelTop = MercatorProjection.latitudeToPixelY(boundingBox.maxLatitude, zoomLevel);
        Point canvasPosition = new Point(canvasPixelLeft, canvasPixelTop);
        //TODO mapsforge-upgrade: folllwing leads to NPE
        //this.circle.draw(boundingBox, zoomLevel, canvas, canvasPosition);
        //this.marker.draw(boundingBox, zoomLevel, canvas, canvasPosition);
    }

    private synchronized boolean enableBestAvailableProvider() {
        disableMyLocation();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestAvailableProvider = this.locationManager.getBestProvider(criteria, true);
        if (bestAvailableProvider == null) {
            return false;
        }
        this.lastLocation = this.locationManager.getLastKnownLocation(bestAvailableProvider);
        this.locationManager.requestLocationUpdates(bestAvailableProvider, UPDATE_INTERVAL,
                UPDATE_DISTANCE, this);
        this.myLocationEnabled = true;
        return true;
    }

    /**
     * Enables the receiving of location updates from the most accurate {@link LocationProvider}
     * available.
     *
     * @param centerAtFirstFix whether the map should be centered to the first received location fix.
     * @return true if at least one location provider was found, false otherwise.
     */
    public synchronized boolean enableMyLocation(boolean centerAtFirstFix) {
        if (!enableBestAvailableProvider()) {
            return false;
        }

        this.centerAtNextFix = centerAtFirstFix;
        return true;
    }

    /**
     * @return the most-recently received location fix (might be null).
     */
    public synchronized Location getLastLocation() {
        return this.lastLocation;
    }

 //   public synchronized boolean checkItemHit(GeoPoint geoPoint, MapView mapView) {
//        return false;
//    }

    /**
     * @return true if the map will be centered at the next received location fix, false otherwise.
     */
    public synchronized boolean isCenterAtNextFix() {
        return this.centerAtNextFix;
    }

    /**
     * @return true if the receiving of location updates is currently enabled, false otherwise.
     */
    public synchronized boolean isMyLocationEnabled() {
        return this.myLocationEnabled;
    }

    /**
     * @return true if the snap-to-location mode is enabled, false otherwise.
     */
    public synchronized boolean isSnapToLocationEnabled() {
        return this.snapToLocationEnabled;
    }

    /**
     * @param snapToLocationEnabled whether the map should be centered at each received location fix.
     */
    public synchronized void setSnapToLocationEnabled(boolean snapToLocationEnabled) {
        this.snapToLocationEnabled = snapToLocationEnabled;
    }

    @Override
    public void onLocationChanged(Location location) {
        synchronized (this) {
            this.lastLocation = location;
            // this.bearing = location.getBearing();

            LatLong geoPoint = locationToGeoPoint(location);
            this.marker.setLatLong(geoPoint);
            this.circle.setLatLong(geoPoint);
            this.circle.setRadius(location.getAccuracy());

            if (this.centerAtNextFix || this.snapToLocationEnabled) {
                this.centerAtNextFix = false;
                this.mapView.setCenter(geoPoint);
            }
        }
        this.mapView.getLayerManager().redrawLayers();
    }

    @Override
    public void onProviderDisabled(String provider) {
        enableBestAvailableProvider();
    }

    @Override
    public void onProviderEnabled(String provider) {
        enableBestAvailableProvider();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // do nothing
    }

 }
