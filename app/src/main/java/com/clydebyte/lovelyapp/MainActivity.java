package com.clydebyte.lovelyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;

import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;
import java.util.List;


public class MainActivity extends AppCompatActivity  {

   private static final int PERMISSION_FINE = 100;

    TextView tv_lad, tv_lon, tv_altitude, tv_speed,tv_sensor,tv_accuracy,tv_address,tv_updates;

    Switch sw_locationupdates, sw_gps;

    Boolean onUpdateOn =false;

    // location request config file

    LocationRequest locationRequest;
    LocationCallback locationCallback;

    // google API for
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        tv_lad = findViewById(R.id.tv_lat);
         tv_lon= findViewById(R.id.tv_lon);
         tv_altitude= findViewById(R.id.tv_altitude);
         tv_speed= findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_address = findViewById(R.id.tv_address);
        tv_updates = findViewById(R.id.tv_updates);

        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);

        //set all properties of Location Request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        createLocationRequest();

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...

            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;

                        resolvable.startResolutionForResult(MainActivity.this,
                                100);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

    //   locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sw_gps.isChecked()){

                  //  locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS");
                }else
                {
                    //locationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Wifi or GSM");
                }

            }
        });
  updateGPS ();


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    tv_lon.setText("NOT NULL");
                }else {
                    tv_lon.setText("SHIT STILL NULL");
                }
            }

        };

    }// end of onCreate

    private void createLocationRequest() {
        LocationRequest.Builder locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY,Priority.PRIORITY_HIGH_ACCURACY);

        locationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setIntervalMillis(Priority.PRIORITY_HIGH_ACCURACY);
    }

    private void updateGPS (){
        // get user permission
        // get current location
        // update UI.
        CancellationToken cancellationToken = new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY,cancellationToken).addOnSuccessListener(this, location -> {
                    //we got permissions, put location and set it to UI components
                updateUIValues(location);


            });
        } else{
            //permission not granted
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_FINE);
            }
        }

    }

   void updateUIValues( Location location) {

        Geocoder geocoder = new Geocoder(MainActivity.this);
        try { List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            String myLocation_1 = (addresses.get(0).getAdminArea());
            String myLocation_2 = (addresses.get(0).getAddressLine(0));
            String myLocation_3 = (addresses.get(0).getLocality());
            tv_address.setText(myLocation_1+" : "+myLocation_3);

        }catch(Exception e){
            tv_address.setText("No address available");
        }

         try {

             tv_lad.setText(String.valueOf(location.getLatitude()));
         } catch (NullPointerException e){

             tv_lad.setText("No latitude");
         }

        try {
            tv_lon.setText(String.valueOf(location.getLongitude()));
        } catch (NullPointerException e){
            tv_lon.setText("No longitude");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        switch (requestCode){

            case PERMISSION_FINE:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }else {
                    Toast.makeText(this, "ACCESS DENIED", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
}