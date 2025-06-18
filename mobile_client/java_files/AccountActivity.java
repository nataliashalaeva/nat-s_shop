package com.example.myshop;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountActivity extends AppCompatActivity {

    private Button btnProfile, btnOrderHistory;
    private ImageButton btnHome, btnCart, btnFav, btnAccount;
    private TextView nameLabel;
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;
    private Button btnCompanyInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        btnProfile = findViewById(R.id.btnProfile);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);

        btnHome = findViewById(R.id.btnHome);
        btnCart = findViewById(R.id.btnCart);
        btnFav = findViewById(R.id.btnFavorite);
        btnAccount = findViewById(R.id.btnAccount);
        nameLabel = findViewById(R.id.nameLabel);
        btnCompanyInfo = findViewById(R.id.btnCompanyInfo);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null) {
                            nameLabel.setText(name);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });
        }


        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        btnOrderHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, OrderHistoryActivity.class));
        });

        btnCompanyInfo.setOnClickListener(v -> {
            startActivity(new Intent(this, CompanyInfoActivity.class));
        });


        btnHome.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        btnFav.setOnClickListener(v -> startActivity(new Intent(this, FavoriteActivity.class)));
        btnAccount.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
    }
}
