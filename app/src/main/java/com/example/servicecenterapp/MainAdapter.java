package com.example.servicecenterapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private List<MainData> mainDataList;

    public MainAdapter(List<MainData> mainDataList) {
        this.mainDataList = mainDataList;
    }

    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
        MainData data = mainDataList.get(position);
        holder.txtDate.setText(data.getDate());
        holder.txtVehicleNo.setText(data.getVehicleNo());
    }

    @Override
    public int getItemCount() {
        return mainDataList.size();
    }

    static class MainViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate, txtVehicleNo;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txt_date);
            txtVehicleNo = itemView.findViewById(R.id.txt_vehicle_no);
        }
    }
}
