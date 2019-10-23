package com.giftsmile.app.smile.Fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.giftsmile.app.smile.MainActivity;
import com.giftsmile.app.smile.R;
import com.giftsmile.app.smile.RecyclerViewAdapter;
import com.giftsmile.app.smile.ServiceDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ServiceFragment extends Fragment {


    DatabaseReference databaseReference;
    RecyclerView recyclerView;
    ArrayList<ServiceDetails> list;
    RecyclerViewAdapter adapter;


    public ServiceFragment() {
        // Required empty public constructor


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_services, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView= view.findViewById(R.id.myRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        list = new ArrayList<ServiceDetails>();

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Requests");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){

                    String name="Not Available",phone="Not Available",req="Not Available",status="Not Available",type="Not Available";

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
                    ServiceDetails s = new ServiceDetails(name,phone,req,type,status);
                    list.add(s);
                }

                adapter = new RecyclerViewAdapter(getActivity(),list);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), "Something is Wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
