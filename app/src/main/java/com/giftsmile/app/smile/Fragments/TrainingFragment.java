package com.giftsmile.app.smile.Fragments;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.giftsmile.app.smile.R;
import com.giftsmile.app.smile.ServiceDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class TrainingFragment extends Fragment {


    DatabaseReference databaseRequestReference,databaseUserReference;
    RecyclerView recyclerView;
    ArrayList<ServiceDetails> list;
    ServiceRecyclerViewAdapter adapter;
    public TextView ReqInstName,ReqPhone,ReqReq,NoReq;
    public Dialog RequestDialog;
    public Button ReqBtn;
    public ProgressBar progressBar;



    public TrainingFragment() {
        // Required empty public constructor


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_training, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView= view.findViewById(R.id.myTrainingRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        list = new ArrayList<ServiceDetails>();

        RequestDialog = new Dialog(getActivity());
        RequestDialog.setContentView(R.layout.request_dialog);
        RequestDialog.setCancelable(true);
        RequestDialog.setCanceledOnTouchOutside(true);
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.80);
        int height= (int)(getResources().getDisplayMetrics().heightPixels*0.70);
        RequestDialog.getWindow().setLayout(width,height);
        RequestDialog.findViewById(R.id.dscroll_view).setHorizontalScrollBarEnabled(false);
        RequestDialog.findViewById(R.id.dscroll_view).setVerticalScrollBarEnabled(false);
        ReqInstName=RequestDialog.findViewById(R.id.DialogInstitutionName);
        ReqPhone=RequestDialog.findViewById(R.id.DialogInstitutionPhone);
        ReqReq=RequestDialog.findViewById(R.id.DialogInstitutionRequest);
        ReqBtn=RequestDialog.findViewById(R.id.DialogHelpBtn);

        progressBar=view.findViewById(R.id.trainingprgsbr);
        NoReq=view.findViewById(R.id.noreqtrainmsg);

        databaseUserReference=FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());


        databaseRequestReference = FirebaseDatabase.getInstance().getReference().child("Requests");
        databaseRequestReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                list.clear();
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){

                    String key="null",name="Not Available",phone="Not Available",req="Not Available",status="Not Available",type="Not Available";

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
                    progressBar.setVisibility(View.GONE);
                    NoReq.setVisibility(View.GONE);
                    ServiceDetails s = new ServiceDetails(key,name,phone,req,type,status);
                    if(!dataSnapshot1.hasChild("Uid")){
                        if(type.equals("Training")){
                            list.add(s);
                        }
                    }

                }

                if(list.isEmpty()){
                    NoReq.setVisibility(View.VISIBLE);
                }

                adapter = new ServiceRecyclerViewAdapter(getActivity(),list);
                recyclerView.setAdapter(adapter);



            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), "Something is Wrong", Toast.LENGTH_SHORT).show();
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
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.recyclerview_items,parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
            holder.name.setText(serviceDetails.get(position).getInstitution());
            holder.phone.setText(serviceDetails.get(position).getPhone());
            holder.request.setText(serviceDetails.get(position).getReq());
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


            ReqInstName.setText(details.getInstitution());
            ReqPhone.setText(details.getPhone());
            ReqReq.setText(details.getReq());

            ReqBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
                    databaseRequestReference.child(details.getKey()).child("Status").setValue("Orange");
                    databaseRequestReference.child(details.getKey()).child("Uid").setValue(FirebaseAuth.getInstance().getUid());
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
                    String format = simpleDateFormat.format(new Date());
                    databaseUserReference.child("Requests").child(details.getInstitution()+format).setValue(details.getKey());
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

                name = itemView.findViewById(R.id.InstitutionName);
                phone=itemView.findViewById(R.id.InstitutionPhone);
                request=itemView.findViewById(R.id.InstitutionRequest);
                // status=itemView.findViewById(R.id.InstitutionStatus);

            }
        }



    }
}
