package com.map.checkpost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ViewEmployeeRouteActivity extends AppCompatActivity implements OnMapReadyCallback , GoogleMap.OnMarkerClickListener{
    DatabaseReference myRefLocation,myRefCheckPoints;
    UserModel curUserModel,otherUserModel;
    ArrayList<LocationModel> locationModels;
    ArrayList<CheckPointsModel> checkPoints;
    ArrayList<Marker> checkPointsMarkers;
    ProgressDialog progressDialog;
    GoogleMap mMap;
    ImageView profileImage;
    TextView name;
    ArrayList<LatLng> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_employee_route);
        getSupportActionBar().hide();
        progressDialog=new ProgressDialog(ViewEmployeeRouteActivity.this);
        progressDialog.setMessage(getString(R.string.please_wait));
        profileImage=findViewById(R.id.profile_image);
        name=findViewById(R.id.name);
        locationModels=new ArrayList<>();
        list=new ArrayList<>();
        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        myRefLocation = FirebaseDatabase.getInstance().getReference("Locations");
        myRefCheckPoints = FirebaseDatabase.getInstance().getReference("CheckPoints");
        try{
            curUserModel=(UserModel)getIntent().getExtras().get("curdata");
            otherUserModel=(UserModel)getIntent().getExtras().get("otherdata");
            name.setText(otherUserModel.getName());
            Glide.with(ViewEmployeeRouteActivity.this).load(otherUserModel.getImageUrl()).into(profileImage);
        }catch (Exception c){
            c.printStackTrace();
        }

        checkPoints=new ArrayList<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ViewEmployeeRouteActivity.this,ChatActivity.class).putExtra("otherUser",otherUserModel));
            }
        });

        findViewById(R.id.clear_route).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewEmployeeRouteActivity.this);
                alertDialogBuilder.setMessage(getString(R.string.are_you_sure_to_delete_route));
                        alertDialogBuilder.setPositiveButton(getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        progressDialog.show();
                                        myRefLocation.child(curUserModel.getCompanyId()).child(otherUserModel.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressDialog.dismiss();
                                            }
                                        });

                                    }
                                });

                alertDialogBuilder.setNegativeButton(getString(R.string.no),new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        progressDialog.show();
        mMap=googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        myRefLocation.child(curUserModel.getCompanyId()).child(otherUserModel.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    mMap.clear();
                    locationModels.clear();
                    list=new ArrayList<>();

                    progressDialog.dismiss();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        LocationModel locationModel=dataSnapshot.getValue(LocationModel.class);
                        locationModel.setId(dataSnapshot.getKey());
                        locationModels.add(locationModel);
                        list.add(new LatLng(locationModel.getLat(),locationModel.getLng()));
//                        LatLng sydney = new LatLng(locationModel.getLat(), locationModel.getLng());
//                        mMap.addMarker(new MarkerOptions().position(sydney).title(locationModel.getAddress()));
////                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15.0f));
                    }
                    try{
                        drawPolyLineOnMap(list);
                    }catch (Exception c){
                        c.printStackTrace();
                    }

                    getCheckPoints();


                }catch (Exception c){
                    c.printStackTrace();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    public void drawPolyLineOnMap(ArrayList<LatLng> list) throws Exception{
        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(Color.BLUE);
        polyOptions.width(10);
        polyOptions.addAll(list);

        mMap.clear();
        mMap.addPolyline(polyOptions);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : list) {
            builder.include(latLng);
        }

        final LatLngBounds bounds = builder.build();

        //BOUND_PADDING is an int to specify padding of bound.. try 100.
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
        mMap.animateCamera(cu);
    }



    private void getCheckPoints() {
        myRefCheckPoints.child(curUserModel.getCompanyId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    checkPoints.clear();
                    checkPointsMarkers=new ArrayList<>();
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        CheckPointsModel postModel=dataSnapshot.getValue(CheckPointsModel.class);
                        postModel.setId(dataSnapshot.getKey());
                        checkPoints.add(postModel);
                        LatLng sydney = new LatLng(postModel.getLat(), postModel.getLng());
                        Marker marker=mMap.addMarker(new MarkerOptions().position(sydney).title(postModel.getName()));
                        marker.setTag(postModel);
                        checkPointsMarkers.add(marker);
//                        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15.0f));
                    }
                    addheckPointsOnMap();
                    progressDialog.dismiss();
                }catch (Exception c){
                    c.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void addheckPointsOnMap(){
        for(int i = 0 ; i<checkPointsMarkers.size() ; i++){
            float minDis=-1;
            long passingtime=0;
            for(int j = 0 ; j < locationModels.size() ; j++){
                Location checkPoint = new Location("checkPoint");
                checkPoint.setLatitude(checkPointsMarkers.get(i).getPosition().latitude);
                checkPoint.setLongitude(checkPointsMarkers.get(i).getPosition().longitude);
                Location userLocation = new Location("userLocation");
                userLocation.setLatitude(locationModels.get(j).getLat());
                userLocation.setLongitude(locationModels.get(j).getLng());
                float distance=checkPoint.distanceTo(userLocation);
                if(distance<=10 && distance!=minDis){
                    minDis=distance;
                    try{
                        passingtime=locationModels.get(j).getPassedTime();
                    }catch (Exception c){
                        c.printStackTrace();
                        passingtime=0;
                    }
                }
            }
            if(minDis>-1 && minDis<=10){
                checkPoints.get(i).setPassed(true);
                checkPoints.get(i).setPassedTime(passingtime+"");
                checkPointsMarkers.get(i).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                checkPointsMarkers.get(i).setTag(checkPoints.get(i));

            }

        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        CheckPointsModel model = (CheckPointsModel) (marker.getTag());
        if(model!=null){
            dialogPopUp(model);
        }else{

        }
        return false;
    }


   public void dialogPopUp(CheckPointsModel checkPointsModel){

       View view = getLayoutInflater().inflate( R.layout.check_points_item_status, null);
       final AlertDialog.Builder dialog = new AlertDialog.Builder( this );
       dialog.setView(view);
       final AlertDialog alertDialog = dialog.create();
       TextView title=view.findViewById(R.id.title);
       TextView name=view.findViewById(R.id.name);
       TextView date_and_time=view.findViewById(R.id.date_and_time);
       TextView status=view.findViewById(R.id.status);
       TextView user_time=view.findViewById(R.id.user_time);
       TextView ok=view.findViewById(R.id.delete_text);
       ok.setText(getString(R.string.ok));
       ok.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               alertDialog.dismiss();
           }
       });
       title.setText(checkPointsModel.getTitle());
       name.setText(checkPointsModel.getName());
       date_and_time.setText(checkPointsModel.getDateAndTime());
       if(checkPointsModel.isPassed()){
           status.setText(getString(R.string.status)+ getString(R.string.client_passed));
       }else{
           status.setText(getString(R.string.status)+ getString(R.string.clients_not_passed_yet));
       }
       try{
           SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
           String dateString = formatter.format(new Date(Long.parseLong(checkPointsModel.getPassedTime())));
           user_time.setText(getString(R.string.user_passes_at)+  dateString);
       }catch (Exception c){
           c.printStackTrace();
           user_time.setText(getString(R.string.user_passes_time_not_avalible));
       }


       alertDialog.show();
   }

}