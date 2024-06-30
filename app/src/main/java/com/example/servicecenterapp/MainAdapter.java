package com.example.servicecenterapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private List<MainData> mainDataList;
    private OnServiceRecordsClickListener onServiceRecordsClickListener;
    private int expandedPosition = -1; // Keep track of the expanded item

    public MainAdapter(List<MainData> mainDataList, OnServiceRecordsClickListener onServiceRecordsClickListener) {
        this.mainDataList = mainDataList;
        this.onServiceRecordsClickListener = onServiceRecordsClickListener;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        MainData mainData = mainDataList.get(position);
        holder.txtVehicleNo.setText("Vehicle:    " + mainData.getVehicleNo());
        holder.txtVehicleBrand.setText("Brand:        " + mainData.getVehicleBrand());
        holder.txtOdometer.setText("Odo:           " + mainData.getOdoMeter());

        boolean isExpanded = position == expandedPosition;
        holder.serviceRecordsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        if (isExpanded && mainData.getServiceRecords() == null) {
            onServiceRecordsClickListener.onServiceRecordsClick(mainData.getVehicleNo(), position);
        } else {
            populateServiceRecords(holder.serviceRecordsContainer, mainData.getServiceRecords());
        }

        holder.itemView.setOnClickListener(v -> {
            int oldExpandedPosition = expandedPosition;
            expandedPosition = isExpanded ? -1 : position;
            notifyItemChanged(oldExpandedPosition);
            notifyItemChanged(expandedPosition);
        });
    }

    @Override
    public int getItemCount() {
        return mainDataList.size();
    }

    private void populateServiceRecords(LinearLayout container, List<ServiceRecord> serviceRecords) {
        container.removeAllViews();
        if (serviceRecords != null) {
            for (ServiceRecord record : serviceRecords) {
                TextView textView = new TextView(container.getContext());
                textView.setText("Inno: " + record.getInno());
                container.addView(textView);
            }
        }
    }

    public interface OnServiceRecordsClickListener {
        void onServiceRecordsClick(String vehicleNo, int position);
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder {
        TextView txtVehicleNo, txtVehicleBrand, txtOdometer;
        LinearLayout serviceRecordsContainer;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
            txtVehicleNo = itemView.findViewById(R.id.txt_vehicle_no);
            txtVehicleBrand = itemView.findViewById(R.id.txt_vehicle_brand);
            txtOdometer = itemView.findViewById(R.id.txt_odo_meter);
            serviceRecordsContainer = itemView.findViewById(R.id.service_records_container);
        }
    }
}











