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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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


public class EmployeeListFragment extends Fragment  {


    DatabaseReference myRefUser;
    ProgressDialog progressDialog;
    FirebaseUser firebaseUser;
    ArrayList<UserModel> userModels;
    RecyclerView recyclerView;
    MyAdapter myAdapter;
    UserModel curUserModel;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.employee_fragment, container, false);
        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.please_wait));

        try{
            curUserModel =(UserModel) getArguments().get("data");
        }catch (Exception c){
            c.printStackTrace();
        }
        myRefUser = FirebaseDatabase.getInstance().getReference("Users");
        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        userModels=new ArrayList<>();
        recyclerView=v.findViewById(R.id.employee_list);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
//        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        myAdapter=new MyAdapter(getContext(),getActivity(),userModels);
        recyclerView.setAdapter(myAdapter);
        progressDialog.show();
        myRefUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    progressDialog.dismiss();
                    UserModel ownerType =snapshot.child(firebaseUser.getUid()).getValue(UserModel.class);

                    if(ownerType.getType().equals("Owner")){
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            userModel.setId(dataSnapshot.getKey());
                            if((userModel.getCompanyId().equals(ownerType.getCompanyId())) &&  !dataSnapshot.getKey().equals(firebaseUser.getUid())){
                                userModels.add(userModel);
                                myAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }catch (Exception c){
                    c.printStackTrace();
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
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
    ArrayList<UserModel> data;
    Context context;
    Activity activity;
    String TAG;
    public class MyViewHolder extends RecyclerView.ViewHolder  {
        TextView name;
        ImageView profileImage;
        public MyViewHolder(View view) {
            super(view);
//                sideImage=view.findViewById(R.id.side_image);
            name=view.findViewById(R.id.name);
            profileImage=view.findViewById(R.id.profile_image);




        }
    }
    public MyAdapter(Context c, Activity a , ArrayList<UserModel> moviesList){
        this.data =moviesList;
        context=c;
        activity=a;
        TAG="***Adapter";
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
//        if (flag==1){// beign called from my profile so we have to set visible following image
//            holder.menuImage.setVisibility(View.VISIBLE);
//        }

        Glide.with(activity).load(data.get(position).getImageUrl()).into(holder.profileImage);
        holder.name.setText(data.get(position).getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity,ViewEmployeeRouteActivity.class).putExtra("curdata",curUserModel)
                        .putExtra("otherdata",data.get(position)));
            }
        });



//        Log.e(TAG,"id is "+data.get(position));


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
