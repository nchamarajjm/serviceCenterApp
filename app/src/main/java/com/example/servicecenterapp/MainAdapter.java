package com.example.servicecenterapp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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
                LinearLayout horizontalLayout = new LinearLayout(container.getContext());
                horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

                //TextView for the service record
                TextView textView = new TextView(container.getContext());
                textView.setText(record.getInno());
                textView.setTextSize(16);
                textView.setTypeface(null, Typeface.BOLD);

                // Set the margins (top and bottom)
                LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1.0f
                );
                textViewLayoutParams.setMargins(0, 50, 0, 35);
                textView.setLayoutParams(textViewLayoutParams);
                horizontalLayout.addView(textView);

                // ImageButton for invoice icon
                ImageButton btnViewInvoice = new ImageButton(container.getContext());
                btnViewInvoice.setImageResource(R.drawable.invoice_icon);
                LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(125, 125);
                btnViewInvoice.setLayoutParams(buttonLayoutParams);
                btnViewInvoice.setBackgroundColor(Color.TRANSPARENT);

                // ImageButton for report icon
                ImageButton btnViewReport = new ImageButton(container.getContext());
                btnViewReport.setImageResource(R.drawable.report_icon);
                btnViewReport.setLayoutParams(buttonLayoutParams);
                btnViewReport.setBackgroundColor(Color.TRANSPARENT);

                btnViewReport.setOnClickListener(v -> showReportDialog(record.getInno(), container.getContext()));

                // Add buttons to the horizontal layout
                horizontalLayout.addView(btnViewInvoice);
                horizontalLayout.addView(btnViewReport);

                // Add the horizontal layout to the serviceRecordsContainer
                container.addView(horizontalLayout);
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

    private void showReportDialog(String inno, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View customTitleView = LayoutInflater.from(context).inflate(R.layout.custom_dialog_title_service_report, null);

        // Set custom title view
        builder.setCustomTitle(customTitleView);

        // Fetch details from tblvsum where inno = the service record number
        String reportDetails = fetchReportDetails(inno);

        // Create a TextView to display the report details
        TextView reportTextView = new TextView(context);
        reportTextView.setText(reportDetails);

        // Set the custom font from res/font folder
        reportTextView.setTypeface(ResourcesCompat.getFont(context, R.font.fmbindumathi));
        reportTextView.setTextSize(16); // Adjust size

        builder.setView(reportTextView);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }




    private String fetchReportDetails(String inno) {
        StringBuilder reportDetails = new StringBuilder();
        try {
            ConnectionHelper connectionHelper = new ConnectionHelper();
            Connection connect = connectionHelper.connectionClass();
            if (connect != null) {
                String query = "SELECT * FROM tblvsum WHERE inno = '" + inno + "'";
                Statement st = connect.createStatement();
                ResultSet rs = st.executeQuery(query);

                while (rs.next()) {
                    String detail = rs.getString("detail");
                    if (detail != null && !detail.isEmpty()) {
                        reportDetails.append("• ").append(detail).append("\n").append("\n");
                    }
                }
                connect.close();
            } else {
                reportDetails.append("Connection Error");
            }
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage());
            reportDetails.append("Error fetching report details");
        }
        return reportDetails.toString();
    }


}











