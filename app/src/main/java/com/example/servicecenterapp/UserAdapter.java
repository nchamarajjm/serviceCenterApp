package com.example.servicecenterapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
    private FirebaseAuth auth;

    public UserAdapter(Context context, List<DocumentSnapshot> userList) {
        this.context = context;
        this.userList = userList;
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
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

        if (customerId == null || customerId.isEmpty()) {
            holder.customerIdTextView.setText("NULL");
        } else {
            holder.customerIdTextView.setText(customerId);
        }
        holder.firstNameTextView.setText(firstName);
        holder.emailTextView.setText(email);
        holder.enableSwitch.setChecked(isEnabled);

        updateSwitchColor(holder.enableSwitch, isEnabled);

        holder.enableSwitch.setOnCheckedChangeListener(null); // Clear previous listener to prevent callback loops

        holder.enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            db.collection("users").document(userId)
                .update("enabled", isChecked)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "User " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
                    userSnapshot.getReference().update("enabled", isChecked); // Update local snapshot for immediate UI update
                    updateSwitchColor(holder.enableSwitch, isChecked);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    holder.enableSwitch.setChecked(!isChecked); // Revert switch state on failure
                    updateSwitchColor(holder.enableSwitch, !isChecked);
                });

            // Optionally, manage user logout based on isEnabled state
            if (!isChecked) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    auth.signOut();
                }
            }
        });

        // Set click listener for customerIdTextView
        holder.customerIdTextView.setOnClickListener(v -> {
            // Create and show the dialog
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_edit_customer_id);

            EditText editCustomerId = dialog.findViewById(R.id.edit_customer_id);
            Button saveButton = dialog.findViewById(R.id.save_button);

            editCustomerId.setText(customerId);

            saveButton.setOnClickListener(view -> {
                String newCustomerId = editCustomerId.getText().toString().trim();
                if (!newCustomerId.isEmpty()) {
                    db.collection("users").document(userId)
                            .update("customer_id", newCustomerId)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Customer ID updated", Toast.LENGTH_SHORT).show();
                                // Update local snapshot
                                userSnapshot.getReference().update("customer_id", newCustomerId).addOnSuccessListener(documentReference -> {
                                    holder.customerIdTextView.setText(newCustomerId.isEmpty() ? "NULL" : newCustomerId);
                                    dialog.dismiss();
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
        Switch enableSwitch;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            customerIdTextView = itemView.findViewById(R.id.customer_id);
            firstNameTextView = itemView.findViewById(R.id.first_name);
            emailTextView = itemView.findViewById(R.id.email);
            enableSwitch = itemView.findViewById(R.id.enable_switch);
        }
    }

    private void updateSwitchColor(Switch enableSwitch, boolean isEnabled) {
        if (isEnabled) {
            enableSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(context, R.color.switch_thumb_enabled), android.graphics.PorterDuff.Mode.MULTIPLY);
            enableSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(context, R.color.switch_track_enabled), android.graphics.PorterDuff.Mode.MULTIPLY);
        } else {
            enableSwitch.getThumbDrawable().setColorFilter(ContextCompat.getColor(context, R.color.switch_thumb_disabled), android.graphics.PorterDuff.Mode.MULTIPLY);
            enableSwitch.getTrackDrawable().setColorFilter(ContextCompat.getColor(context, R.color.switch_track_disabled), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
    }
}
