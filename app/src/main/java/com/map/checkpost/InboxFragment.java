package com.map.checkpost;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InboxFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class InboxFragment extends Fragment {


    FirebaseUser firebaseUser;
    DatabaseReference myRefChat,myRefuser;
    String combination;
    ArrayList<UserModel> userModels;
    RecyclerView recyclerView;
    MyAdapter myAdapter;
    ProgressDialog progressDialog;
    UserModel curUserModel;


    public static InboxFragment newInstance(String param1, String param2) {
        InboxFragment fragment = new InboxFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public InboxFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            curUserModel =(UserModel) getArguments().getSerializable("data");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_inbox, container, false);
        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setMessage("Please wait");
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        myRefChat = FirebaseDatabase.getInstance().getReference("Chats");
        myRefuser = FirebaseDatabase.getInstance().getReference("Users");
        userModels=new ArrayList<>();
        recyclerView = v.findViewById(R.id.chats);
        myAdapter=new MyAdapter(getContext(),getActivity(),userModels);
        recyclerView.setAdapter(myAdapter);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        progressDialog.show();
        myRefuser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot usersnapshot) {
                 try{
                     userModels.clear();
                     myRefChat.addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot snapshot) {
                             try{
                                 progressDialog.dismiss();
                                 for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                     if(dataSnapshot.getKey().contains(firebaseUser.getUid())){
                                         String key=dataSnapshot.getKey();
                                         key=key.replaceAll(firebaseUser.getUid(),"");
                                         key=key.replace("_","");
                                         UserModel userModel=usersnapshot.child(key).getValue(UserModel.class);
                                         userModel.setId(key);
                                         userModels.add(userModel);
                                         myAdapter.notifyDataSetChanged();
                                     }
                                 }
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

        return v;
    }


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
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_item, parent, false);
            return new MyAdapter.MyViewHolder(itemView);
        }
        @Override
        public void onBindViewHolder(final MyAdapter.MyViewHolder holder, final int position) {
//        if (flag==1){// beign called from my profile so we have to set visible following image
//            holder.menuImage.setVisibility(View.VISIBLE);
//        }

            Glide.with(activity).load(data.get(position).getImageUrl()).into(holder.profileImage);
            holder.name.setText(data.get(position).getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(activity,ChatActivity.class).putExtra("curdata",curUserModel)
                            .putExtra("otherUser",data.get(position)));
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