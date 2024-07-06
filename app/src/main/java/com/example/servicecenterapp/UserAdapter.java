package com.example.servicecenterapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

        holder.customerIdTextView.setText(customerId);
        holder.firstNameTextView.setText(firstName);
        holder.emailTextView.setText(email);
        holder.enableSwitch.setChecked(isEnabled);

        holder.enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            db.collection("users").document(userId)
                    .update("enabled", isChecked)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "User " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to update user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        holder.enableSwitch.setChecked(!isChecked);
                    });

            // Enable or disable login for this user
            if (!isChecked) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    auth.signOut();
                }
            }
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
}
