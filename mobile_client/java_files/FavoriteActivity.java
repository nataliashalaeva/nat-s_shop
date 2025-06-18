package com.example.myshop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavoriteAdapter favoriteAdapter;
    private List<Product> favoriteList;
    private TextView emptyMessage;
    private DatabaseReference productsRef, userFavRef,productsDataRef;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        ImageButton btnHome = findViewById(R.id.btnHome);
        ImageButton btnCart = findViewById(R.id.btnCart);
        ImageButton btnAccount = findViewById(R.id.btnAccount);
        ImageButton btnFav = findViewById(R.id.btnFavorite);
        emptyMessage = findViewById(R.id.empty_message);

        recyclerView = findViewById(R.id.recycler_view_favorite);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        favoriteList = new ArrayList<>();
        favoriteAdapter = new FavoriteAdapter(favoriteList);
        recyclerView.setAdapter(favoriteAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userFavRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("Favorites");
            productsDataRef = FirebaseDatabase.getInstance().getReference("Products");


            userFavRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    favoriteList.clear();
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Product product = snap.getValue(Product.class);
                        checkProductAvailability(product);
                    }
                    favoriteAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(FavoriteActivity.this, "Ошибка загрузки избранного", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnHome.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        btnAccount.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
        btnFav.setOnClickListener(v -> startActivity(new Intent(this, FavoriteActivity.class)));
    }


    // Проверка доступности товара в актуальных продуктах
    private void checkProductAvailability(Product product) {
        productsDataRef.orderByChild("imageUrl").equalTo(product.getImageUrl()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Товар был удален или выкуплен
                    removeProductFromFavoritesAndCart(product);
                } else {
                    // Товар актуален, добавляем в список избранных
                    favoriteList.add(product);
                }
                // После проверки всех товаров обновляем адаптер
                favoriteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FavoriteActivity.this, "Ошибка при проверке товара", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Удаление товара из избранного и корзины с уведомлением
    private void removeProductFromFavoritesAndCart(Product product) {
        // Удаляем товар из избранного
        userFavRef.orderByChild("imageUrl").equalTo(product.getImageUrl()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        snap.getRef().removeValue();
                    }
                    Log.d("FavoriteActivity", "Товар с URL " + product.getImageUrl() + " успешно удален из избранного.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("FavoriteActivity", "Ошибка при удалении товара из избранного по URL.");
            }
        });

        // Удаляем товар из корзины
        DatabaseReference userCartRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(currentUser.getUid())
                .child("Cart");

        userCartRef.orderByChild("imageUrl").equalTo(product.getImageUrl()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        snap.getRef().removeValue();
                    }
                    Log.d("FavoriteActivity", "Товар с URL " + product.getImageUrl() + " успешно удален из корзины.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("FavoriteActivity", "Ошибка при удалении товара из корзины по URL.");
            }
        });

        // Показываем сообщение пользователю
        Toast.makeText(FavoriteActivity.this, "Товар " + product.getName() + " был выкуплен другим покупателем", Toast.LENGTH_LONG).show();
    }
}
