package dja33.msc.ukc.myapplication;

import com.google.android.gms.maps.model.LatLng;

import static dja33.msc.ukc.myapplication.MapHandler.USER_LAT;
import static dja33.msc.ukc.myapplication.MapHandler.USER_LON;

/**
 *
 * Static class for use in calculating maths on distances given
 * the lat and lon of one location to another.
 *
 * Uses the 'haversine' formula to calculate the great-circle distance between
 * two points. I.e as the crow flies, no google map plotting in this case.
 *
 * Created by Dante on 15/03/2017.
 */
public class DistanceCalculator {

    // constant used for earths radius
    private static final int EARTH_RADIUS = 6371;

    /**
     * Returns a pretty formatted string in
     * "distance away %dm | %.2fmi" from a location
     * provided to the users location
     * @param latLng The location to reference
     * @return formatted string
     */
    public static String getDistance(final LatLng latLng){
        final double distance = calculateDistance(latLng);
        return String.format("Distance away: %dm | %.2fmi", distanceToInt(distance), kilometersToMiles(distance));
    }

    /**
     * Calculate the distance between one location and another
     * @param latlng The location to reference
     * @return distance (As the crow flies) in metres
     */
    public static double calculateDistance(LatLng latlng) {

        double latDistance = toRad(USER_LAT - latlng.latitude);
        double lonDistance = toRad(USER_LON - latlng.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(latlng.latitude)) * Math.cos(toRad(USER_LAT)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        return EARTH_RADIUS * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));

    }

    /**
     * Kilometer to miles conversion given a double
     * @param distance The km to convert
     * @return miles translation
     */
    private static double kilometersToMiles(final double distance) {
        return distance/1.609344;
    }

    /**
     * To Radians
     * @param value values to convert
     * @return radian value
     */
    private static double toRad(final Double value) {
        return value * Math.PI / 180;
    }

    /**
     * Simple value upscale, metres to kilometres
     * @param km value to upscale
     * @return upscaled value
     */
    private static int distanceToInt(double km){
        km *= 1000;
        return (int) km;
    }

}
