package com.giftsmile.app.smile;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {




    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabaseUsers;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Firebase Authentication


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                 if(firebaseAuth.getCurrentUser()==null){

                     Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                     loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     startActivity(loginIntent);
                 }
                 else{

                     checkUserExist();
                 }

            }
        };
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        //To Dispaly Fragment 1 in begining
        setTitle("Fragment Title One");
        Fragment1 fragment1=new Fragment1();
        FragmentTransaction fragmentTransaction1=getSupportFragmentManager().beginTransaction();
        fragmentTransaction1.replace(R.id.frame,fragment1,"FragmentName");
        fragmentTransaction1.commit();


        //Navigation bar and fragments
        BottomNavigationView bottomNavigationView =(BottomNavigationView)findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.action_search:
                        setTitle("Fragment Title One");
                        Fragment1 fragment1=new Fragment1();
                        FragmentTransaction fragmentTransaction1=getSupportFragmentManager().beginTransaction();
                        fragmentTransaction1.replace(R.id.frame,fragment1,"FragmentName");
                        fragmentTransaction1.commit();
                        break;

                    case R.id.action_add:
                        setTitle("Fragment Title Two");
                        Fragment2 fragment2=new Fragment2();
                        FragmentTransaction fragmentTransaction2=getSupportFragmentManager().beginTransaction();
                        fragmentTransaction2.replace(R.id.frame,fragment2,"FragmentName");
                        fragmentTransaction2.commit();
                        break;

                    case R.id.action_people:
                        setTitle("Fragment Title Three");
                        Fragment3 fragment3=new Fragment3();
                        FragmentTransaction fragmentTransaction3=getSupportFragmentManager().beginTransaction();
                        fragmentTransaction3.replace(R.id.frame,fragment3,"FragmentName");
                        fragmentTransaction3.commit();
                        break;

                    /*case R.id.action_logout:
                        logout();
                        break;*/
                }
                return true;
            }
        });
    }

    private void logout() {

        mAuth.signOut();
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);


    }

    private void checkUserExist() {

        final String user_id = mAuth.getCurrentUser().getUid();

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(user_id)) {

                    Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(setupIntent);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
