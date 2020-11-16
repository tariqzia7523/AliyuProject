package com.map.checkpost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    UserModel otherUserModel;
    FirebaseUser firebaseUser;
    DatabaseReference myRefChat;
    String combination;
    ArrayList<ChatModel> chatModels;
    RecyclerView recyclerView;
    MyAdapter myAdapter;
    EditText editText;
    ProgressDialog progressDialog;
    TextView otherUserName;
    ImageView profileImage;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().hide();
        progressDialog=new ProgressDialog(ChatActivity.this);
        progressDialog.setMessage("Please wait");
        otherUserName=findViewById(R.id.user_name);
        profileImage=findViewById(R.id.profile_image);
        try{
            otherUserModel = (UserModel)getIntent().getExtras().get("otherUser");
            otherUserName.setText(otherUserModel.getName());
            Glide.with(ChatActivity.this).load(otherUserModel.getImageUrl()).into(profileImage);
        }catch (Exception c){
            c.printStackTrace();
        }
        editText=findViewById(R.id.edit_text);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        combination = otherUserModel.getId()+"_"+firebaseUser.getUid();
        chatModels=new ArrayList<>();
        myRefChat = FirebaseDatabase.getInstance().getReference("Chats");
        recyclerView = findViewById(R.id.chats);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(ChatActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        myAdapter= new MyAdapter(getApplicationContext(),ChatActivity.this,chatModels);
        recyclerView.setAdapter(myAdapter);

        progressDialog.show();
        myRefChat.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{

                    if(snapshot.child(firebaseUser.getUid()+"_"+otherUserModel.getId()).exists()){
                        combination = firebaseUser.getUid()+"_"+otherUserModel.getId();
                    }else if(snapshot.child(otherUserModel.getId()+"_"+firebaseUser.getUid()).exists()){
                        combination = otherUserModel.getId()+"_"+firebaseUser.getUid();
                    }else{
                        combination = otherUserModel.getId()+"_"+firebaseUser.getUid();
                    }
                    myRefChat.child(combination).addValueEventListener((new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try{
                                progressDialog.dismiss();
                                chatModels.clear();
                                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                    ChatModel chatModel= snapshot1.getValue(ChatModel.class);
                                    chatModel.setId(snapshot1.getKey());
                                    chatModels.add(chatModel);
                                    myAdapter.notifyDataSetChanged();
                                    recyclerView.scrollToPosition(myAdapter.getItemCount() - 1);
                                }
                                
                            }catch (Exception c){
                                c.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    }));
                }catch (Exception c){
                    c.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"Please add message",Toast.LENGTH_LONG).show();
                }else{
                    ChatModel chatModel=new ChatModel();
                    chatModel.setMessage(editText.getText().toString());
                    chatModel.setSenderId(firebaseUser.getUid());
                    chatModel.setTimeStamp(System.currentTimeMillis());
                    progressDialog.show();
                    myRefChat.child(combination).push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            try{

                            }catch (Exception c){
                                c.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        ArrayList<ChatModel> data;
        Context context;
        Activity activity;
        String TAG;
        public class MyViewHolder extends RecyclerView.ViewHolder  {
            TextView incomingmessage,outgoingMessage;

            public MyViewHolder(View view) {
                super(view);
//                sideImage=view.findViewById(R.id.side_image);
                incomingmessage=view.findViewById(R.id.incoming_mesasge);
                outgoingMessage=view.findViewById(R.id.out_comming_mesasge);




            }
        }
        public MyAdapter(Context c, Activity a , ArrayList<ChatModel> moviesList){
            this.data =moviesList;
            context=c;
            activity=a;
            TAG="***Adapter";
        }
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_model, parent, false);
            return new MyAdapter.MyViewHolder(itemView);
        }
        @Override
        public void onBindViewHolder(final MyAdapter.MyViewHolder holder, final int position) {
//        if (flag==1){// beign called from my profile so we have to set visible following image
//            holder.menuImage.setVisibility(View.VISIBLE);
//        }

            if(data.get(position).getSenderId().equalsIgnoreCase(firebaseUser.getUid())){
                holder.incomingmessage.setVisibility(View.GONE);
                holder.outgoingMessage.setVisibility(View.VISIBLE);
                holder.outgoingMessage.setText(data.get(position).getMessage());
            }else{
                holder.outgoingMessage.setVisibility(View.GONE);
                holder.incomingmessage.setVisibility(View.VISIBLE);
                holder.incomingmessage.setText(data.get(position).getMessage());
            }



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