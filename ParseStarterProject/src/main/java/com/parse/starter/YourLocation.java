package com.parse.starter;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class YourLocation extends FragmentActivity implements  LocationListener {

    private GoogleMap mMap;

    LocationManager locationManager;
    String provider;

    TextView infoTextView;

    Button requestUberButton;

    Boolean requestActive = false;
    String driverUsername = "";
    ParseGeoPoint driverLocation = new ParseGeoPoint(0, 0);

    Handler handler  = new Handler();

    public void requestUber(View view){

        if(requestActive == false){
            ParseObject request = new ParseObject("Requests");

            request.put("requesterUsername", ParseUser.getCurrentUser().getUsername());

            ParseACL parseACL = new ParseACL();

            parseACL.setPublicWriteAccess(true);
            parseACL.setPublicReadAccess(true);
            request.setACL(parseACL);

            request.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null){
                        infoTextView.setText("Finding Uber driver...");
                        requestUberButton.setText("Cancel Uber");
                        requestActive = true;
                    }
                }
            });
        } else {
            infoTextView.setText("Uber Cancelled.");
            requestUberButton.setText("Request Uber");
            requestActive = false;

            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");

            query.whereEqualTo("requesterUsername", ParseUser.getCurrentUser().getUsername());

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null){
                        if (objects.size() > 0){
                            for (ParseObject object : objects){
                                object.deleteInBackground();
                            }
                        }
                    }
                }
            });
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
        setUpMapIfNeeded();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        infoTextView = (TextView) findViewById(R.id.infoTextView);

        requestUberButton = (Button) findViewById(R.id.requestUber);

        locationManager.requestLocationUpdates(provider, 400, 1, this);

        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {


            updateLocation(location);

        }
    }

    public void updateLocation( final Location location){

        mMap.clear();

        if(requestActive == false){
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");

            query.whereEqualTo("requesterUsername", ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null){
                        if(objects.size() > 0){
                            for (ParseObject object : objects){
                                requestActive = true;
                                infoTextView.setText("Finding Uber driver");
                                requestUberButton.setText("Cancel Uber");

                                if(object.get("driverUsername") != null){
                                    driverUsername = object.getString("driverUsername");
                                    infoTextView.setText("Your driver is on their way");
                                    requestUberButton.setVisibility(View.INVISIBLE);

                                    Log.i("AppInfo", driverUsername);
                                }

                            }
                        }
                    }
                }
            });
        }

        if(driverUsername.equals("")){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("You are here"));
        }

        if(requestActive == true){

            if(!driverUsername.equals("")){
                ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
                userQuery.whereEqualTo("username", driverUsername);
                userQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        if( e == null){
                            if (objects.size() > 0){
                                for(ParseUser driver: objects){

                                    driverLocation = driver.getParseGeoPoint("location");
                                }
                            }
                        }
                    }
                });

                if(driverLocation.getLatitude() != 0 && driverLocation.getLongitude() != 0){
                    Log.i("AppInfo", driverLocation.toString());

                    Double distanceInKm = driverLocation.distanceInKilometersTo(new ParseGeoPoint(location.getLatitude(), location.getLongitude()));

                    infoTextView.setText("You driver is " + String.valueOf(String.format("%.1f", distanceInKm) + " away"));

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    ArrayList<Marker> markers = new ArrayList<Marker>();

                    markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("Driver Location")));

                    markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location")));

                    for (Marker marker : markers) {
                        builder.include(marker.getPosition());
                    }

                    LatLngBounds bounds = builder.build();

                    int padding = 1000;
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                    mMap.animateCamera(cu);
                }
            }

            final ParseGeoPoint userLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");

            query.whereEqualTo("requesterUsername", ParseUser.getCurrentUser().getUsername());

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0) {
                            for (ParseObject object : objects) {
                                object.put("requesterLocation", userLocation);
                                object.saveInBackground();
                            }
                        }
                    }
                }
            });


        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLocation(location);
            }
        }, 5000);

    }


    @Override
    protected void onResume() {
        super.onResume();
//        onMapReady(mMap);
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }


    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {

    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//    }

    @Override
    public void onLocationChanged(Location location) {

        mMap.clear();

        updateLocation(location);




    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
