package com.map.checkpost;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;


public class CheckPointsFragment extends Fragment  {


    DatabaseReference myRefCheckPoints,myRefUser;
    ProgressDialog progressDialog;
    FirebaseUser firebaseUser;
    RecyclerView recyclerView;
    MyAdapter myAdapter;
    ArrayList<CheckPointsModel> checkModels;
    UserModel userModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_check_points, container, false);
        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.please_wait));
//        myRefselectedQuestions = FirebaseDatabase.getInstance().getReference("SelectedQuestions");
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        myRefCheckPoints = FirebaseDatabase.getInstance().getReference("CheckPoints");
        myRefUser = FirebaseDatabase.getInstance().getReference("Users");
        recyclerView=v.findViewById(R.id.check_points_list);
        checkModels=new ArrayList<>();
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
//        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        myAdapter=new MyAdapter(getContext(),getActivity(),checkModels);
        recyclerView.setAdapter(myAdapter);
        progressDialog.show();
        myRefUser.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    userModel=snapshot.getValue(UserModel.class);
                    if(userModel.getType().equalsIgnoreCase("Employee")){
                        v.findViewById(R.id.add_checkpoints).setVisibility(View.GONE);
                    }
                    myRefCheckPoints.child(userModel.getCompanyId()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try{
                                checkModels.clear();
                                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                    CheckPointsModel postModel=dataSnapshot.getValue(CheckPointsModel.class);
                                    postModel.setId(dataSnapshot.getKey());
                                    checkModels.add(postModel);
                                }
                                myAdapter.notifyDataSetChanged();
                                progressDialog.dismiss();
                            }catch (Exception c){
                                c.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }catch (Exception c){
                    c.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        v.findViewById(R.id.add_checkpoints).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),AddCheckPointsActivity.class).putExtra("list",checkModels));
            }
        });

//
//
//


        return v;
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
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    ArrayList<CheckPointsModel> data;
    Context context;
    Activity activity;
    String TAG;
    public class MyViewHolder extends RecyclerView.ViewHolder  {
        TextView name, delete,dateandTime,title;
        public MyViewHolder(View view) {
            super(view);
//                sideImage=view.findViewById(R.id.side_image);
            name=view.findViewById(R.id.name);
            delete=view.findViewById(R.id.delete_text);
            dateandTime=view.findViewById(R.id.date_and_time);
            title=view.findViewById(R.id.title);


        }
    }
    public MyAdapter(Context c, Activity a , ArrayList<CheckPointsModel> moviesList){
        this.data =moviesList;
        context=c;
        activity=a;
        TAG="***Adapter";
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.check_points_item, parent, false);
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
//        if (flag==1){// beign called from my profile so we have to set visible following image
//            holder.menuImage.setVisibility(View.VISIBLE);
//        }

        holder.name.setText(data.get(position).getName());
        holder.dateandTime.setText(data.get(position).getDateAndTime());
        holder.title.setText(data.get(position).getTitle());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                myRefCheckPoints.child(userModel.getCompanyId()).child(data.get(position).getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(),getString(R.string.check_point_added),Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    @Override
    public int getItemCount() {
//        return  5;
        return data.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
}


}
