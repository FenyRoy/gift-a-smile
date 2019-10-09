package com.giftsmile.app.smile;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.FacebookSdk;
import com.giftsmile.app.smile.Auth.LoginActivity;
import com.giftsmile.app.smile.Helper.MainViewPagerAdapter;
import com.giftsmile.app.smile.Profile.SetupActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {




    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private ViewPager MainViewPager;
    private TabLayout MainTabs;
    private MainViewPagerAdapter viewPagerAdapter;
    private CircleImageView MainProfileBtn;
    private ImageButton MainLocRefreshBtn;
    private ProgressBar MainLocProgressbar;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double MainLat, MainLng;
    private static final int REQUEST_LOCATION_PERMISSION = 1013;
    private boolean GpsStatus=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FacebookSdk.sdkInitialize(getApplicationContext());

        //Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null){

            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
            finish();
        }
        else{

            Map map = new HashMap<>();
            map.put("email","fenyroy@gmail.com");
            FirebaseDatabase.getInstance().getReference().child("Institution_Mail").push().setValue(map);
            mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
            checkUserExist();
            mDatabaseUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.hasChild("profile_uri"))
                    {
                        String uri = dataSnapshot.child("profile_uri").getValue().toString();
                        Glide.with(getApplicationContext()).load(uri).apply(new RequestOptions().placeholder(R.drawable.ic_account_circle)).into(MainProfileBtn);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        mDatabaseUsers.keepSynced(true);

        MainViewPager = findViewById(R.id.main_viewpager);
        MainTabs = findViewById(R.id.main_tab);
        viewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());
        MainTabs.setupWithViewPager(MainViewPager);
        MainViewPager.setAdapter(viewPagerAdapter);

        MainProfileBtn = findViewById(R.id.main_profile_btn);


        MainProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this,SetupActivity.class));

            }
        });

        MainLocProgressbar = findViewById(R.id.main_loc_progressbar);
        MainLocRefreshBtn = findViewById(R.id.main_loc_refresh_btn);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        MainLocRefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ShowMyLocation();

            }
        });


    }

    private boolean IsConnectedToNet() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

    }

    private boolean IsGPSEnabled() {
        locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return GpsStatus;
    }


    private void SnackBarMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void ShowMyLocation() {


        MainLocProgressbar.setVisibility(View.VISIBLE);
        MainLocRefreshBtn.setVisibility(View.GONE);

        if (IsGPSEnabled() && IsConnectedToNet()) {

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    MainLat = latitude;
                    MainLng = longitude;


                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addresses =
                                geocoder.getFromLocation(latitude, longitude, 1);
                        String result = addresses.get(0).getLocality() + ":";
                        result += addresses.get(0).getCountryName();

                        MainLat = latitude;
                        MainLng = longitude;

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(MainLat==0 && MainLng==0)
                                {
                                    ShowMyLocation();
                                }
                                else
                                {
                                    MainLocProgressbar.setVisibility(View.GONE);
                                    MainLocRefreshBtn.setVisibility(View.VISIBLE);
                                    SnackBarMessage("Location updated");
                                }

                            }
                        },1000);


                        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Lat").setValue(MainLat);
                        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Lng").setValue(MainLng);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]
                                {Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            }
            //locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,locationListener,null);
            //locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,locationListener,null);

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 60, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60, 10, locationListener);


        }
        else if(!IsConnectedToNet())
        {
            MainLocProgressbar.setVisibility(View.GONE);
            MainLocRefreshBtn.setVisibility(View.VISIBLE);


            ConnectToNet();
        }
        else if(!IsGPSEnabled())
        {
            MainLocProgressbar.setVisibility(View.GONE);
            MainLocRefreshBtn.setVisibility(View.VISIBLE);


            EnableGPS();
        }
        else {

            MainLocProgressbar.setVisibility(View.GONE);
            MainLocRefreshBtn.setVisibility(View.VISIBLE);

            SnackBarMessage("Please enable location and data and try again.");

        }


    }

    private void EnableGPS() {


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setTitle("Enable Location")
                .setMessage("App needs to access your location. Please enable location to continue further.")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 234);
                    }
                })
                .create()
                .show();

    }


    private void ConnectToNet() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setTitle("Enable Data")
                .setMessage("App needs to access internet. Please enable internet to continue further.")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        SnackBarMessage("Enable data and try again");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                finish();

                            }
                        },3000);

                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 123);
                    }
                })
                .create()
                .show();
    }

    private void checkUserExist() {

        final String user_id = mAuth.getCurrentUser().getUid();

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(user_id) || !dataSnapshot.child(user_id).hasChild("name") || !dataSnapshot.child(user_id).hasChild("email") || !dataSnapshot.child(user_id).hasChild("number")) {

                    Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(setupIntent);
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
