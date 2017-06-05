package dja33.msc.ukc.myapplication;

/**
 * Created by Dante on 15/03/2017.
 */

import android.content.Context;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Map Handler
 *
 * Handles all interaction from the application to
 * Google Maps. Provides interfaces for the application
 * to interact with the HTTPHandler in which it can
 * poll data and display it.
 *
 * Implements Runnable to allow for background interactions
 * with the HTTPHandler which uses AsyncTask to perform
 * web requests in a separate thread.
 *
 * Created by Dante on 15/03/2017.
 */
public class MapHandler implements Runnable{

    /* User lat and lon for senate building at the University */
    public static final double USER_LAT = 51.297269;
    public static final double USER_LON = 1.069740;

    private static final int DEFAULT_ZOOM = 14;

    /* URL for HTTPHandler */
    private static final String URL = "https://www.cs.kent.ac.uk/people/staff/iau/LocalUsers.php";

    /* User marker information */
    private static final String USER_TITLE = "You";
    private static final String USER_SNIPPET = "Your current location.";
    private static final float USER_COLOUR = BitmapDescriptorFactory.HUE_BLUE;

    /* Delay between each attempt at GET request with HTTPHandler */
    private static final long DELAY_BETWEEN_POLLING = 5000;

    /* Other constants set in the constructor for use */
    private final GoogleMap gmap;
    private final Context app;
    private final List<Person> friends;
    private final HTTPHandler httpHandler;
    private final MarkerOptions user;
    private final ClusterManager<Person> clusterManager;

    // Whether our friends are visible
    private boolean friendsVisible;
    // Whether the HTTPHandler is running in a thread
    private boolean runHTTPHandler;

    /**
     * Create the MapHandler which creates and defines
     * information from the constants provided in fields
     * such as location of the user as well as setting up
     * the HTTPHandler.
     * @param app The Application to be used for Context
     * @param gmap The GoogleMap reference
     */
    public MapHandler(final Context app, final GoogleMap gmap){
        this.gmap = gmap;
        this.app = app;
        // ClusterManager used to perform nice arrangements of markers
        this.clusterManager = new ClusterManager<>(app, gmap);
        this.clusterManager.setAlgorithm(new GridBasedAlgorithm<Person>());

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        this.gmap.setOnCameraIdleListener(clusterManager);
        this.gmap.setOnMarkerClickListener(clusterManager);

        this.httpHandler = new HTTPHandler(URL);
        this.httpHandler.execute();
        this.friends = new ArrayList<>();

        // Set and add user data
        this.user = new MarkerOptions();
        LatLng userLocation = new LatLng(USER_LAT, USER_LON);

        {
            this.user.position(userLocation);
            this.user.title(USER_TITLE);
            this.user.snippet(USER_SNIPPET);
            this.user.icon(BitmapDescriptorFactory.defaultMarker(USER_COLOUR));
            this.gmap.addMarker(this.user);
        }

        // Move the maps current camera to the users location
        this.gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM));

    }

    /**
     * Move the camera to the users location
     * and zoom to 'street level'
     * @return String of the event
     */
    public String moveCameraToUser() {
        this.gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(USER_LAT, USER_LON), 15));
        return "Moved camera to user position.";
    }

    /**
     * Toggle whether the friends are currently visible
     * returns true if they are and false if not, will
     * redraw the clusterManagers collection
     * @return true if friends are visible
     */
    public boolean toggleFriends() {
        if(friendsVisible){
            clusterManager.clearItems();
        }else{
            clusterManager.addItems(friends);
        }
        clusterManager.cluster();
        friendsVisible = !friendsVisible;
        return friendsVisible;
    }

    /**
     * Find the closest friend, if no
     * friends are present or are hidden
     * will instead return nothing. The returned
     * result contains the distance as a string in
     * km and miles with the friends name.
     * @return String of the results
     */
    public String findClosestFriend() {
        Person closest = null;
        double distance = Double.MAX_VALUE;
        for(Person p : friends){
            if(p.distance < distance){
                distance = p.distance;
                closest = p;
            }
        }

        // If we found a friend and they are not hidden
        if(closest != null && friendsVisible) {
            this.gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(closest.getPosition(), gmap.getCameraPosition().zoom >= 15 ? gmap.getCameraPosition().zoom : 15));
            return closest.getTitle() + " is the closest friend ( " + DistanceCalculator.getDistance(closest.getPosition()) + ")";
        }
        return "No friends found :(";
    }

    @Deprecated
    /**
     * Add all friends to the map as markers,
     * unused and deprecated due to Cluster implementation
     */
    public void addAllPeople(){
        for(Person p : friends){
            gmap.addMarker(new MarkerOptions().position(p.getPosition()).title(p.getTitle()));
        }
    }

    /**
     * Add all friends to the ClusterManager
     * will not duplicate and instead first removes
     * all friends to then add all them again sets
     * friends to be visible by default
     */
    public void addCluster(){

        clusterManager.clearItems();
        clusterManager.addItems(friends);

        friendsVisible = true;

    }

    @Override
    /**
     * Threading element of the MapHandler, will run
     * until pollData returns successfully or throws
     * an exception otherwise which is outside the control
     * of the application.
     *
     * Tries to retrieve all content from the HTTPHandler
     * given the URL and previous information.
     */
    public void run() {
        runHTTPHandler = true;
        while(runHTTPHandler){
            try {
                Thread.sleep(DELAY_BETWEEN_POLLING);
                runHTTPHandler = !pollData();
                addCluster();
            }catch(InterruptedException ie){
                // We were interrupted outside of the application
                runHTTPHandler = false;
            } catch (HTTPNoResponseException e) {
                // Alternatively use a SnackBar to display an error or the alike.
                //System.out.println("No response from '" + e.getURL() + "'... response = " + e.getResponseCode());
            }
        }
    }

    /**
     * Unused but here for potential, stops the HTTPHandler
     * if needed.
     */
    public void stopHTTPHandler(){
        runHTTPHandler = false;
    }

    /**
     * Unused but here for potential, returns whether the HTTPHandler
     * is running or not
     * @return true if running
     */
    public boolean isHTTPHandlerRunning() { return runHTTPHandler; }

    /**
     * Polls the HTTPHandler to fetch all data from URL, assumes JSON
     * format of the URL using GET. Will fill the friends collection with
     * data polled from the website.
     * @return true if successful, false if JSON was malformed
     * @throws HTTPNoResponseException If the website requested will not respond beyond response 200
     */
    public boolean pollData() throws HTTPNoResponseException{

        int response = httpHandler.getResponse();

        if(response != 200){
            // We didn't receive the OK from the website
            throw new HTTPNoResponseException(httpHandler.getURL(), response, "No response from website.");
        }

        String raw = httpHandler.getRawContentsAsString();

        try {

            // Parse JSON into an Array
            JSONArray array = new JSONObject(raw).getJSONArray("Users");

            // For all elements add them to the friends collection
            for(int i = 0; i < array.length(); i++){
                JSONObject js = array.getJSONObject(i);
                friends.add(new Person(js.getDouble("lat"), js.getDouble("lon"), js.getString("name"), null));
            }

            return true;

        }catch(JSONException jsonExc){
            // Malformed JSON
            jsonExc.printStackTrace();
        }

        return false;
    }

    /**
     * Wrapper class for ClusterItem component of the
     * ClusterManager, stores generic data including the
     * distance from the User location to the friends.
     *
     * This class assumes that the Person never changes location
     * and is a shortcoming of the given implementation, however
     * this is ASSUMED of the assessment - if given the knowledge
     * that the friends may move around this implementation would
     * either of allowed for an update functionality or rather
     * used listener events to update the distance.
     */
    private class Person implements ClusterItem {

        private final LatLng position;
        private final String name;
        private final String mSnippet;
        private final double distance;

        /**
         * Create person or 'friend' with provided parameters to be
         * used in the ClusterManager
         * @param lat Their latitude
         * @param lng Their longitude
         * @param title Their name or 'title'
         * @param snippet Their subtitle
         */
        public Person(final double lat, final double lng, final String title, final String snippet) {
            position = new LatLng(lat, lng);
            name = title;
            /* Set the subtitle snippet to a description of the distance
            * from the users location */
            mSnippet = DistanceCalculator.getDistance(position);
            distance = DistanceCalculator.calculateDistance(position);
        }

        @Override
        public LatLng getPosition() {
            return position;
        }

        @Override
        public String getTitle() {
            return name;
        }

        @Override
        public String getSnippet() {
            return mSnippet;
        }
    }
}
