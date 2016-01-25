package com.parse.starter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class viewRiderLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Intent i;

    public void acceptRequest(View view){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");

        query.whereEqualTo("requesterUsername", i.getStringExtra("username"));

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {

                    if(objects.size() > 0){
                        for(ParseObject object : objects){
                            object.put("driverUsername", ParseUser.getCurrentUser().getUsername());
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null){
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        Uri.parse("http://maps.google.com/maps?daddr=" + i.getDoubleExtra("latitude", 0) + "," + i.getDoubleExtra("longitude", 0));

                                        startActivity(i);

                                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + i.getDoubleExtra("latitude", 0) + "," + i.getDoubleExtra("longitude", 0));
                                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                        mapIntent.setPackage("com.google.android.apps.maps");
                                        startActivity(mapIntent);
                                    }
                                }
                            });


                        }
                    }

                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rider_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        i = getIntent();

        RelativeLayout mapLayout = (RelativeLayout)findViewById(R.id.relativeLayout);


        mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                ArrayList<Marker> markers = new ArrayList<Marker>();

                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(i.getDoubleExtra("latitude", 0), i.getDoubleExtra("longitude", 0))).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("Rider Location")));

                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(i.getDoubleExtra("userLatitude", 0), i.getDoubleExtra("userLongitude", 0))).title("Your Location")));

                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }

                LatLngBounds bounds = builder.build();

                int padding = 100;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                mMap.animateCamera(cu);



            }
        });

        onMapReady(mMap);
    }

    public void back(View view){
        Intent i = new Intent(getApplicationContext(), ViewRequests.class);
            startActivity(i);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }

        // Add a marker in Sydney and move the camera
    }
    private void setUpMap() {




    }

}
