package com.example.servicecenterapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserManageActivity extends AppCompatActivity {

    private static final String TAG = "UserManageActivity";
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<DocumentSnapshot> userList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manage);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(userAdapter);

        fetchUsers();
    }

    private void fetchUsers() {
    db.collection("users")
        .whereEqualTo("user_type", 0)
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    userList.clear();
                    userList.addAll(querySnapshot.getDocuments());

                    // Sort the list: move users with null or empty customer_id to the beginning
                    userList.sort((user1, user2) -> {
                        String customerId1 = user1.getString("customer_id");
                        String customerId2 = user2.getString("customer_id");

                        boolean isCustomerId1Empty = (customerId1 == null || customerId1.isEmpty());
                        boolean isCustomerId2Empty = (customerId2 == null || customerId2.isEmpty());

                        if (isCustomerId1Empty && !isCustomerId2Empty) {
                            return -1;
                        } else if (!isCustomerId1Empty && isCustomerId2Empty) {
                            return 1;
                        } else {
                            return 0;
                        }
                    });

                    userAdapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(UserManageActivity.this, "Failed to fetch users: " + task.getException(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to fetch users", task.getException());
            }
        });
    }

}
