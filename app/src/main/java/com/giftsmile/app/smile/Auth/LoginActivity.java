package com.giftsmile.app.smile.Auth;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.giftsmile.app.smile.InstitutionMainActivity;
import com.giftsmile.app.smile.MainActivity;
import com.giftsmile.app.smile.Profile.SetupActivity;
import com.giftsmile.app.smile.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {


    CallbackManager callbackManager;
    LoginButton FBLoginButton;
    Button EmailPasswordBtn, MobileNumberBtn,InstitutionLoginBtn,VolunteerRegBtn;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks Callbacks;
    String VerificationID="-",email;
    Dialog VerificationDialog;
    ProgressBar VerifyProgressbar;
    ImageButton VerifyManualButton,VerifyBackButton;
    TextView VerifyTextView,VerifyTextViewNote,AuthInfo,VerifyCounter;
    EditText VerifyEditText;
    Button VerifyGoManualButton;
    CountryCodePicker PhneCCP;
    Button InstitutionAuthVerifyBtn,VolunteerRegCmpltBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        EmailPasswordBtn = findViewById(R.id.login_email);
        MobileNumberBtn = findViewById(R.id.login_phone);
        InstitutionLoginBtn = findViewById(R.id.login_institution);
        VolunteerRegBtn = findViewById(R.id.login_volunteer);


        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager  = CallbackManager.Factory.create();
        FBLoginButton = findViewById(R.id.login_facebook);
        FBLoginButton.setReadPermissions("public_profile","email");


        // facebook login

        FBLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {

                final String accesstoken = loginResult.getAccessToken().getToken();
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        Toast.makeText(LoginActivity.this, "Logged in successfully. (Facebook)", Toast.LENGTH_SHORT).show();

                        String profile_uri="not available",email="not available";

                        try{


                            URL profile_pic = new URL("https://graph.facebook.com/"+object.getString("id")+"/picture?width=250&height=250");
                            profile_uri=profile_pic.toString();
                            email=object.getString("email");

                            Toast.makeText(LoginActivity.this, email, Toast.LENGTH_SHORT).show();

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        AuthCredential credential = FacebookAuthProvider.getCredential(accesstoken);
                        final String finalEmail = email;
                        final String finalProfile_uri = profile_uri;
                        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful())
                                {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    //Verify(user);
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("email").setValue(finalEmail);
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profile_uri").setValue(finalProfile_uri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                startActivity(new Intent(LoginActivity.this, SetupActivity.class));
                                                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                                finish();

                                            }
                                            else
                                            {
                                                Toast.makeText(LoginActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText(LoginActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();


                                        }
                                    });

                                    Toast.makeText(LoginActivity.this, "Logged in successfully. (Firebase)", Toast.LENGTH_SHORT).show();


                                }
                                else
                                {
                                    Toast.makeText(LoginActivity.this, "Failed to login.(Firebase)", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });


                    }
                });



            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Failed to login.(Facebook) User cancelled.", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Failed to login.(Facebook) "+error.toString(), Toast.LENGTH_SHORT).show();

            }
        });


        // email and password login and registration


        final Dialog loginemailpassworddialog  = new Dialog(LoginActivity.this);
        final Dialog regemailpassworddialog  = new Dialog(LoginActivity.this);

        EmailPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginemailpassworddialog.setCancelable(false);
                loginemailpassworddialog.setCanceledOnTouchOutside(false);
                loginemailpassworddialog.setContentView(R.layout.login_email_passoword_dialog);
                loginemailpassworddialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                ImageButton LoginBackButton = loginemailpassworddialog.findViewById(R.id.email_password_back_button);
                ImageButton LoginDoneButton = loginemailpassworddialog.findViewById(R.id.email_password_done_button);
                final EditText LoginEmailText = loginemailpassworddialog.findViewById(R.id.email_password_emailtext);
                final EditText LoginPasswordText = loginemailpassworddialog.findViewById(R.id.email_password_passwordtext);
                final ProgressBar LoginPBar = loginemailpassworddialog.findViewById(R.id.email_password_progressbar);
                TextView LoginNewUser = loginemailpassworddialog.findViewById(R.id.email_password_new_user);

                LoginNewUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        loginemailpassworddialog.dismiss();


                        regemailpassworddialog.setCancelable(false);
                        regemailpassworddialog.setCanceledOnTouchOutside(false);
                        regemailpassworddialog.setContentView(R.layout.reg_email_passoword_dialog);
                        regemailpassworddialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


                        ImageButton RegBackButton = regemailpassworddialog.findViewById(R.id.reg_email_password_back_button);
                        ImageButton RegDoneButton = regemailpassworddialog.findViewById(R.id.reg_email_password_done_button);
                        final EditText RegNameText = regemailpassworddialog.findViewById(R.id.reg_email_password_nametext);
                        final EditText RegEmailText = regemailpassworddialog.findViewById(R.id.reg_email_password_emailtext);
                        final EditText RegPasswordText = regemailpassworddialog.findViewById(R.id.reg_email_password_passwordtext);
                        final ProgressBar RegPBar = regemailpassworddialog.findViewById(R.id.reg_email_password_progressbar);

                        RegBackButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if(RegPBar.isShown())
                                {
                                    Toast.makeText(LoginActivity.this, "Please wait until process is complete.", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    regemailpassworddialog.dismiss();
                                }

                            }
                        });

                        RegDoneButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                RegPBar.setVisibility(View.VISIBLE);

                                final String name1 = RegNameText.getText().toString();
                                final String email1 =RegEmailText.getText().toString();
                                String password1  = RegPasswordText.getText().toString();


                                if(!TextUtils.isEmpty(email1)  && !TextUtils.isEmpty(password1) && !TextUtils.isEmpty(name1))
                                {
                                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email1,password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                            if(task.isSuccessful())
                                            {

                                                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("name").setValue(name1);
                                                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("email").setValue(email1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())
                                                        {
                                                            regemailpassworddialog.dismiss();
                                                            startActivity(new Intent(LoginActivity.this, SetupActivity.class));
                                                            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                                            finish();
                                                        }
                                                        else
                                                        {
                                                            Toast.makeText(LoginActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();

                                                        }
                                                        RegPBar.setVisibility(View.INVISIBLE);

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                        Toast.makeText(LoginActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
                                                        RegPBar.setVisibility(View.INVISIBLE);


                                                    }
                                                });

                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText(LoginActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
                                            Toast.makeText(LoginActivity.this, "Error : "+e.toString(), Toast.LENGTH_SHORT).show();
                                            RegPBar.setVisibility(View.INVISIBLE);


                                        }
                                    });
                                }
                                else
                                {
                                    RegPBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(LoginActivity.this, "Fill in all the fields.", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                        regemailpassworddialog.show();
                    }
                });

                LoginBackButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(LoginPBar.isShown())
                        {
                            Toast.makeText(LoginActivity.this, "Please wait until process is complete.", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            loginemailpassworddialog.dismiss();
                        }

                    }
                });

                LoginDoneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final String email = LoginEmailText.getText().toString();
                        final String password  = LoginPasswordText.getText().toString();
                        LoginPBar.setVisibility(View.VISIBLE);

                        if(!TextUtils.isEmpty(email)  && !TextUtils.isEmpty(password))
                        {
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if(task.isSuccessful())
                                    {
                                        loginemailpassworddialog.dismiss();
                                        startActivity(new Intent(LoginActivity.this, SetupActivity.class));
                                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                        finish();
                                    }

                                    LoginPBar.setVisibility(View.INVISIBLE);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull final Exception e) {

                                    Toast.makeText(LoginActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(LoginActivity.this, "Error : "+e.toString(), Toast.LENGTH_SHORT).show();

                                    LoginPBar.setVisibility(View.INVISIBLE);

                                }


                            });
                        }
                        else
                        {
                            LoginPBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginActivity.this, "Fill in all the fields.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

                loginemailpassworddialog.show();

            }
        });



        // phone auth

        Callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {



            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                ShowToast("Successfully completed verification.");
                SignInWithCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_out));
                VerifyTextViewNote.setText("*NOTE \nVerification failed. Please try after sometime.");
                VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_in));
                VerifyTextViewNote.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCodeSent(final String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                VerificationID = s;
            }
        };


        final Dialog phneauthdialog = new Dialog(LoginActivity.this);
        VerifyDialogInit();
        MobileNumberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                phneauthdialog.setCancelable(true);
                phneauthdialog.setCanceledOnTouchOutside(false);
                phneauthdialog.setContentView(R.layout.phone_auth_layout);
                phneauthdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                ImageButton PhneBackBtn = phneauthdialog.findViewById(R.id.auth_back_button);
                Button PhneNextBtn = phneauthdialog.findViewById(R.id.volunteer_reg_go_btn);
                PhneCCP = phneauthdialog.findViewById(R.id.volunteer_reg_countrypicker);
                final EditText PhneNum = phneauthdialog.findViewById(R.id.volunteer_reg_number);
                PhneCCP.registerCarrierNumberEditText(PhneNum);

                PhneBackBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        phneauthdialog.dismiss();

                    }
                });

                PhneNextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String number = PhneNum.getText().toString();
                        if(!TextUtils.isEmpty(number))
                        {
                            VerificationDialog.show();
                            PhoneAuthProvider.getInstance().verifyPhoneNumber(PhneCCP.getFullNumberWithPlus(),60, TimeUnit.SECONDS,LoginActivity.this,Callbacks);

                            new CountDownTimer(60000, 1000) {

                                public void onTick(long millisUntilFinished) {
                                    VerifyCounter.setText(String.format("Timeout in : %d s", millisUntilFinished / 1000));
                                }

                                public void onFinish() {
                                    VerifyCounter.setText("Please wait.");
                                    EnableManualVerification();
                                }

                            }.start();
                        }
                        else
                        {
                            Toast.makeText(LoginActivity.this, "Fill in the phone number.", Toast.LENGTH_SHORT).show();

                        }


                    }
                });

                phneauthdialog.show();
            }
        });


        // institution  login
        InstitutionLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InstitutionLogin();

            }
        });

        //volunteer registration
        VolunteerRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                VolunteerRegistration();

            }
        });



    }

    private void VolunteerRegistration(){

        final Dialog VolunteerDialog = new Dialog(this);
        VolunteerRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                VolunteerDialog.setContentView(R.layout.activity_volunteer_reg);
                VolunteerDialog.setCanceledOnTouchOutside(false);
                VolunteerDialog.setCancelable(false);
                VolunteerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                final EditText VolName = VolunteerDialog.findViewById(R.id.volunteer_reg_name);
                final EditText VolMobile = VolunteerDialog.findViewById(R.id.volunteer_reg_number);
                final EditText VolEmail = VolunteerDialog.findViewById(R.id.volunteer_reg_Email);
                final EditText VolAddress = VolunteerDialog.findViewById(R.id.volunteer_reg_address);
                final EditText VolProf = VolunteerDialog.findViewById(R.id.volunteer_reg_prof);
                ImageButton VolBack = VolunteerDialog.findViewById(R.id.volunteer_reg_back_button);

                final ProgressBar VolunteerRegPbar = VolunteerDialog.findViewById(R.id.volunteer_reg_progressbar);

                VolunteerRegCmpltBtn = VolunteerDialog.findViewById(R.id.volunteer_reg_go_btn);

                VolunteerRegCmpltBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        VolunteerRegPbar.setVisibility(View.VISIBLE);
                        ShowToast("Btn Clicked");
                        String name="", mobile="",email ="",address ="",prof="";
                        name=VolName.getText().toString();
                        mobile=VolMobile.getText().toString();
                        email=VolEmail.getText().toString();
                        address =VolAddress.getText().toString();
                        prof=VolProf.getText().toString();


                        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(mobile)&&!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(address)&&!TextUtils.isEmpty(prof)){

                            ShowToast("Entered Submission");
                            FirebaseDatabase.getInstance().getReference().child("Volunteer").child(mobile).child("name").setValue(name);
                            FirebaseDatabase.getInstance().getReference().child("Volunteer").child(mobile).child("mobile").setValue(mobile);
                            FirebaseDatabase.getInstance().getReference().child("Volunteer").child(mobile).child("email").setValue(email);
                            FirebaseDatabase.getInstance().getReference().child("Volunteer").child(mobile).child("address").setValue(address);
                            FirebaseDatabase.getInstance().getReference().child("Volunteer").child(mobile).child("prof").setValue(prof).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){

                                        startActivity(new Intent(LoginActivity.this,LoginActivity.class));
                                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                                        finish();
                                    }
                                    else{
                                        ShowToast("Error Registering Volunteer, Try Again");
                                    }

                                    VolunteerRegPbar.setVisibility(View.GONE);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    ShowToast("Check Your Connectivity");
                                    VolunteerRegPbar.setVisibility(View.GONE);
                                }
                            });

                        }else{

                            ShowToast("Check The Fields above");
                            VolunteerRegPbar.setVisibility(View.GONE);
                        }

                    }
                });

                VolBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(VolunteerRegPbar.isShown())
                        {
                            ShowToast("Please wait till process is complete.");
                        }
                        else
                        {
                            VolunteerDialog.dismiss();
                        }

                    }
                });

                VolunteerDialog.show();

            }
        });



    }

    private void InstitutionLogin() {

        /// institution verification

        final Dialog InstitutionDialog = new Dialog(this);
        InstitutionLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InstitutionDialog.setContentView(R.layout.institution_auth_dialog);
                InstitutionDialog.setCanceledOnTouchOutside(false);
                InstitutionDialog.setCancelable(false);
                InstitutionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                ImageButton InstitutionAuthBackBtn = InstitutionDialog.findViewById(R.id.institution_auth_back_button);
                ImageButton InstitutionAuthDoneBtn = InstitutionDialog.findViewById(R.id.institution_auth_done_button);

                final EditText InstitutionAuthEmail = InstitutionDialog.findViewById(R.id.institution_auth_email_edittext);
                final EditText InstitutionAuthPassword = InstitutionDialog.findViewById(R.id.institution_auth_password_edittext);

                final ProgressBar InstitutionAuthPbar = InstitutionDialog.findViewById(R.id.institution_auth_progressbar);

                InstitutionAuthVerifyBtn = InstitutionDialog.findViewById(R.id.institution_auth_verified_btn);

                InstitutionAuthVerifyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Reloadverification();

                    }
                });

                InstitutionAuthBackBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(InstitutionAuthPbar.isShown())
                        {
                            ShowToast("Please wait till process is complete.");
                        }
                        else
                        {
                            InstitutionDialog.dismiss();
                        }

                    }
                });

                InstitutionAuthDoneBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        ShowToast("Btn Clicked");
                        String email ="",password ="";
                        email=InstitutionAuthEmail.getText().toString();
                        password = InstitutionAuthPassword.getText().toString();


                        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
                        {

                            InstitutionAuthPbar.setVisibility(View.VISIBLE);
                            final String finalPassword = password;
                            final String finalEmail = email;

                            VerifyInstitution(finalEmail, finalPassword);

                            InstitutionAuthPbar.setVisibility(View.GONE);

                        }
                        else
                        {
                            ShowToast("Fill in the fields");
                        }

                    }
                });

                InstitutionDialog.show();


            }
        });

    }

    private void ShowToast(String s) {

        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


    private void VerifyDialogInit() {
        VerificationDialog  = new Dialog(this);
        VerificationDialog.setCancelable(false);
        VerificationDialog.setContentView(R.layout.verification_layout);
        VerifyProgressbar = VerificationDialog.findViewById(R.id.verification_progressbar);
        VerifyEditText = VerificationDialog.findViewById(R.id.verification_edittext);
        VerifyTextView = VerificationDialog.findViewById(R.id.verification_textview);
        VerifyTextViewNote = VerificationDialog.findViewById(R.id.verification_textviewnote);
        VerifyManualButton = VerificationDialog.findViewById(R.id.verification_done_button);
        VerifyBackButton = VerificationDialog.findViewById(R.id.verification_back_button);
        VerifyCounter = VerificationDialog.findViewById(R.id.verification_counter);
        VerifyGoManualButton = VerificationDialog.findViewById(R.id.verification_manual_verify_button);

        VerificationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        VerifyManualButton.setVisibility(View.GONE);
        VerifyTextView.setText("Verifying");
        VerifyEditText.setVisibility(View.GONE);
        VerifyProgressbar.setAnimation(AnimationUtils.loadAnimation(this,android.R.anim.fade_in));
        VerifyProgressbar.setVisibility(View.VISIBLE);
        VerifyTextViewNote.setVisibility(View.INVISIBLE);

        VerifyBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                VerificationDialog.dismiss();
            }
        });

        VerifyManualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(VerifyEditText.getText().toString()))
                {
                    PhoneAuthCredential credential =PhoneAuthProvider.getCredential(VerificationID,VerifyEditText.getText().toString());
                    SignInWithCredential(credential);
                }
                else
                {
                    VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_out));
                    VerifyTextViewNote.setText("*NOTE \nVerification field is empty");
                    VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_in));
                    VerifyTextViewNote.setVisibility(View.VISIBLE);
                }

            }
        });

        VerifyGoManualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EnableManualVerification();

            }
        });


    }

    private void VerifyInstitution(final String finalEmail, String finalPassword) {


        FirebaseAuth.getInstance().signInWithEmailAndPassword(finalEmail,finalPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {

                    if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
                    {
                        FirebaseDatabase.getInstance().getReference().child("Institutions").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("name").setValue("Name");
                        startActivity(new Intent(LoginActivity.this, InstitutionMainActivity.class));
                        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                        finish();
                    }
                    else
                    {

                        FirebaseDatabase.getInstance().getReference().child("InstitutionMails").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){

                                    if (dataSnapshot1.hasChildren()){
                                        email=dataSnapshot1.child("email").getValue().toString();
                                        if(email.equals(finalEmail)){

                                            VerifyMail();
                                            ShowToast("Verification mail send");

                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        ShowToast("Unautorised Entry");
                    }
                   // InstitutionAuthVerifyBtn.setVisibility(View.VISIBLE);


                }
                else
                {
                    ShowToast("Verification failed. Please try again.");
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                ShowToast(e.toString());

            }
        });
    }

    private void VerifyMail() {

        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {


                if(task.isSuccessful())
                {

                    Reloadverification();

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                ShowToast(e.toString());
            }
        });


    }

    private void Reloadverification() {

        FirebaseAuth.getInstance().getCurrentUser().reload();

        if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
        {

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            finish();
        }
        else
        {
            ShowToast("Email not verified");
        }
    }

    private void SignInWithCredential(PhoneAuthCredential phoneAuthCredential) {

        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {

                    FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("number").setValue(PhneCCP.getFullNumberWithPlus());
                    VerificationDialog.dismiss();
                    startActivity(new Intent(LoginActivity.this, SetupActivity.class));
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                    finish();
                }
                else  if(!task.isSuccessful())
                {
                    try {
                        throw task.getException();
                    }
                    catch (FirebaseAuthInvalidCredentialsException e)
                    {
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_out));
                        VerifyTextViewNote.setText("*NOTE \nVerification code is invalid or has expired.");
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_in));
                        VerifyTextViewNote.setVisibility(View.VISIBLE);
                    }
                    catch (FirebaseAuthInvalidUserException e)
                    {
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_out));
                        VerifyTextViewNote.setText("*NOTE \nThe corresponding account has been blocked. Please contact FoodyGuide help-desk.");
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_in));
                        VerifyTextViewNote.setVisibility(View.VISIBLE);
                    }
                    catch (RuntimeException e)
                    {
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_out));
                        VerifyTextViewNote.setText("*NOTE \nVerification failed. Please try after sometime.");
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_in));
                        VerifyTextViewNote.setVisibility(View.VISIBLE);
                    }
                    catch (Exception e)
                    {
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_out));
                        VerifyTextViewNote.setText("*NOTE \nVerification failed. Please try after sometime.");
                        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_in));
                        VerifyTextViewNote.setVisibility(View.VISIBLE);
                    }


                }
                else
                {
                    ShowToast("Registration failed. Please try after sometime.");
                }
            }
        });

    }

    private void EnableManualVerification() {

        VerifyGoManualButton.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_out));
        VerifyGoManualButton.setVisibility(View.GONE);
        VerifyCounter.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_out));
        VerifyCounter.setVisibility(View.GONE);
        VerifyProgressbar.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_out));
        VerifyProgressbar.setVisibility(View.GONE);
        VerifyEditText.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_in));
        VerifyEditText.setVisibility(View.VISIBLE);

        VerifyTextView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_out));
        VerifyTextView.setText("Manual Verification");
        VerifyTextView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_in));
        VerifyTextViewNote.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_in));
        VerifyTextViewNote.setVisibility(View.VISIBLE);
        VerifyManualButton.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),android.R.anim.fade_in));
        VerifyManualButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
