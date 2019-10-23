package com.giftsmile.app.smile;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    Context context;
    ArrayList<ServiceDetails> serviceDetails;

    public RecyclerViewAdapter(Context c, ArrayList<ServiceDetails> s){
        context=c;
        serviceDetails=s;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.recyclerview_items,parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.name.setText(serviceDetails.get(position).getInstitution());
        holder.phone.setText(serviceDetails.get(position).getPhone());
        holder.request.setText(serviceDetails.get(position).getReq());
        holder.status.setText(serviceDetails.get(position).getStatus());
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
            status=itemView.findViewById(R.id.InstitutionStatus);
        }
    }

}
