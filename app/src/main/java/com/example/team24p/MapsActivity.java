
package com.example.team24p;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener {


    //Maps Activity variables
    private GoogleMap mMap;
    private static final int REQUEST_PERMISSION_LOCATION = 255;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRef = mDatabase.getReference().child("locations");
    private LocationManager lm;
    private Location myLocation;
    private String userNameLoggedIn;
    Marker marker;
    String isAdmin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        userNameLoggedIn= getIntent().getStringExtra("userNameLoggedIn");
        isAdmin = getIntent().getStringExtra("isAdmin");
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia, By Default
        // and move the map's camera to the same location.
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 8);
            }
        }
        else if (myLocation == null) {
            lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 1000, 1, this);
            myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            mMap.setMyLocationEnabled(true);

        }

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Getting sports locations from The Server To ArrayLists
                //use the location in loop to show them on screen by every lat&lon of the locations
                ArrayList<Map<String, String>> locations = (ArrayList<Map<String, String>>) dataSnapshot.getValue();
                ArrayList<LatLng> latLngList = new ArrayList<LatLng>();
                ArrayList<String> names = new ArrayList<>();
                ArrayList<String> types = new ArrayList<>();
                for (Map<String, String> entry : locations)
                {
                    Double lat = 0.0;
                    Double lon = 0.0;
                    for (String key : entry.keySet()) {
                        String value = entry.get(key);
                        if (key.equals("lat")){
                            lat = Double.parseDouble(value);
                        }
                        else if (key.equals("lon")){
                            lon = Double.parseDouble(value);
                        }
                        else if (key.equals("Name")){
                            names.add(value);
                        }
                        else if(key.equals("Type")){
                            types.add(value);
                        }
                    }       // if location OK Add new Lating
                    if(lat!=0.0)latLngList.add(new LatLng(lat, lon));
                }
                int i =0;
                for (LatLng latLng : latLngList) {

                    int height = 200;
                    int width = 100;
                    Bitmap smallMarker = null;
                    // Using Bitmap Drawable to show different Icons on the location
                    // by Location-Type: If Soccer- show Soccer Ball Icon
                    MarkerOptions marker = new MarkerOptions().position(latLng)
                            .title(names.get(i)).snippet("לחץ כאן כדי לפתוח משחק חדש!");
                    // by Location-Type: If Soccer- show marker_basketball Icon
                    if(types.get(i).equals("כדורסל")) {
                        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_basketball);
                        Bitmap b = bitmapdraw.getBitmap();
                        smallMarker= Bitmap.createScaledBitmap(b, width, height, false);
                    }
                    else if(types.get(i).equals("כדורגל")) {
                        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_soccer);
                        Bitmap b = bitmapdraw.getBitmap();
                        smallMarker= Bitmap.createScaledBitmap(b, width, height, false);
                    }

                    // by Location-Type: If Soccer- show Tennis Icon
                    else if(types.get(i).equals("טניס")){

                        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_tennis);
                        Bitmap b = bitmapdraw.getBitmap();
                        smallMarker= Bitmap.createScaledBitmap(b, width, height, false);

                    } // by Location-Type: If Hybrid Icon
                    else if(types.get(i).equals("משולב")) {
                        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_both);
                        Bitmap b = bitmapdraw.getBitmap();
                        smallMarker= Bitmap.createScaledBitmap(b, width, height, false);
                    }
                    if(smallMarker!=null)marker.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                    mMap.addMarker(marker);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    i++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            // After User Clock On location Store And Send the important details
            //to Games Activity Class By Itenet
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MapsActivity.this, GamesActivity.class);
                intent.putExtra("markerName", marker.getTitle());
                // Checks That User STILL LOGGED IN
                intent.putExtra("userNameLoggedIn", userNameLoggedIn);
                intent.putExtra("isAdmin",isAdmin);
                startActivity(intent);

            }
        });

    }

    @Override   // Change The Camera Screen by chenge the position touching the map
    public void onLocationChanged(Location location) {
        myLocation = location;
        double myLatitude = myLocation.getLatitude();
        double myLongitude = myLocation.getLongitude();
        LatLng latlng = new LatLng(myLatitude, myLongitude);
        float zoomLevel = 16.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoomLevel));


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

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override       // Ask Permission Work With googlesLocation
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We now have permission to use the location
            }
        }
    }
}


