package com.giftsmile.app.smile;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.giftsmile.app.smile.Auth.LoginActivity;
import com.giftsmile.app.smile.Profile.SetupActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class InstitutionMainActivity extends AppCompatActivity {

    public DatabaseReference mDatabaseUsers,mDatabaseInstitutions;
    public FirebaseAuth mAuth;
    private CircleImageView MainProfileBtn;
    private ImageButton LogoutBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_institution_main);

        MainProfileBtn = findViewById(R.id.inst_main_profile_btn);
        LogoutBtn = findViewById(R.id.inst_logout_btn);

        mDatabaseInstitutions= FirebaseDatabase.getInstance().getReference().child("Institutions");
        mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users");

        //Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null){

            Intent loginIntent = new Intent(InstitutionMainActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
            finish();
        }
        else {
            checkUserExist();

            mDatabaseInstitutions.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
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
        try{

            mDatabaseInstitutions.keepSynced(true);

        }catch (Exception e){
            e.printStackTrace();
        }

        MainProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(InstitutionMainActivity.this, InstitutionSetupActivity.class));

            }
        });

        LogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });


    }

    private void logout() {

        mAuth.signOut();
        Intent loginIntent = new Intent(InstitutionMainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
        finish();
    }

    private void checkUserExist() {

        final String user_id = mAuth.getCurrentUser().getUid();

                    mDatabaseInstitutions.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(!dataSnapshot.hasChild(user_id) || !dataSnapshot.child(user_id).hasChild("name") || !dataSnapshot.child(user_id).hasChild("email") || !dataSnapshot.child(user_id).hasChild("number")) {

                                Intent setupIntent = new Intent(InstitutionMainActivity.this, InstitutionSetupActivity.class);
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
