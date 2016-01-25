package com.parse.starter;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ViewRequests extends AppCompatActivity implements LocationListener {

    ListView listView;
    ArrayList<String> listViewContent;
    ArrayAdapter arrayAdapter;
    ArrayList<String> usernames;
    ArrayList<Double> latitudes;
    ArrayList<Double> longitudes;
    LocationManager locationManager;
    String provider;
    Location location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        listView = (ListView) findViewById(R.id.listView);

        listViewContent = new ArrayList<String>();
        usernames = new ArrayList<String>();
        latitudes = new ArrayList<Double>();
        longitudes = new ArrayList<Double>();


        listViewContent.add("finding nearby requests");

        //testing codes
//        ParseObject score = new ParseObject("Score");
//        score.put("username", "Ben");
//        score.put("points", 1000);
//
//        score.saveInBackground();
//
//        score.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e == null) {
//                    Log.i("SaveInBackground", "Successful");
//                } else {
//                    Log.i("SaveInBackground", "Failed");
//                }
//            }
//        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), viewRiderLocation.class);

                i.putExtra("username", usernames.get(position));
                i.putExtra("latitude", latitudes.get(position));
                i.putExtra("longitude", longitudes.get(position));
                i.putExtra("userLatitude", location.getLatitude());
                i.putExtra("userLongitude", location.getLongitude());
                startActivity(i);

                Log.i("PassengerLocation", usernames.get(position) + latitudes.get(position) + longitudes.get(position));
                Log.i("DriverLocation", String.valueOf(location.getLatitude() + " " + location.getLongitude()));
            }
        });


        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listViewContent);

        listView.setAdapter(arrayAdapter);

        locationManager = (LocationManager)

                getSystemService(Context.LOCATION_SERVICE);

        provider = locationManager.getBestProvider(new

                        Criteria(),

                false);

        locationManager.requestLocationUpdates(provider, 400, 1, this);

        location = locationManager.getLastKnownLocation(provider);

        Log.i("locationOnCreate", String.valueOf(location));

        if (location != null)

        {
            updateLocation();
        }


    }

    public void updateLocation() {
        final ParseGeoPoint userLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

        ParseUser.getCurrentUser().put("location", userLocation);
        ParseUser.getCurrentUser().saveInBackground();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");
        Log.i("userlocation", String.valueOf(userLocation));
//        query.whereDoesNotExist("requesterUsername");
        query.whereNear("requesterLocation", userLocation);
        query.setLimit(100);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    Log.i("requests", String.valueOf(objects));
                    if (objects.size() > 0) {
                        listViewContent.clear();
                        usernames.clear();
                        latitudes.clear();
                        longitudes.clear();

                        for (ParseObject object : objects) {
                            if (object.get("driverUsername") == null) {

                                Double distanceInKm = userLocation.distanceInKilometersTo((ParseGeoPoint) object.get("requesterLocation"));

                                listViewContent.add(String.valueOf(String.format("%.1f", distanceInKm)+ " km"));

                                usernames.add(object.getString("requesterUsername"));
                                latitudes.add(object.getParseGeoPoint("requesterLocation").getLatitude());
                                longitudes.add(object.getParseGeoPoint("requesterLocation").getLongitude());
                            }


                        }

                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
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

//        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {

        updateLocation();
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
