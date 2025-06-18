package com.example.myshop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, phoneTextView, addressTextView, telegramTextView;
    private DatabaseReference usersRef;
    private FirebaseUser currentUser;
    private Button Exit, saveButton;
    private ImageButton btnHome, btnCart, btnFav, btnAccount, myButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameTextView = findViewById(R.id.nameEditText);
        phoneTextView = findViewById(R.id.phoneEditText);
        addressTextView = findViewById(R.id.addressEditText);
        telegramTextView = findViewById(R.id.telegramEditText);
        Exit = findViewById(R.id.button_home);
        btnHome = findViewById(R.id.btnHome);
        btnCart = findViewById(R.id.btnCart);
        btnFav = findViewById(R.id.btnFavorite);
        btnAccount = findViewById(R.id.btnAccount);
        myButton = findViewById(R.id.button);
        saveButton = findViewById(R.id.saveButton);


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        nameTextView.setText(snapshot.child("name").getValue(String.class));
                        phoneTextView.setText(snapshot.child("phone").getValue(String.class));
                        addressTextView.setText(snapshot.child("address").getValue(String.class));
                        telegramTextView.setText(snapshot.child("telegram").getValue(String.class));
                        }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });
            // Сохранение данных
            saveButton.setOnClickListener(v -> saveProfile());

            myButton.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));

            Exit.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent exitIntent = new Intent(this, MainActivity.class);
                exitIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(exitIntent);
                finish();
            });

            btnHome.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
            btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
            btnFav.setOnClickListener(v -> startActivity(new Intent(this, FavoriteActivity.class)));
            btnAccount.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
        }
    }

    private void saveProfile() {
        String name = nameTextView.getText().toString();
        String phone = phoneTextView.getText().toString();
        String address = addressTextView.getText().toString();
        String telegram = telegramTextView.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address) || TextUtils.isEmpty(telegram)) {
            Toast.makeText(this, "Заполните данные", Toast.LENGTH_SHORT).show();
            return;
        }

        // Сохранение в Firebase
        usersRef.child("name").setValue(name);
        usersRef.child("phone").setValue(phone);
        usersRef.child("address").setValue(address);
        usersRef.child("telegram").setValue(telegram);

        Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show();
        saveButton.setVisibility(View.GONE); // Скрыть кнопку "Сохранить"
    }
}
