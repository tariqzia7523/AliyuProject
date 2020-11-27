package com.map.checkpost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AddCheckPointsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private MarkerOptions currentMarkerOption;
    private Location curLocation;
    DatabaseReference myRefCheckPoints,myRefUser;
    FirebaseUser firebaseUser;
    UserModel userModel;
    ProgressDialog progressDialog;
    ArrayList<CheckPointsModel> checkPointsModels;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_check_points);
        getSupportActionBar().hide();
        progressDialog=new ProgressDialog(AddCheckPointsActivity.this);
        progressDialog.setMessage(getString(R.string.please_wait));
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

// check if enabled and if not send user to the GSP settings
// Better solution would be to display a dialog and suggesting to
// go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        myRefCheckPoints = FirebaseDatabase.getInstance().getReference("CheckPoints");
        myRefUser = FirebaseDatabase.getInstance().getReference("Users");
        myRefUser.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    userModel=snapshot.getValue(UserModel.class);
                    userModel.setId(snapshot.getKey());
                }catch (Exception c){
                    c.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        try{
            checkPointsModels=(ArrayList<CheckPointsModel>)getIntent().getExtras().get("list");

        }catch (Exception c){
             c.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap = googleMap;
//        mMap.getUiSettings().setScrollGesturesEnabled(false);
//        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(AddCheckPointsActivity.this);
//        if (ActivityCompat.checkSelfPermission(AddCheckPointsActivity.this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(AddCheckPointsActivity.this,
//                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        fusedLocationClient.getLastLocation()
//                .addOnSuccessListener(AddCheckPointsActivity.this, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(final Location location) {
//                        // Got last known location. In some rare situations this can be null.
//
//                        if (location != null) {
//                            curLocation = location;
//
//                            LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
//                            currentMarkerOption = new MarkerOptions().position(sydney).title("Current Location")
//                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
////                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_32));
//                            mMap.addMarker(currentMarkerOption);
//                            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12.0f));
////                            Toast.makeText(AddCheckPointsActivity.this,"called",Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });

        addListToMap();

    }

    @Override
    public void onMapLongClick(LatLng sydney) {
//        LatLng sydney = new LatLng(latLng.getl, location.getLongitude());





        final CheckPointsModel checkPointsModel=new CheckPointsModel();
        checkPointsModel.setLat(sydney.latitude);
        checkPointsModel.setLng(sydney.longitude);
        checkPointsModel.setCompanyid(userModel.getCompanyId());
        checkPointsModel.setOwnerId(userModel.getId());
        checkPointsModel.setName(getAddress(sydney.latitude,sydney.longitude));
        String id=myRefCheckPoints.child(checkPointsModel.getCompanyid()).push().getKey();

        currentMarkerOption = new MarkerOptions().position(sydney).title(checkPointsModel.getName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_32));
        mMap.addMarker(currentMarkerOption);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12.0f));
        QuestionPopup(checkPointsModel,id);





    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    public String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(AddCheckPointsActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
            return add;

            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(AddCheckPointsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return getString(R.string.no_value_found);
        }
    }

    public void addListToMap(){
        mMap.clear();
        for(int i=0;i<checkPointsModels.size();i++){
            LatLng sydney = new LatLng(checkPointsModels.get(i).getLat(), checkPointsModels.get(i).getLng());
            currentMarkerOption = new MarkerOptions().position(sydney).title(checkPointsModels.get(i).getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(currentMarkerOption);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12.0f));
        }



    }


    public int QuestionPopup(final CheckPointsModel checkPointsModel,final String id) {
        AlertDialog.Builder al = new AlertDialog.Builder(AddCheckPointsActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
//        view = inflater.inflate(R.layout.payment_layout, null);
        view = inflater.inflate(R.layout.title_date_and_time_pop_up, null);
        al.setView(view);
        final AlertDialog value = al.create();
        value.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //final ListView lv=new ListView(this);
        final EditText title = view.findViewById(R.id.title);
        final EditText locationName=view.findViewById(R.id.location_name);
        locationName.setText(checkPointsModel.getName());
        final EditText dateTime=view.findViewById(R.id.date_and_time);
        dateTime.setFocusable(false);
        dateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateAndTimePicker(dateTime);
            }
        });

        view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                value.dismiss();
                progressDialog.show();
                checkPointsModel.setTitle(title.getText().toString());
                checkPointsModel.setDateAndTime(dateTime.getText().toString());
                myRefCheckPoints.child(checkPointsModel.getCompanyid()).child(id).setValue(checkPointsModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        checkPointsModels.add(checkPointsModel);
                        addListToMap();
                        Toast.makeText(getApplicationContext(),getString(R.string.check_point_added),Toast.LENGTH_LONG).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                    }
                });
            }
        });



        value.show();
        value.setCancelable(false);
        return 0;
    }

    public void dateAndTimePicker(final EditText textView){

            // Get Current Date
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(AddCheckPointsActivity.this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            textView.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            final Calendar c = Calendar.getInstance();
                            int mHour = c.get(Calendar.HOUR_OF_DAY);
                            int mMinute = c.get(Calendar.MINUTE);

                            // Launch Time Picker Dialog
                            TimePickerDialog timePickerDialog = new TimePickerDialog(AddCheckPointsActivity.this,
                                    new TimePickerDialog.OnTimeSetListener() {

                                        @Override
                                        public void onTimeSet(TimePicker view, int hourOfDay,
                                                              int minute) {

                                            textView.setText(textView.getText().toString()+" "+hourOfDay + ":" + minute);
                                        }
                                    }, mHour, mMinute, false);
                            timePickerDialog.show();
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }


}