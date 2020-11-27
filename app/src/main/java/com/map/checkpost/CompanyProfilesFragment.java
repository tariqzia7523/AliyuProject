package com.map.checkpost;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class CompanyProfilesFragment extends Fragment  {


    DatabaseReference myRefCompany;
    ProgressDialog progressDialog;
    FirebaseUser firebaseUser;
    UserModel userModel;
    CompanyModel companyModel;
    EditText companyName,companyAddress,companyCountry,companyPhoneNumber,companyTextNumber,companyBillingAddress;
    TextView loginText;
    ProgressBar progressBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_company_profile, container, false);
        progressDialog=new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.please_wait));
//        myRefselectedQuestions = FirebaseDatabase.getInstance().getReference("SelectedQuestions");
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        companyName=v.findViewById(R.id.company_name);
        companyAddress=v.findViewById(R.id.company_address);
        companyCountry=v.findViewById(R.id.company_country);
        companyPhoneNumber=v.findViewById(R.id.company_phone_number);
        companyTextNumber=v.findViewById(R.id.company_text_number);
        companyBillingAddress=v.findViewById(R.id.company_billing_address);
        loginText=v.findViewById(R.id.login_text);
        progressBar=v.findViewById(R.id.progress_bar);
        try{
            userModel =(UserModel)getArguments().getSerializable("data");
            if(userModel.getType().equalsIgnoreCase("Employee")){
                v.findViewById(R.id.login_button).setVisibility(View.GONE);
                companyName.setFocusable(false);
                companyAddress.setFocusable(false);
                companyCountry.setFocusable(false);
                companyPhoneNumber.setFocusable(false);
                companyTextNumber.setFocusable(false);
                companyBillingAddress.setFocusable(false);
            }
        }catch (Exception c){
            c.printStackTrace();
        }



        myRefCompany = FirebaseDatabase.getInstance().getReference("Company").child(userModel.getCompanyId());
        progressDialog.show();
        myRefCompany.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    progressDialog.dismiss();
                    companyModel=snapshot.getValue(CompanyModel.class);
                    companyModel.setId(snapshot.getKey());
                    companyName.setText(companyModel.getCompanyName());
                    companyAddress.setText(companyModel.getAddress());
                    companyCountry.setText(companyModel.getCountry());
                    companyPhoneNumber.setText(companyModel.getPhoneNumber());
                    companyTextNumber.setText(companyModel.getTextNumber());
                    companyBillingAddress.setText(companyModel.getBillingAddress());

                }catch (Exception c){
                    c.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        v.findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!userModel.getType().equalsIgnoreCase("Employee")){
                    if(companyAddress.getText().toString().isEmpty() ||
                            companyBillingAddress.getText().toString().isEmpty() ||
                            companyCountry.getText().toString().isEmpty() ||
                            companyName.getText().toString().isEmpty() ||
                            companyPhoneNumber.getText().toString().isEmpty() ||
                            companyTextNumber.getText().toString().isEmpty()){
                        Toast.makeText(getActivity(),getString(R.string.please_add_all_data),Toast.LENGTH_LONG).show();
                        return;

                    }
                    progressDialog.show();
                    CompanyModel companyModel=new CompanyModel();
                    companyModel.setAddress(companyAddress.getText().toString());
                    companyModel.setTextNumber(companyTextNumber.getText().toString());
                    companyModel.setBillingAddress(companyBillingAddress.getText().toString());
                    companyModel.setPhoneNumber(companyPhoneNumber.getText().toString());
                    companyModel.setCountry(companyCountry.getText().toString());
                    companyModel.setCompanyName(companyName.getText().toString());
                    myRefCompany.setValue(companyModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),getString(R.string.data_updated),Toast.LENGTH_LONG).show();
                        }
                    });
                }



            }
        });




        return v;
    }






}
