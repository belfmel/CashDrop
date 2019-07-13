package com.example.cashdrop;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FindUsers extends AppCompatActivity {
    Button signOutBtn;
    Button updateCard;
    TextView userName;
//    TextView latitudeView;
//    TextView longitudeView;

    LocationManager locationManager;
    String locationProvider;

    double latitude;
    double longitude;

    //testing purposes
    String lat;
    String lon;

    // reference for the fireBase auth library
    private FirebaseAuth mAuth;
    // Listener that detects changes in the user's current authentication state
    private FirebaseAuth.AuthStateListener mAuthListener;

    // For Database
    private DatabaseReference mDatabaseReference;
    private DatabaseReference otherDatabaseReference;

    //user email
    private String userEmail;
    private String tempEmail;

    //array of people close by
    ArrayList<String> peopleCloseBy = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users);

        //get a reference to the FireBase auth object
        mAuth = FirebaseAuth.getInstance();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        signOutBtn = (Button) findViewById(R.id.signoutBtn);
        updateCard = (Button) findViewById(R.id.updateCard);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationProvider = LocationManager.GPS_PROVIDER;
        locationManager.requestLocationUpdates(locationProvider, 5000, 10, locationListener);

        // log out button
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUserOut();
            }
        });
        updateCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FindUsers.this, creditCardUpdate.class));
            }
        });


    }


    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
//                                peopleCloseBy.clear();
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            lat = Double.toString(latitude);
            lon = Double.toString(longitude);

//            latitudeView.setText(lat);
//            longitudeView.setText(lon);

            // update the database with the new gps coordinate of the user
            mDatabaseReference.child("Latitude").setValue(lat);
            mDatabaseReference.child("Longitude").setValue(lon);

            // search through database to compare other users latitude and longitude and see if it's
            // within a certain distance and then display their name on the screen.
            otherDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Map<String, Object> userEmails = (Map<String, Object>) dataSnapshot.getValue();
                    List keys = new ArrayList(userEmails.keySet());
                    int i = 0;

                    for(DataSnapshot emailSnapshot : dataSnapshot.getChildren()){
                        Object obj = keys.get(i);
                        String currUserEmail = obj.toString();
//                       Log.d("TAG: OBJECT USER", currUserEmail);

                        Map<String, Object> user = (Map<String, Object>) emailSnapshot.getValue();
                        String otherLat = (String) user.get("Latitude");
                        String otherLong = (String) user.get("Longitude");
                        double latVal = Double.parseDouble(otherLat);
                        double longVal = Double.parseDouble(otherLong);
//                       Log.d("TAG: OBJECT USER", otherLat + " " + otherLong);

                        if(currUserEmail.equals(tempEmail)){
//                            Log.d("TAG: EMAIL SIMILAR?", currUserEmail + " " + tempEmail);
                        }else {

                            if (isCloseToAnotherLocation(longVal, latVal, 20)) { // 5 meter range
                                // if true, save emailSnapshot in array to display
//                               Log.d("TAG: CLOSE BY", currUserEmail);
                                if (!peopleCloseBy.contains(currUserEmail)) {
                                    Log.d("TAG: CLOSE BY", currUserEmail);
                                    peopleCloseBy.add(currUserEmail);
                                }
                            }
                        }

                        i++;

                    }
                    initRecyclerView();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);

        if (currentUser != null) {
            userEmail = currentUser.getEmail();
            // FireBase Database
            Log.d("USER_EMAIL", userEmail);
            tempEmail = userEmail.replace('.', '-');
            mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users/" + tempEmail);
            otherDatabaseReference = FirebaseDatabase.getInstance().getReference("Users/");
        }


    }

    @Override
    public void onStop() {
        super.onStop();
        //remove auth listener
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void updateUI(FirebaseUser user) {
        userName = (TextView) findViewById(R.id.welcomeText);
        if (user != null) {
            userName.setText(user.getEmail());
        } else {
            userName.setText("Please Sign Back In");
        }
    }

    /**
     * Sign User out
     */
    private void signUserOut() {
        mAuth.signOut();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            Toast.makeText(FindUsers.this, "Could not sign user out", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(FindUsers.this, "User signed out", Toast.LENGTH_SHORT).show();
            // if signing out was successful, go to log in page
            Intent signInIntent = new Intent(FindUsers.this, LoginActivity.class);
            startActivity(signInIntent);
        }
    }



    public boolean isCloseToAnotherLocation(double longitude, double latitude, double range){
        if(distanceFromNewLocation(longitude, latitude, this.longitude, this.latitude) < range){
            //if the distance from new location is less than the range (is close)
            return true;
        }else{
            return false;
        }
    }
    private double distanceFromNewLocation(double longitude1, double latitude1, double longitude2, double latitude2){
        double theta = longitude1 - longitude2;
        double dist = Math.sin(deg2rad(latitude1))
                * Math.sin(deg2rad(latitude2))
                + Math.cos(deg2rad(latitude1))
                * Math.cos(deg2rad(latitude2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515* 1000.0;
        return (dist); //distance in meters
    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad){
        return (rad * 180.0 / Math.PI);
    }

    private void initRecyclerView() {
        Log.d("RECYCLER VIEW: ", "initRecyclerView: init recyclerview");
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        RecyclerView.Adapter mAdapter = new RecyclerViewAdapter(this, userEmail, peopleCloseBy);
        mRecyclerView.setAdapter(mAdapter);

        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

}
