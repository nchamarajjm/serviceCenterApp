package com.example.servicecenterapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Dialog;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<DocumentSnapshot> userList;
    private FirebaseFirestore db;

    public UserAdapter(Context context, List<DocumentSnapshot> userList) {
        this.context = context;
        this.userList = userList;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        DocumentSnapshot userSnapshot = userList.get(position);
        String userId = userSnapshot.getId();
        String customerId = userSnapshot.getString("customer_id");
        String firstName = userSnapshot.getString("firstName");
        String email = userSnapshot.getString("email");
        boolean isEnabled = userSnapshot.getBoolean("enabled") != null ? userSnapshot.getBoolean("enabled") : true;

        // Set the text fields
        holder.customerIdTextView.setText(customerId == null || customerId.isEmpty() ? "NULL" : customerId);
        holder.firstNameTextView.setText(firstName);
        holder.emailTextView.setText(email);

        // Set click listener for the entire row (LinearLayout)
        holder.itemView.setOnClickListener(v -> {
            // Create and show the dialog for editing customer ID and enabled status
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_edit_customer_id);

            EditText editCustomerId = dialog.findViewById(R.id.edit_customer_id);
            CheckBox accEnableCheckBox = dialog.findViewById(R.id.acc_enable_checkbox); // Get the CheckBox
            Button saveButton = dialog.findViewById(R.id.save_button);

            // Pre-fill the current customer ID and enabled status in the dialog
            editCustomerId.setText(customerId);
            accEnableCheckBox.setChecked(isEnabled); // Set CheckBox status to current 'enabled' value

            saveButton.setOnClickListener(view -> {
                String newCustomerId = editCustomerId.getText().toString().trim();
                boolean newEnabledStatus = accEnableCheckBox.isChecked(); // Get the new enabled status

                // Update the enabled status in Firestore
                db.collection("users").document(userId)
                        .update("enabled", newEnabledStatus)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Account status updated", Toast.LENGTH_SHORT).show();

                            // Update local snapshot with the new enabled status
                            userSnapshot.getReference().update("enabled", newEnabledStatus)
                                    .addOnSuccessListener(success -> {
                                        // Refresh the UI for this item
                                        userSnapshot.getReference().get().addOnSuccessListener(updatedSnapshot -> {
                                            userList.set(position, updatedSnapshot); // Update the list with the latest data
                                            notifyItemChanged(position); // Refresh this specific item
                                            dialog.dismiss(); // Close dialog
                                        });
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

                if (!newCustomerId.isEmpty()) {
                    // Update the customer ID only if it's not empty
                    db.collection("users").document(userId)
                            .update("customer_id", newCustomerId)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Customer ID updated", Toast.LENGTH_SHORT).show();

                                // Update local snapshot and UI
                                userSnapshot.getReference().update("customer_id", newCustomerId)
                                        .addOnSuccessListener(success -> {
                                            userSnapshot.getReference().get().addOnSuccessListener(updatedSnapshot -> {
                                                userList.set(position, updatedSnapshot); // Update the list with the latest data
                                                notifyItemChanged(position); // Refresh this specific item
                                            });
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to update Customer ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(context, "Customer ID cannot be empty", Toast.LENGTH_SHORT).show();
                }
            });

            // Set the dialog width to match the screen width
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            dialog.show();
            dialog.getWindow().setAttributes(lp);
        });
    }





    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView customerIdTextView;
        TextView firstNameTextView;
        TextView emailTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            customerIdTextView = itemView.findViewById(R.id.customer_id);
            firstNameTextView = itemView.findViewById(R.id.first_name);
            emailTextView = itemView.findViewById(R.id.email);
        }
    }
}

