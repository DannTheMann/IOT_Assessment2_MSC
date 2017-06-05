package dja33.msc.ukc.myapplication;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 *
 * Created by Dante on 15/03/2017.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Snackbar notification;

    /**
     * Wrapper class to handler the 3 buttons at the top of the map
     *
     * Has 3 potential events, each event will feed a result that
     * will then been displayed using a SnackBar at the bottom of
     * the View
     *
     * Handles moving of the camera to the user, toggling friends and
     * finding the closest friend.
     */
    private class ButtonHandler implements View.OnClickListener{

        // The state of this button
        private final int state;

        /**
         * Create a button handler that based on
         * a certain state will act out certain
         * events on the map
         * @param state The state
         */
        public ButtonHandler(final int state){
            this.state = state;
        }

        /**
         * Override the onClick event to
         * provide a call as for when the
         * button is clicked, uses the
         * state to handle the map
         * @param v The view
         */
        @Override
        public void onClick(View v) {
            String message = null;
            /* Switch on the state */
            switch(state){
                case 1:
                    // Move the maps camera to the users location
                    message = map.moveCameraToUser();
                    break;
                case 2:
                    // Toggles whether we can currently see our friends
                    boolean visible = map.toggleFriends();
                    message = visible ? "Friends are now visible." : "Friends are now hidden.";
                    ((Button)findViewById(R.id.togglefriends)).setText(visible ? "Hide Friends" : "Show Friends");
                    break;
                case 3:
                    // Find the closest friend to our users location
                    message = map.findClosestFriend();
                    break;
            }

            /* Given the state was valid display a SnackBar with the reported
            *  information on the event */
            if(message != null) {
                if(notification != null){
                    notification.dismiss();
                }
                notification = Snackbar.make(findViewById(R.id.map), message, Snackbar.LENGTH_LONG);
                notification.show();
            }

        }
    }

    // Handler for GoogleMap and HTTPHandler
    private static MapHandler map;

    // Set up the view for GoogleMaps
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /* Set up listeners for the 3 buttons at the top of the View */
        findViewById(R.id.findme).setOnClickListener(new ButtonHandler(1));
        findViewById(R.id.togglefriends).setOnClickListener(new ButtonHandler(2));
        findViewById(R.id.close).setOnClickListener(new ButtonHandler(3));

    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     *
     * Creates a MapHandler that takes the passed GoogleMap parameter
     * and then-on handles all map related purposes for the application
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Create the map handler passing it the context of
        // the app as well as the GoogleMap reference
        map = new MapHandler(this, googleMap);

        // Start retrieving JSON data using HTTPHandler
        map.run();
    }

}
