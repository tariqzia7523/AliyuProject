package com.map.checkpost;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by deepshikha on 24/11/16.
 */

//public class GoogleService extends Service implements LocationListener {
public class GoogleService extends Service {

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude, longitude;
    LocationManager locationManager;
    Location location;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 1000;
    public static String str_receiver = "servicetutorial.service.receiver";

    String TAG = "GoogleSerive";
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    FirebaseUser firebaseUser;
    CountDownTimer countDownTimer;
    UserModel userModel;
    double oldlat,oldlng;

    public GoogleService() {

    }

    private String provider;

    int counter = 0;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        list=(ArrayList<PickUpOffersAllEntity>)intent.getExtras().get("list");
        Log.e(TAG, "On start Command");
        StartForeground();
        try{
            oldlat=0.0;
            oldlng=0.0;
            userModel=(UserModel) intent.getExtras().get("data");
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference("Locations").child(userModel.getCompanyId()).child(firebaseUser.getUid());
//        isLocationEnabled();
//        fn_getlocation();
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            countDownTimer = new CountDownTimer(300000, 10000) {

                public void onTick(long millisUntilFinished) {

                    counter++;
                    Log.e(TAG, "In service " + counter);

                    if (ActivityCompat.checkSelfPermission(GoogleService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(GoogleService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                    }
                    Location location = locationManager.getLastKnownLocation(provider);

                    // Initialize the location fields
                    if (location != null) {

                        System.out.println("Provider " + provider + " has been selected.");
//                    onLocationChanged(location);
                        fn_update(location);

                    } else {
                        Log.e(TAG, "In service " + "location is null");
                    }


                }

                public void onFinish() {
                    countDownTimer.start();
                }

            };
            countDownTimer.start();
        }catch (Exception c){
            c.printStackTrace();
        }



        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users").child("DeliveryMen").child(firebaseUser.getUid());
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users").child("DeliveryMen").child(firebaseUser.getUid());
        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 5, notify_interval);

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

// check if enabled and if not send user to the GSP settings
// Better solution would be to display a dialog and suggesting to
// go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

//        intent = new Intent(str_receiver);
//        fn_getlocation();
    }

//    @Override
//    public void onLocationChanged(Location location) {
//        fn_update(location);
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }

//    private void fn_getlocation() {
//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//
//            }
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 10, this);
//        }
//
////        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
////        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
////        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
////
////        if (!isGPSEnable && !isNetworkEnable) {
////
////        } else {
////
////            if (isNetworkEnable) {
////                location = null;
////                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
////
////                }
////                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);
//////                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
////                if (locationManager!=null){
////                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
////                    if (location!=null){
////
////                        Log.e("latitude",location.getLatitude()+"");
////                        Log.e("longitude",location.getLongitude()+"");
////
////                        latitude = location.getLatitude();
////                        longitude = location.getLongitude();
////                        fn_update(location);
////                    }
////                }
////
////            }else if (isGPSEnable){
////                location = null;
////                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10,this);
////                if (locationManager!=null){
////                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
////                    if (location!=null){
////                        Log.e("latitude",location.getLatitude()+"");
////                        Log.e("longitude",location.getLongitude()+"");
////                        latitude = location.getLatitude();
////                        longitude = location.getLongitude();
////                        fn_update(location);
////                    }
////                }
////            }
////
////
////        }
//
//    }

    private class TimerTaskToGetLocation extends TimerTask{
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
//                    fn_getlocation();
                }
            });

        }
    }

    private void StartForeground() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String CHANNEL_ID = "channel_location";
        String CHANNEL_NAME = "channel_location";

        NotificationCompat.Builder builder = null;
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
            builder.setChannelId(CHANNEL_ID);
            builder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE);
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        }

        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText("is getting your live location");
        Uri notificationSound = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(notificationSound);
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        startForeground(101, notification);
    }
    private void fn_update(final Location location){

        Log.e(TAG,location.getLatitude()+"");
        Log.e(TAG,location.getLongitude()+"");

        if(oldlng==location.getLongitude() && oldlat==location.getLatitude()){

        }else{
            LocationModel locationModel=new LocationModel();
            locationModel.setLat(location.getLatitude());
            locationModel.setLng(location.getLongitude());
            locationModel.setAddress(getAddress(location.getLatitude(),location.getLongitude()));
            locationModel.setPassedTime(System.currentTimeMillis());
            myRef.push().setValue(locationModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    oldlat=location.getLatitude();
                    oldlng=location.getLongitude();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    Log.e(TAG,"error");
                }
            });
        }


//        sendBroadcast(intent);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(),"service stoped",Toast.LENGTH_LONG).show();
    }


    public String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(GoogleService.this, Locale.getDefault());
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
            Toast.makeText(GoogleService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return "";
        }
    }

}