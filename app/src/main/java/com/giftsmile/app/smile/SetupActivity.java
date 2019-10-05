package com.giftsmile.app.smile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView SetupImageBtn;
    private EditText NameEditText,EmailEditText,PhoneEditText;
    private Button SubmitBtn;

    private Uri mImageUri = null;

    private ProgressDialog mProgress;

    private static final int GALLERY_REQUEST = 1;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseUsers;

    private StorageReference mStorageImage;
    private String name="NA",email="NA",phone="NA",profile_uri="NA";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth= FirebaseAuth.getInstance();

        mDatabaseUsers= FirebaseDatabase.getInstance().getReference().child("Users");

        mStorageImage = FirebaseStorage.getInstance().getReference().child("profile_images");

        SetupImageBtn =findViewById(R.id.setup_image);

        NameEditText = (EditText)findViewById(R.id.setup_name);
        EmailEditText = findViewById(R.id.setup_email);
        PhoneEditText = findViewById(R.id.setup_phone);
        SubmitBtn =(Button)findViewById(R.id.setupFinishBtn);

        mProgress = new ProgressDialog(this);

        SubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startSetupAccout();

            }
        });

        SetupImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);

            }
        });

        SetAllValues();

    }

    private void SetAllValues() {


        mDatabaseUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("name"))
                {
                    name = dataSnapshot.child("name").getValue().toString();
                    NameEditText.setText(name);
                }
                if(dataSnapshot.hasChild("email"))
                {
                    email = dataSnapshot.child("email").getValue().toString();
                    EmailEditText.setText(email);

                }
                if(dataSnapshot.hasChild("profile_uri"))
                {
                    profile_uri = dataSnapshot.child("profile_uri").getValue().toString();

                    if(!profile_uri.equals("NA"))
                    {
                        Glide.with(getApplicationContext()).load(profile_uri).into(SetupImageBtn);
                    }
                }
                if(dataSnapshot.hasChild("number"))
                {
                    phone = dataSnapshot.child("number").getValue().toString();
                    PhoneEditText.setText(phone);

                }





            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void startSetupAccout() {

        mProgress.setMessage("Uploading....");
        mProgress.setCancelable(false);
        mProgress.show();

        final String user_id = mAuth.getCurrentUser().getUid();

        if(!TextUtils.isEmpty(NameEditText.getText().toString().trim()) && !TextUtils.isEmpty(EmailEditText.getText().toString().trim())  && !TextUtils.isEmpty(PhoneEditText.getText().toString().trim())  && mImageUri != null){

            StorageReference filepath = mStorageImage.child("Profile_Pic"+user_id);

            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                    final String name_= NameEditText.getText().toString().trim();
                    final String email_= EmailEditText.getText().toString().trim();
                    final String phone_= PhoneEditText.getText().toString().trim();

                    String downloadUri = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

                    mDatabaseUsers.child(user_id).child("name").setValue(name_);
                    mDatabaseUsers.child(user_id).child("email").setValue(email_);
                    mDatabaseUsers.child(user_id).child("number").setValue(phone_);
                    mDatabaseUsers.child(user_id).child("profile_uri").setValue(downloadUri);
                    mProgress.dismiss();
                    Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
            });

        }
        else  if(!TextUtils.isEmpty(NameEditText.getText().toString().trim()) && !TextUtils.isEmpty(EmailEditText.getText().toString().trim())  && !TextUtils.isEmpty(PhoneEditText.getText().toString().trim())  && mImageUri == null)
        {


            final String name_= NameEditText.getText().toString().trim();
            final String email_= EmailEditText.getText().toString().trim();
            final String phone_= PhoneEditText.getText().toString().trim();

            Map map = new HashMap();
            map.put("name",name_);
            map.put("email",email_);
            map.put("number",phone_);

            mDatabaseUsers.child(user_id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful())
                    {
                        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                    else
                    {
                        Toast.makeText(SetupActivity.this, "Failed to update values.", Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(SetupActivity.this, "Error : "+e.toString(), Toast.LENGTH_SHORT).show();

                }
            });


        }
        else
        {
            Toast.makeText(getApplicationContext(),"Fields found empty.",Toast.LENGTH_LONG).show();
            mProgress.dismiss();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode ==RESULT_OK){

            Uri imageuri =data.getData();
            CropImage.activity(imageuri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                     .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri();

                SetupImageBtn.setImageURI(mImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                String err = error.toString();
                Toast.makeText(SetupActivity.this,err,Toast.LENGTH_LONG).show();
            }
        }
    }
    public void onBackPressed(){
        finish();
        moveTaskToBack(true);
    }
}
