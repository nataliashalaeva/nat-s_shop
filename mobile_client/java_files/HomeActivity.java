package com.example.myshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageButton btnHome = findViewById(R.id.btnHome);
        ImageButton btnCart = findViewById(R.id.btnCart);
        ImageButton btnFavorite = findViewById(R.id.btnFavorite);
        ImageButton btnAccount = findViewById(R.id.btnAccount);

        recyclerView = findViewById(R.id.recycler_view_product_list);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("Products");

        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Product product = snap.getValue(Product.class);
                    if (product != null) {
                        try {
                            int id = Integer.parseInt(snap.getKey());
                            product.setId(id); // Устанавливаем ID из ключа
                            productList.add(product);
                        } catch (NumberFormatException e) {
                            e.printStackTrace(); // если ключ не число — просто пропустим
                        }
                    }
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });

        btnHome.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        btnFavorite.setOnClickListener(v -> startActivity(new Intent(this, FavoriteActivity.class)));
        btnAccount.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
    }
}
