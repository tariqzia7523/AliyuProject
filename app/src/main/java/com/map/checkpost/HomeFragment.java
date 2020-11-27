package com.map.checkpost;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.Context.LOCATION_SERVICE;


public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {


    DatabaseReference myRefPosts,myRefUser;
    ProgressDialog progressDialog;
    FirebaseUser firebaseUser;
//    RecyclerView recyclerView;
//    MyAdapter myAdapter;
    private GoogleMap mMap;
    private MarkerOptions currentMarkerOption;
    private Location curLocation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.home_fragment, container, false);
        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.please_wait));
//        myRefselectedQuestions = FirebaseDatabase.getInstance().getReference("SelectedQuestions");
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        myRefPosts = FirebaseDatabase.getInstance().getReference("Posts");
        myRefUser = FirebaseDatabase.getInstance().getReference("Users");
//        recyclerView=v.findViewById(R.id.posts_list);
//        postModels=new ArrayList<>();
//        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
////        linearLayoutManager.setReverseLayout(true);
//        recyclerView.setLayoutManager(linearLayoutManager);
//        myAdapter=new MyAdapter(getContext(),getActivity(),postModels);
//        recyclerView.setAdapter(myAdapter);
//        progressDialog.show();
//        myRefPosts.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                try{
//                    postModels.clear();
//                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
//                        PostModel postModel=dataSnapshot.getValue(PostModel.class);
//                        postModel.setId(dataSnapshot.getKey());
//                        if(dataSnapshot.child("likes").child(firebaseUser.getUid()).exists()){
//                            postModel.setUserliked(true);
//                        }else{
//                            postModel.setUserliked(false);
//                        }
//                        postModels.add(postModel);
//                    }
//                    myAdapter.notifyDataSetChanged();
//                    progressDialog.dismiss();
//                }catch (Exception c){
//                    c.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//
//
//

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        LocationManager service = (LocationManager)getActivity().getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

// check if enabled and if not send user to the GSP settings
// Better solution would be to display a dialog and suggesting to
// go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
//        v.findViewById(R.id.add_checkpoints).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(getActivity(),AddCheckPointsActivity.class));
//            }
//        });
        return v;
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
        mMap.setMyLocationEnabled(true);
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(final Location location) {
                        // Got last known location. In some rare situations this can be null.

                        if (location != null) {
                            curLocation = location;

                            LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
                            currentMarkerOption = new MarkerOptions().position(sydney).title(getString(R.string.current_location))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_32));
                            mMap.addMarker(currentMarkerOption);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12.0f));
//                            Toast.makeText(getActivity(),"called",Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


//
//    public static void sendNotification(final String fcmToken, final String title, final String body, final String p1,final String osType) {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//                    Log.e("***sender","player one id 1 "+p1);
//                    MediaType JSON
//                            = MediaType.parse("application/json; charset=utf-8");
//                    OkHttpClient client = new OkHttpClient();
//                    JSONObject json = new JSONObject();
//                    JSONObject dataJson = new JSONObject();
//                    dataJson.put("body", body);
//                    dataJson.put("title", title);
////                    dataJson.put("PlayerOne", p2);
//                    dataJson.put("otherid", p1);
//
//                    dataJson.put("type", "game");
//
//                    json.put("data", dataJson);
//                    if(osType.equalsIgnoreCase("Apple"))
//                        json.put("notification",dataJson);
//                    json.put("to", fcmToken);
//                    Log.e("finalResponse","token is "+fcmToken);
//                    RequestBody body = RequestBody.create(JSON, json.toString());
//                    Request request = new Request.Builder()
////                        .header("Authorization", "key=AIzaSyANhp-yl7w4fKmgD-cuV_7U72CKCb3UA78") //Legacy Server Key
//                            .header("Authorization", "key=AAAAScXMytg:APA91bEPcIs8kWEZPbJ9RKa3_YJ7XAq7loCNKSaDvjqB61mpqK_KkLLj-Auw-L_zcI5R_1zfBdDERVbhVZ1Q44dtYAbUaT3RQmhPUP_TaNjg1-L_Z-308Dq9G_f1aqCy1vLmpI2tZ1dk") //Legacy Server Key
//                            .url("https://fcm.googleapis.com/fcm/send")
//                            .post(body)
//                            .build();
//                    Response response = client.newCall(request).execute();
//                    String finalResponse = response.body().string();
//                    Log.e("finalResponse", finalResponse);
//
//
//
//                } catch (Exception e) {
//                    //Log.d(TAG,e+"");
//                    e.printStackTrace();
//                }
//                return null;
//            }
//        }.execute();
//    }
//public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
//    ArrayList<PostModel> data;
//    Context context;
//    Activity activity;
//    String TAG;
//    public class MyViewHolder extends RecyclerView.ViewHolder  {
//        TextView name, description,title;
//        ImageView profileImage,likeImage,mainImage;
//        public MyViewHolder(View view) {
//            super(view);
////                sideImage=view.findViewById(R.id.side_image);
//            name=view.findViewById(R.id.name);
//            description=view.findViewById(R.id.discription);
//            title=view.findViewById(R.id.title);
//            profileImage=view.findViewById(R.id.profile_image);
//            likeImage=view.findViewById(R.id.heart_image);
//            mainImage=view.findViewById(R.id.image);
//
//
//
//        }
//    }
//    public MyAdapter(Context c, Activity a , ArrayList<PostModel> moviesList){
//        this.data =moviesList;
//        context=c;
//        activity=a;
//        TAG="***Adapter";
//    }
//    @Override
//    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View itemView = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.post_item, parent, false);
//        return new MyViewHolder(itemView);
//    }
//    @Override
//    public void onBindViewHolder(final MyViewHolder holder, final int position) {
////        if (flag==1){// beign called from my profile so we have to set visible following image
////            holder.menuImage.setVisibility(View.VISIBLE);
////        }
//
//        Log.e(TAG,"user id "+data.get(position).getUserid());
//        myRefUser.child(data.get(position).getUserid()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                try{
//                    UserModel userModel=dataSnapshot.getValue(UserModel.class);
//                    holder.name.setText(userModel.getName());
//                    Glide.with(activity).load(userModel.getImageUrl()).into(holder.profileImage);
//                }catch (Exception c){
//                    c.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                databaseError.toException().printStackTrace();
//            }
//        });
//        holder.title.setText(data.get(position).getTitle());
//        holder.description.setText(data.get(position).getDescription());
//        if(data.get(position).getPostType().equalsIgnoreCase("withImage")){
//            holder.mainImage.setVisibility(View.VISIBLE);
//            Glide.with(activity).load(data.get(position).getImageUrl()).into(holder.mainImage);
//        }
//        holder.likeImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if(data.get(position).isUserliked()){
//                    myRefPosts.child(data.get(position).getId()).child("likes").child(firebaseUser.getUid()).removeValue();
//                }else{
//                    myRefPosts.child(data.get(position).getId()).child("likes").child(firebaseUser.getUid()).setValue(true);
//                }
//                data.get(position).setUserliked(!data.get(position).isUserliked());
//                notifyDataSetChanged();
//            }
//        });
//        if(data.get(position).isUserliked()){
//            holder.likeImage.setImageResource(R.drawable.liked);
//        }else{
//            holder.likeImage.setImageResource(R.drawable.not_liked);
//        }
//
//
//
////        Log.e(TAG,"id is "+data.get(position));
//
//
//    }
//    @Override
//    public int getItemCount() {
////        return  5;
//        return data.size();
//    }
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//}


}
