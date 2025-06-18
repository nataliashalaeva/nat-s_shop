package com.example.myshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private DatabaseReference ordersRef, usersRef;
    private FirebaseUser currentUser;
    private List<Order> ordersList;
    private OrderHistoryAdapter adapter;
    private ImageButton btnHome, btnCart, btnFav, btnAccount, backButton;
    private TextView nameLabel, emptyMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersList = new ArrayList<>();
        adapter = new OrderHistoryAdapter(this, ordersList);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(adapter);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));

        nameLabel = findViewById(R.id.nameLabel);
        emptyMessage = findViewById(R.id.emptyMessage);

        btnHome = findViewById(R.id.btnHome);
        btnCart = findViewById(R.id.btnCart);
        btnFav = findViewById(R.id.btnFavorite);
        btnAccount = findViewById(R.id.btnAccount);

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(adapter);

        // В onCreate() добавьте следующий код
                ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(adapter);

// Используем кастомный разделитель
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider));
        ordersRecyclerView.addItemDecoration(dividerItemDecoration);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Загружаем имя пользователя
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

            ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
            ordersRef.orderByChild("userId").equalTo(currentUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            ordersList.clear();
                            if (snapshot.exists()) {
                                emptyMessage.setVisibility(View.GONE);
                                ordersRecyclerView.setVisibility(View.VISIBLE);
                                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                                    String status = orderSnapshot.child("status").getValue(String.class);
                                    Long totalAmount = orderSnapshot.child("totalAmount").getValue(Long.class);

                                    List<Product> productList = new ArrayList<>();
                                    for (DataSnapshot productSnapshot : orderSnapshot.child("productList").getChildren()) {
                                        String name = productSnapshot.child("name").getValue(String.class);
                                        String imageUrl = productSnapshot.child("imageUrl").getValue(String.class);
                                        String price = productSnapshot.child("price").getValue(String.class);

                                        int id = productSnapshot.getKey() != null ? Integer.parseInt(productSnapshot.getKey()) : 0;

                                        if (name != null && imageUrl != null) {
                                            productList.add(new Product(id, imageUrl, name, price));
                                        }
                                    }

                                    ordersList.add(new Order(orderSnapshot.getKey(), productList, totalAmount.intValue(), currentUser.getUid(), orderSnapshot.child("timestamp").getValue(Long.class), status));
                                }
                            } else {
                                emptyMessage.setVisibility(View.VISIBLE);
                                ordersRecyclerView.setVisibility(View.GONE);
                            }
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                        }
                    });

            btnHome.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
            btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
            btnAccount.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
            btnFav.setOnClickListener(v -> startActivity(new Intent(this, FavoriteActivity.class)));
        }
    }
}
