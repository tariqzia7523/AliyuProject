package com.map.checkpost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    EditText edname,edemail,edpassword,edconfirmPassword;
    RadioGroup radioGroup;
    EditText compnayName,companyAddress,companyCountry,companyPhoneNumber,compnayTextNumber,companybillingAddress;
    Spinner companyList;
    ImageView imageView;
    Uri imageUri;
    String TAG,fcmToken,imageUrl;
    ProgressBar progressBar;
    TextView signuptext;
    StorageReference mStorageRef;
    DatabaseReference myRef,myRefCompany;
    int selectedOptionFlag;//1 for owner 2 for employee
    ProgressDialog progressDialog;
    ArrayList<String> companyNames,compnayIds;
    TextView compnayHeading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();
        companyNames=new ArrayList<>();
        compnayIds=new ArrayList<>();
        progressDialog=new ProgressDialog(SignUpActivity.this);
        progressDialog.setMessage(getString(R.string.please_wait));
        mStorageRef = FirebaseStorage.getInstance().getReference();
        myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRefCompany = FirebaseDatabase.getInstance().getReference("Company");
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        getFcmToken();
        selectedOptionFlag=1;
        radioGroup=findViewById(R.id.radio_group);
        compnayName=findViewById(R.id.company_name);
        companyAddress=findViewById(R.id.company_address);
        companyCountry=findViewById(R.id.company_country);
        companyPhoneNumber=findViewById(R.id.company_phone_number);
        compnayTextNumber=findViewById(R.id.company_text_number);
        companybillingAddress=findViewById(R.id.company_billing_address);
        companyList=findViewById(R.id.company_list);
        companyList=findViewById(R.id.company_list);
        compnayHeading=findViewById(R.id.company_heading);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton)group.findViewById(checkedId);
                if(radioButton.getId()==R.id.rb_employee){
                    selectedOptionFlag=2;
                    compnayName.setVisibility(View.GONE);
                    companyAddress.setVisibility(View.GONE);
                    companyCountry.setVisibility(View.GONE);
                    companyPhoneNumber.setVisibility(View.GONE);
                    compnayTextNumber.setVisibility(View.GONE);
                    companybillingAddress.setVisibility(View.GONE);
                    companyList.setVisibility(View.VISIBLE);
                    compnayHeading.setText(getString(R.string.compnay_heading_to_select_company));
                }else if(radioButton.getId()==R.id.rb_owner){
                    selectedOptionFlag=1;
                    compnayName.setVisibility(View.VISIBLE);
                    companyAddress.setVisibility(View.VISIBLE);
                    companyCountry.setVisibility(View.VISIBLE);
                    companyPhoneNumber.setVisibility(View.VISIBLE);
                    compnayTextNumber.setVisibility(View.VISIBLE);
                    companybillingAddress.setVisibility(View.VISIBLE);
                    companyList.setVisibility(View.GONE);
                    compnayHeading.setText(getString(R.string.enter_company_details));
                }
            }
        });

        edname=findViewById(R.id.name);
        edemail=findViewById(R.id.email);
        edpassword=findViewById(R.id.password);
        edconfirmPassword=findViewById(R.id.confrim_password);
        imageView=findViewById(R.id.profile_image);
        progressBar=findViewById(R.id.progress_bar);
        signuptext=findViewById(R.id.login_text);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 100);
            }
        });
        findViewById(R.id.login_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this,LoginActivity.class));

            }
        });
        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name=edname.getText().toString();
                final String email=edemail.getText().toString();
                String pass=edpassword.getText().toString();
                String conPass=edconfirmPassword.getText().toString();
                if(pass.equals(conPass)){
                    if(!name.isEmpty() || !email.isEmpty()){
                        if(imageUri==null){
                            Toast.makeText(getApplicationContext(),getString(R.string.add_an_image_to),Toast.LENGTH_LONG).show();
                            return;
                        }
                        if(selectedOptionFlag==1){
//                            compnayName,companyAddress,companyCountry,companyPhoneNumber,CompnayTextNumber,companybillingAddress
                            if(compnayName.getText().toString().isEmpty() ||
                                    companyAddress.getText().toString().isEmpty() ||
                                    companyCountry.getText().toString().isEmpty() ||
                                    companyPhoneNumber.getText().toString().isEmpty() ||
                                    compnayTextNumber.getText().toString().isEmpty() ||
                                    companybillingAddress.getText().toString().isEmpty()){
                                Toast.makeText(getApplicationContext(),getString(R.string.add_company_data),Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        progressBar.setVisibility(View.VISIBLE);
                        signuptext.setVisibility(View.GONE);
                        mAuth.createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "createUserWithEmail:success");
                                            currentUser = mAuth.getCurrentUser();

                                            final StorageReference riversRef = mStorageRef.child("User/"+currentUser.getUid());
                                            riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            Log.d("image View", "onSuccess: uri= "+ uri.toString());
                                                            imageUrl=uri.toString();
                                                            UserModel userModel=new UserModel();
                                                            userModel.setFcmToken(fcmToken);
                                                            userModel.setName(name);
                                                            userModel.setImageUrl(imageUrl);
                                                            if(selectedOptionFlag==1){
                                                                userModel.setType("Owner");
                                                                userModel.setCompanyName(compnayName.getText().toString());
                                                                CompanyModel companyModel=new CompanyModel();
                                                                companyModel.setCompanyName(compnayName.getText().toString());
                                                                companyModel.setAddress(companyAddress.getText().toString());
                                                                companyModel.setBillingAddress(companybillingAddress.getText().toString());
                                                                companyModel.setCountry(companyCountry.getText().toString());
                                                                companyModel.setPhoneNumber(companyPhoneNumber.getText().toString());
                                                                companyModel.setTextNumber(compnayTextNumber.getText().toString());
                                                                userModel.setCompanyId(myRefCompany.push().getKey());

                                                                myRefCompany.child(userModel.getCompanyId()).setValue(companyModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {

                                                                    }
                                                                });

                                                            }else if(selectedOptionFlag==2){
                                                                userModel.setType("Employee");
                                                                userModel.setCompanyName(companyList.getSelectedItem().toString());
                                                                userModel.setCompanyId(compnayIds.get(companyList.getSelectedItemPosition()));
                                                            }

                                                            myRef.child(currentUser.getUid()).setValue(userModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    progressBar.setVisibility(View.GONE);
                                                                    signuptext.setVisibility(View.VISIBLE);
                                                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK));

                                                                }
                                                            }).addOnCanceledListener(new OnCanceledListener() {
                                                                @Override
                                                                public void onCanceled() {
                                                                    progressBar.setVisibility(View.GONE);
                                                                    signuptext.setVisibility(View.VISIBLE);
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });


                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                            Toast.makeText(SignUpActivity.this, getString(R.string.authentation_failed),
                                                    Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                            signuptext.setVisibility(View.VISIBLE);

                                        }

                                        // ...
                                    }
                                });

//                        UserModel userModel=new UserModel(name,email,gender,age)

                    }else{
                        Toast.makeText(getApplicationContext(),getString(R.string.please_add_all_data),Toast.LENGTH_LONG).show();

                    }
                }else{
                    Toast.makeText(getApplicationContext(),getString(R.string.password_not_match),Toast.LENGTH_LONG).show();
                }
            }
        });
        progressDialog.show();
        myRefCompany.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    progressDialog.dismiss();
                    companyNames.clear();
                    for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            companyNames.add(snapshot1.child("companyName").getValue(String.class));
                            compnayIds.add(snapshot1.getKey());
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SignUpActivity.this, android.R.layout.simple_spinner_item, companyNames);
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    companyList.setAdapter(arrayAdapter);
                }catch (Exception c){
                    c.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });

    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            try {
                imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(SignUpActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
            }

        }else {
//            Toast.makeText(SignUpActivity.this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }


    public void getFcmToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        fcmToken = task.getResult().getToken().toString();

                        // Log and toast
//                        String msg = getString(R.string.msg_token_fmt, token);
//                        Log.d(TAG, msg);
//                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}