package com.giftsmile.app.smile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    private ImageButton LogOutBtn;
    private CircleImageView SetupImageViewBtn;
    private TextView NameText,NoReq,ReqInstName,ReqPhone,ReqReq;
    private Button EditProfBtn;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseUsers,mDatabaseRequests;
    RecyclerView recyclerView;
    ArrayList<ServiceDetails> list;
    ServiceRecyclerViewAdapter adapter;
    public Dialog RequestDialog;

    private ProgressBar progressBar;
    private String pname="NA",prequest="NA",pphone="NA",pprofile_uri="NA";
    private List<String> Requests;
    private String Uid,InstitutionName,InstitutionNumber,reqkey;
    public String key="null",name="Not Available",phone="Not Available",req="Not Available",status="Not Available",type="Not Available",uid="Not Available",instId="Not Available",uname="Not Available",unumber="Not Available";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SetupImageViewBtn = findViewById(R.id.profile_image);
        NameText=findViewById(R.id.profile_name);
        LogOutBtn=findViewById(R.id.profilelogoutbtn);
        NoReq=findViewById(R.id.profilenoreqpenmsg);
        progressBar=findViewById(R.id.profilerequestprgsbr);
        recyclerView=findViewById(R.id.profileRequestRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(ProfileActivity.this));

        EditProfBtn=findViewById(R.id.profileEditBtn);

        RequestDialog = new Dialog(ProfileActivity.this);
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

        list = new ArrayList<ServiceDetails>();

        mAuth= FirebaseAuth.getInstance();

        mDatabaseUsers=FirebaseDatabase.getInstance().getReference().child("Users");

        Uid=FirebaseAuth.getInstance().getCurrentUser().getUid();

        LogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        EditProfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, SetupActivity.class));
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        SetAllValues();
        LoadRecycler();

    }

    private void LoadRecycler() {
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

                    ServiceDetails s = new ServiceDetails(key,name,phone,req,type,status,instId,uid,uname,unumber);
                    if(Uid.equals(uid)){
                        list.add(s);
                    }


                }

                NoReq.setVisibility(View.GONE);
                if(list.isEmpty()){
                    NoReq.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
                adapter = new ServiceRecyclerViewAdapter(ProfileActivity.this,list);
                recyclerView.setAdapter(adapter);



            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(ProfileActivity.this, "Something is Wrong", Toast.LENGTH_SHORT).show();
            }
        });


    }

    public class ServiceRecyclerViewAdapter extends RecyclerView.Adapter<ServiceRecyclerViewAdapter.MyViewHolder> {

        Context context;
        ArrayList<ServiceDetails> serviceDetails;
        Integer pos;


        public ServiceRecyclerViewAdapter(Context c, ArrayList<ServiceDetails> s) {
            context = c;
            serviceDetails = s;
        }

        @NonNull
        @Override
        public ServiceRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ServiceRecyclerViewAdapter.MyViewHolder(LayoutInflater.from(context).inflate(R.layout.reqrecyclerview_items, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ServiceRecyclerViewAdapter.MyViewHolder holder, final int position) {
            //holder.name.setText(serviceDetails.get(position).getInstitution());
            //holder.phone.setText(serviceDetails.get(position).getPhone());
            holder.request.setText(serviceDetails.get(position).getReq());

            if (serviceDetails.get(position).getStatus().equals("Red")) {
                holder.status.setBackgroundColor(Color.RED);
            }
            if (serviceDetails.get(position).getStatus().equals("Orange")) {
                holder.status.setBackgroundColor(getResources().getColor(R.color.orange));
            }
            if (serviceDetails.get(position).getStatus().equals("Yellow")) {
                holder.status.setBackgroundColor(Color.YELLOW);
            }
            if (serviceDetails.get(position).getStatus().equals("Green")) {
                holder.status.setBackgroundColor(Color.GREEN);
            }
            //  holder.status.setText(serviceDetails.get(position).getStatus());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ServiceDetails Details = serviceDetails.get(position);
                    ShowDialog(Details);
                    RequestDialog.show();
                }
            });
        }

        private void ShowDialog(final ServiceDetails details) {

            ReqInstName.setText(details.getInstitution());
            ReqPhone.setText(details.getPhone());
            ReqReq.setText(details.getReq());
            adapter.notifyDataSetChanged();

            ReqPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:"+ReqPhone.getText().toString()));
                    startActivity(intent);
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
    private void SetAllValues() {


        mDatabaseUsers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("name"))
                {
                    pname = dataSnapshot.child("name").getValue().toString();
                    NameText.setText(pname);
                }
                if(dataSnapshot.hasChild("profile_uri"))
                {
                    pprofile_uri = dataSnapshot.child("profile_uri").getValue().toString();

                    if(!pprofile_uri.equals("NA"))
                    {
                        Glide.with(getApplicationContext()).load(pprofile_uri).apply(new RequestOptions().placeholder(R.drawable.ic_account_circle)).into(SetupImageViewBtn);
                    }
                    else
                    {
                        Glide.with(getApplicationContext()).load(R.drawable.ic_account_circle).into(SetupImageViewBtn);

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void logout() {

        mAuth.signOut();
        Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
        finish();
    }


}
