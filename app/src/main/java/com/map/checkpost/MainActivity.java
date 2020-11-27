package com.map.checkpost;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends FragmentActivity  {


    DrawerLayout drawer;
    ImageView profileImage;
    DatabaseReference myRef;
    FirebaseUser currentUser;
    ProgressDialog progressDialog;
    static UserModel currentUserModel;
    ProgressBar progressBarImage;
    TextView userName;
    UserModel userModel;

    ImageView homeImage,inboxImage,employeImage,checkpointimage,mycompanyImage;
    TextView homeText,inboxText,employeText,checkpointText,mycompanyText;
    RelativeLayout employeelistLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getSupportActionBar().hide();
        drawer = findViewById(R.id.drawer_layout);
        progressDialog=new ProgressDialog(MainActivity.this);
        progressDialog.setMessage(getString(R.string.please_wait));
        myRef = FirebaseDatabase.getInstance().getReference("Users");
        currentUser= FirebaseAuth.getInstance().getCurrentUser();

        findViewById(R.id.menu_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);

            }
        });
        drawerInit();
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        progressDialog.show();
        myRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                try{
                    userModel=snapshot.getValue(UserModel.class);
                    userModel.setId(snapshot.getKey());
                    currentUserModel=userModel;
                    userName.setText(currentUserModel.getName());
                    Glide.with(MainActivity.this).load(userModel.getImageUrl())
                            .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBarImage.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBarImage.setVisibility(View.GONE);
                            return false;
                        }
                    }).into(profileImage);

                    if(currentUserModel.getType().equals("Employee")){
                        employeelistLayout.setVisibility(View.GONE);
                        Intent intent=new Intent(MainActivity.this, GoogleService.class);
                        intent.putExtra("data",currentUserModel);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent);
                        } else {
                            startService(intent);
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
        setupHomeFragment(R.id.homeLayout);
    }
    public void drawerInit(){
        NavigationView navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        profileImage=header.findViewById(R.id.profile_image);
        progressBarImage=header.findViewById(R.id.progress_bar_image);
        userName=header.findViewById(R.id.name);

        homeImage=header.findViewById(R.id.home_image);
        inboxImage=header.findViewById(R.id.inbox_image);
        employeImage=header.findViewById(R.id.employee_image);
        checkpointimage=header.findViewById(R.id.check_point_image);
        mycompanyImage=header.findViewById(R.id.company_image);
//        homeText,inboxText,employeText,checkpointText,mycompanyText;
        homeText=header.findViewById(R.id.home_text);
        inboxText=header.findViewById(R.id.inbox_text);
        employeText=header.findViewById(R.id.employee_text);
        checkpointText=header.findViewById(R.id.check_point_text);
        mycompanyText=header.findViewById(R.id.company_text);


        header.findViewById(R.id.checkpoints_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(Gravity.LEFT);
                setupcheckpointsFragment(v);
            }
        });
        header.findViewById(R.id.homeLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(Gravity.LEFT);
                setupHomeFragment(v.getId());
            }
        });
        header.findViewById(R.id.my_company_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(Gravity.LEFT);
                setupProfileFragment(v);
            }
        });
        employeelistLayout=header.findViewById(R.id.myposts_layout);
        header.findViewById(R.id.myposts_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(Gravity.LEFT);
                setupEmployeeListFragment(v);
            }
        });
        header.findViewById(R.id.logout_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(Gravity.LEFT);
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                finish();
            }
        });
        header.findViewById(R.id.inbox_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(Gravity.LEFT);
                setupInboxFragment(v);
            }
        });

    }

    private void setupProfileFragment(View v) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putSerializable("data",userModel);
        CompanyProfilesFragment newCustomFragment = new CompanyProfilesFragment();
        newCustomFragment.setArguments(bundle);
        transaction.replace(R.id.container, newCustomFragment );
        transaction.addToBackStack(null);
        transaction.commit();
        selector(v.getId());

    }
    private void setupInboxFragment(View v) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putSerializable("data",userModel);
        InboxFragment newCustomFragment = new InboxFragment();
        newCustomFragment.setArguments(bundle);
        transaction.replace(R.id.container, newCustomFragment );
        transaction.addToBackStack(null);
        transaction.commit();
        selector(v.getId());

    }
    private void setupEmployeeListFragment(View v) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putSerializable("data",userModel);
        EmployeeListFragment newCustomFragment = new EmployeeListFragment();
        newCustomFragment.setArguments(bundle);
        transaction.replace(R.id.container, newCustomFragment );
        transaction.addToBackStack(null);
        transaction.commit();
        selector(v.getId());

    }


    public void setupHomeFragment(int id){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        HomeFragment newCustomFragment = new HomeFragment();
        transaction.replace(R.id.container, newCustomFragment );
        transaction.addToBackStack(null);
        transaction.commit();
        selector(id);

    }
    public void setupcheckpointsFragment(View v){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        CheckPointsFragment newCustomFragment = new CheckPointsFragment();
        transaction.replace(R.id.container, newCustomFragment );
        transaction.addToBackStack(null);
        transaction.commit();
        selector(v.getId());

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        finishAffinity();
    }


    public void selector(int id){
//        ImageView homeImage,inboxImage,employeImage,checkpointimage,mycompanyImage;
//        TextView homeText,inboxText,employeText,checkpointText,mycompanyText;
        homeImage.setColorFilter(MainActivity.this.getResources().getColor(R.color.black));
        inboxImage.setColorFilter(MainActivity.this.getResources().getColor(R.color.black));
        employeImage.setColorFilter(MainActivity.this.getResources().getColor(R.color.black));
        checkpointimage.setColorFilter(MainActivity.this.getResources().getColor(R.color.black));
        mycompanyImage.setColorFilter(MainActivity.this.getResources().getColor(R.color.black));
        homeText.setTextColor(getResources().getColor(R.color.black));
        inboxText.setTextColor(getResources().getColor(R.color.black));
        employeText.setTextColor(getResources().getColor(R.color.black));
        checkpointText.setTextColor(getResources().getColor(R.color.black));
        mycompanyText.setTextColor(getResources().getColor(R.color.black));

        if(id==R.id.homeLayout){
//            homeImage.setColorFilter(MainActivity.this.getResources().getColor( R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
            homeImage.setColorFilter(MainActivity.this.getResources().getColor(R.color.colorPrimary));
            homeText.setTextColor(getResources().getColor(R.color.colorPrimary));
        }else if(id==R.id.inbox_layout){
            inboxImage.setColorFilter(MainActivity.this.getResources().getColor(R.color.colorPrimary));
            inboxText.setTextColor(getResources().getColor(R.color.colorPrimary));
        } else if(id==R.id.myposts_layout){
            employeImage.setColorFilter(MainActivity.this.getResources().getColor(R.color.colorPrimary));
            employeText.setTextColor(getResources().getColor(R.color.colorPrimary));
        }else if(id==R.id.checkpoints_layout){
            checkpointimage.setColorFilter(MainActivity.this.getResources().getColor(R.color.colorPrimary));
            checkpointText.setTextColor(getResources().getColor(R.color.colorPrimary));
        }else if(id==R.id.my_company_layout){
            mycompanyImage.setColorFilter(MainActivity.this.getResources().getColor(R.color.colorPrimary));
            mycompanyText.setTextColor(getResources().getColor(R.color.colorPrimary));
        }

    }
}