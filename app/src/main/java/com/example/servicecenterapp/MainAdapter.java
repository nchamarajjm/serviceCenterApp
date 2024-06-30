package com.example.servicecenterapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private List<MainData> mainDataList;
    private OnServiceRecordsClickListener onServiceRecordsClickListener;

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
        holder.txtVehicleNo.setText(mainData.getVehicleNo());
        holder.txtVehicleBrand.setText(mainData.getvehicleBrand());
        holder.txtOdometer.setText(mainData.getodoMeter());

        holder.btnServiceRecords.setOnClickListener(v ->
                onServiceRecordsClickListener.onServiceRecordsClick(mainData.getVehicleNo()));
    }

    @Override
    public int getItemCount() {
        return mainDataList.size();
    }

    public interface OnServiceRecordsClickListener {
        void onServiceRecordsClick(String vehicleNo);
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder {
        TextView txtVehicleNo, txtVehicleBrand, txtOdometer;
        Button btnServiceRecords;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
            txtVehicleNo = itemView.findViewById(R.id.txt_vehicle_no);
            txtVehicleBrand = itemView.findViewById(R.id.txt_vehicle_brand);
            txtOdometer = itemView.findViewById(R.id.txt_odo_meter);
            btnServiceRecords = itemView.findViewById(R.id.btn_service_recordes);
        }
    }
}
