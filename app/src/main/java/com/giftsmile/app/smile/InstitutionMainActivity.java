package com.giftsmile.app.smile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.giftsmile.app.smile.Auth.LoginActivity;
import com.giftsmile.app.smile.Fragments.ServiceFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class InstitutionMainActivity extends AppCompatActivity {

    public DatabaseReference mDatabaseUsers,mDatabaseInstitutions, mDatabaseRequests,databaseUser;
    public FirebaseAuth mAuth;
    private CircleImageView MainProfileBtn;
    private ImageButton LogoutBtn;
    private TextView ReqHeaderTxt,NoReq,ReqInstName,ReqPhone,ReqReq,StatusColor;
    public EditText ReqEditText;
    private Button NewRequest,SerRequest,TrainRequest,DenyBtn,VerifyBtn,CmpltBtn;
    private ImageButton ReqSubmit,ReqBack,SerBack;
    RecyclerView recyclerView;
    ArrayList<ServiceDetails> list;
    ServiceRecyclerViewAdapter adapter;
    public Dialog RequestDialog;
    private ProgressBar progressBar;
    private Dialog SerDialog,ReqDialog;
    private String Uid,InstitutionName,InstitutionNumber,reqkey;
    public String key="null",name="Not Available",phone="Not Available",req="Not Available",status="Not Available",type="Not Available",uid="Not Available",instId="Not Available",uname="Not Available",unumber="Not Available";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_institution_main);

        MainProfileBtn = findViewById(R.id.inst_main_profile_btn);
        LogoutBtn = findViewById(R.id.inst_logout_btn);
        NewRequest= findViewById(R.id.inst_make_new_req);

        StatusColor=findViewById(R.id.statuscolor);

        progressBar=findViewById(R.id.requestprgsbr);
        NoReq=findViewById(R.id.noreqpenmsg);
        recyclerView=findViewById(R.id.myRequestRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(InstitutionMainActivity.this));
        list = new ArrayList<ServiceDetails>();

        RequestDialog = new Dialog(InstitutionMainActivity.this);
        RequestDialog.setContentView(R.layout.instrequest_dialog);
        RequestDialog.setCancelable(true);
        RequestDialog.setCanceledOnTouchOutside(true);
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.80);
        int height= (int)(getResources().getDisplayMetrics().heightPixels*0.70);
        RequestDialog.getWindow().setLayout(width,height);
        RequestDialog.findViewById(R.id.dscroll_view).setHorizontalScrollBarEnabled(false);
        RequestDialog.findViewById(R.id.dscroll_view).setVerticalScrollBarEnabled(false);
        ReqInstName=RequestDialog.findViewById(R.id.inst_DialogInstitutionName);
        ReqPhone=RequestDialog.findViewById(R.id.inst_DialogInstitutionPhone);
        ReqReq=RequestDialog.findViewById(R.id.inst_DialogInstitutionRequest);
        DenyBtn=RequestDialog.findViewById(R.id.inst_service_denied_btn);
        VerifyBtn=RequestDialog.findViewById(R.id.inst_service_verified_btn);
        CmpltBtn=RequestDialog.findViewById(R.id.inst_service_completed_btn);


        SerDialog = new Dialog(InstitutionMainActivity.this);
        SerDialog.setContentView(R.layout.request_type_select_dialog);
        SerDialog.setCancelable(true);
        SerDialog.setCanceledOnTouchOutside(true);
        SerRequest= SerDialog.findViewById(R.id.inst_service_request_btn);
        TrainRequest=SerDialog.findViewById(R.id.inst_training_request_btn);
        SerBack=SerDialog.findViewById(R.id.inst_req_ser_back_button);


        ReqDialog = new Dialog(InstitutionMainActivity.this);
        ReqDialog.setContentView(R.layout.make_new_request_dialog);
        ReqDialog.setCancelable(true);
        ReqDialog.setCanceledOnTouchOutside(true);
        ReqHeaderTxt=ReqDialog.findViewById(R.id.inst_req_txtview);
        ReqEditText=ReqDialog.findViewById(R.id.inst_request_edittext);
        ReqSubmit=ReqDialog.findViewById(R.id.inst_req_submit_btn);
        ReqBack=ReqDialog.findViewById(R.id.inst_req_back_button);




        mDatabaseInstitutions= FirebaseDatabase.getInstance().getReference().child("Institutions");
        mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseRequests =FirebaseDatabase.getInstance().getReference().child("Requests");
        Uid=FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDatabaseInstitutions.child(Uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("name")){

                    InstitutionName=dataSnapshot.child("name").getValue().toString();
                }
                if(dataSnapshot.hasChild("number")){
                    InstitutionNumber=dataSnapshot.child("number").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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

        NewRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SerDialog.show();

                SerBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SerDialog.dismiss();
                    }
                });

                SerRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SerDialog.dismiss();
                        ReqHeaderTxt.setText("Service Request");
                        ReqDialog.show();

                    }
                });

                TrainRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SerDialog.dismiss();
                        ReqHeaderTxt.setText("Training Request");
                        ReqDialog.show();
                    }
                });

            }
        });


        ReqBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReqDialog.dismiss();
            }
        });

        ReqSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String reqid= md5(ReqEditText.getText().toString());

                Toast.makeText(InstitutionMainActivity.this, InstitutionName, Toast.LENGTH_SHORT).show();
                Toast.makeText(InstitutionMainActivity.this, reqid, Toast.LENGTH_SHORT).show();
                try {
                    mDatabaseRequests.child(reqid).child("Institution").setValue(InstitutionName);
                    mDatabaseRequests.child(reqid).child("Phone").setValue(InstitutionNumber);
                    mDatabaseRequests.child(reqid).child("Req").setValue(ReqEditText.getText().toString());
                    mDatabaseRequests.child(reqid).child("Status").setValue("Red");
                    mDatabaseRequests.child(reqid).child("Type").setValue(ServiceType());
                    mDatabaseRequests.child(reqid).child("InstID").setValue(Uid);
                    mDatabaseInstitutions.child(Uid).child("Requests").child(reqid).setValue(ServiceType());
                }
                catch (Exception e){
                    Toast.makeText(InstitutionMainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }

                ReqDialog.dismiss();

            }

            public String ServiceType() {

                if (ReqHeaderTxt.getText().toString().equals("Service Request")) {
                    return "Service";
                } else {
                    return "Training";
                }

            }
        });


        mDatabaseRequests = FirebaseDatabase.getInstance().getReference().child("Requests");
        mDatabaseRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                list.clear();

                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){

                    key="null";
                    name="Not Available";
                    phone="Not Available";
                    req="Not Available";
                    status="Not Available";
                    type="Not Available";
                    uid="Not Available";
                    instId="Not Available";
                    uname="Not Available";
                    unumber="Not Available";

                    if(dataSnapshot1.hasChildren()){
                        key=dataSnapshot1.getKey().toString();
                    }
                    if(dataSnapshot1.hasChild("Institution"))
                    {
                        name=dataSnapshot1.child("Institution").getValue().toString();
                    }
                    if(dataSnapshot1.hasChild("Phone"))
                    {
                        phone=dataSnapshot1.child("Phone").getValue().toString();
                    }
                    if(dataSnapshot1.hasChild("Req"))
                    {
                        req=dataSnapshot1.child("Req").getValue().toString();
                    }
                    if(dataSnapshot1.hasChild("Status"))
                    {
                        status=dataSnapshot1.child("Status").getValue().toString();
                    }
                    if(dataSnapshot1.hasChild("Type"))
                    {
                        type=dataSnapshot1.child("Type").getValue().toString();
                    }
                    if(dataSnapshot1.hasChild("InstID"))
                    {
                        instId=dataSnapshot1.child("InstID").getValue().toString();
                    }
                    if(dataSnapshot1.hasChild("Uid"))
                    {
                        uid=dataSnapshot1.child("Uid").getValue().toString();
                    }
                    databaseUser=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    databaseUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {


                            if(dataSnapshot2.hasChild("name")){
                                uname=dataSnapshot2.child("name").getValue().toString();
                          //      Toast.makeText(InstitutionMainActivity.this, "t Value"+dataSnapshot2.child("name").getValue().toString(), Toast.LENGTH_SHORT).show();
                            }
                            if(dataSnapshot2.hasChild("number")){
                                unumber=dataSnapshot2.child("number").getValue().toString();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            Toast.makeText(InstitutionMainActivity.this, "Error Loading Database", Toast.LENGTH_SHORT).show();

                        }
                    });

                    ServiceDetails s = new ServiceDetails(key,name,phone,req,type,status,instId,uid,uname,unumber);
                    //Toast.makeText(InstitutionMainActivity.this, Uid+"  "+instId+"  "+uid+"  "+uname, Toast.LENGTH_LONG).show();
                    if(Uid.equals(instId)){
                        list.add(s);
                    }


                }

                if(list.isEmpty()){
                    NoReq.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
                NoReq.setVisibility(View.GONE);
                adapter = new ServiceRecyclerViewAdapter(InstitutionMainActivity.this,list);
                recyclerView.setAdapter(adapter);



            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(InstitutionMainActivity.this, "Something is Wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public class ServiceRecyclerViewAdapter extends RecyclerView.Adapter<ServiceRecyclerViewAdapter.MyViewHolder> {

        Context context;
        ArrayList<ServiceDetails> serviceDetails;
        Integer pos;


        public ServiceRecyclerViewAdapter(Context c, ArrayList<ServiceDetails> s){
            context=c;
            serviceDetails=s;
        }

        @NonNull
        @Override
        public ServiceRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ServiceRecyclerViewAdapter.MyViewHolder(LayoutInflater.from(context).inflate(R.layout.reqrecyclerview_items,parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ServiceRecyclerViewAdapter.MyViewHolder holder, final int position) {
            //holder.name.setText(serviceDetails.get(position).getInstitution());
            //holder.phone.setText(serviceDetails.get(position).getPhone());
            holder.request.setText(serviceDetails.get(position).getReq());

            if(serviceDetails.get(position).getStatus().equals("Red")){
                holder.status.setBackgroundColor(Color.RED);
            }
            if(serviceDetails.get(position).getStatus().equals("Orange")){
                holder.status.setBackgroundColor(getResources().getColor(R.color.orange));
            }
            if(serviceDetails.get(position).getStatus().equals("Yellow")){
                holder.status.setBackgroundColor(Color.YELLOW);
            }
            if(serviceDetails.get(position).getStatus().equals("Green")){
                holder.status.setBackgroundColor(Color.GREEN);
            }
            //  holder.status.setText(serviceDetails.get(position).getStatus());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ServiceDetails Details =  serviceDetails.get(position);
                    ShowDialog(Details);
                    RequestDialog.show();

                }
            });
        }

            private void ShowDialog(final ServiceDetails details) {

                databaseUser=FirebaseDatabase.getInstance().getReference().child("Users").child(details.getUid());
                databaseUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {


                        if(dataSnapshot2.hasChild("name")){
                            uname=dataSnapshot2.child("name").getValue().toString();
                            details.setUname(uname);

                        }
                        if(dataSnapshot2.hasChild("number")){
                            unumber=dataSnapshot2.child("number").getValue().toString();
                            details.setUnumber(unumber);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        Toast.makeText(InstitutionMainActivity.this, "Error Loading Database", Toast.LENGTH_SHORT).show();

                    }
                });
                ReqInstName.setText(details.getUname());
                ReqPhone.setText(details.getUnumber());
                ReqReq.setText(details.getReq());
                adapter.notifyDataSetChanged();
                uname=unumber="Not Available";

                ReqPhone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:"+ReqPhone.getText().toString()));
                        startActivity(intent);
                    }
                });

                if(details.getStatus().equals("Red")){

                    DenyBtn.setVisibility(View.GONE);
                    VerifyBtn.setVisibility(View.GONE);
                    CmpltBtn.setVisibility(View.GONE);

                }
                else if(details.getStatus().equals("Green")){
                    DenyBtn.setVisibility(View.GONE);
                    VerifyBtn.setVisibility(View.GONE);
                    CmpltBtn.setVisibility(View.GONE);

                }
                else{
                    DenyBtn.setVisibility(View.VISIBLE);
                    VerifyBtn.setVisibility(View.VISIBLE);
                    CmpltBtn.setVisibility(View.VISIBLE);
                }

                DenyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mDatabaseRequests.child(details.getKey()).child("Status").setValue("Red");
                        mDatabaseUsers.child(details.getUid()).child("Requests").child(details.getKey()).setValue(null);
                        mDatabaseRequests.child(details.getKey()).child("Uid").setValue(null);
                        RequestDialog.dismiss();

                    }
                });

                VerifyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mDatabaseRequests.child(details.getKey()).child("Status").setValue("Yellow");
                        RequestDialog.dismiss();

                    }
                });

                CmpltBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mDatabaseRequests.child(details.getKey()).child("Status").setValue("Green");
                        RequestDialog.dismiss();

                    }
                });


            }

            @Override
            public int getItemCount() {
                return serviceDetails.size();
            }

            class MyViewHolder extends RecyclerView.ViewHolder{

                TextView name,phone,request,status;
                public MyViewHolder(View itemView) {
                    super(itemView);

                    //name = itemView.findViewById(R.id.InstitutionName);
                    //phone=itemView.findViewById(R.id.InstitutionPhone);
                    request=itemView.findViewById(R.id.InstitutionRequest);
                    status=itemView.findViewById(R.id.statuscolor);

                }
            }



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

    public String md5(String s) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
            String format = simpleDateFormat.format(new Date());
            s=s+format;
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

            return hexString.toString();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
