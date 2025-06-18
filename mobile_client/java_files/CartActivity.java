package com.example.myshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<Product> cartList;
    private TextView emptyCartText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ImageButton btnHome = findViewById(R.id.btnHome);
        ImageButton btnCart = findViewById(R.id.btnCart);
        ImageButton btnFav = findViewById(R.id.btnFavorite);
        ImageButton btnAccount = findViewById(R.id.btnAccount);
        emptyCartText = findViewById(R.id.emptyCartText);

        recyclerView = findViewById(R.id.recycler_view_cart);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartList = new ArrayList<>();
        cartAdapter = new CartAdapter(cartList);
        recyclerView.setAdapter(cartAdapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userCartRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("Cart");
            userCartRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    cartList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Product product = snapshot.getValue(Product.class);
                        cartList.add(product);
                    }
                    cartAdapter.notifyDataSetChanged();

                    if (cartList.isEmpty()) {
                        emptyCartText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyCartText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Обработка ошибки чтения данных из базы данных
                    Toast.makeText(CartActivity.this, "Ошибка чтения данных из базы данных", Toast.LENGTH_SHORT).show();
                }
            });
        }

        MaterialButton btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnPlaceOrder.setOnClickListener(v -> {
            if (!cartList.isEmpty()) {
                FirebaseUser currentUser1 = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser1 != null) {
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users")
                            .child(currentUser1.getUid());

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.child("name").getValue(String.class);
                            String phone = snapshot.child("phone").getValue(String.class);
                            String telegram = snapshot.child("telegram").getValue(String.class);
                            String address = snapshot.child("address").getValue(String.class);

                            if (name != null && !name.isEmpty()
                                    && phone != null && !phone.isEmpty()
                                    && telegram != null && !telegram.isEmpty()
                                    && address != null && !address.isEmpty()) {
                                placeOrderWithPayment();
                            } else {
                                Toast.makeText(CartActivity.this, "Заполните профиль перед оформлением заказа", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(CartActivity.this, ProfileActivity.class);
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(CartActivity.this, "Ошибка проверки данных профиля", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(this, "Добавьте товары в корзину", Toast.LENGTH_SHORT).show();
            }
        });

        btnHome.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        btnAccount.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
        btnFav.setOnClickListener(v -> startActivity(new Intent(this, FavoriteActivity.class)));

        }

    private void placeOrderWithPayment() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(currentUser.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String address = snapshot.child("address").getValue(String.class);
                if (address == null) address = "не указан";

                int[] totalAmount = {0};  // Используем массив из одного элемента
                for (Product p : cartList) {
                    totalAmount[0] += Integer.parseInt(p.getPrice());
                }

                TextView messageView = new TextView(CartActivity.this);
                messageView.setText(Html.fromHtml(
                        "<b>Сумма заказа:</b> " + totalAmount[0] + "₽<br><br>" +
                                "Заказ будет отправлен СДЭК в ближайший пункт выдачи к адресу:<br>" +
                                address + "<br><br>" +
                                "<a href='https://www.sberbank.com/sms/pbpn?requisiteNumber=79892674844'>Перейти к оплате</a>"
                ));
                messageView.setMovementMethod(LinkMovementMethod.getInstance());
                messageView.setPadding(40, 40, 40, 0);

                new androidx.appcompat.app.AlertDialog.Builder(CartActivity.this)
                        .setTitle("Подтверждение оплаты")
                        .setView(messageView)
                        .setPositiveButton("Оплачено", (dialog, which) -> {
                            Order order = new Order();
                            order.setUserId(currentUser.getUid());
                            order.setProductList(new ArrayList<>(cartList));
                            order.setTotalAmount(totalAmount[0]);
                            order.setTimestamp(System.currentTimeMillis());
                            order.setStatus("оформлен");

                            saveOrderToDatabase(order);
                        })
                        .setNegativeButton("Отмена", null)
                        .show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, "Ошибка получения адреса", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveOrderToDatabase(Order order) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        DatabaseReference ordersRef = database.child("Orders");
        String orderId = ordersRef.push().getKey();

        if (orderId != null) {
            ordersRef.child(orderId).setValue(order)
                    .addOnSuccessListener(unused -> {
                        for (Product product : order.getProductList()) {
                            String productId = String.valueOf(product.getId());
                            database.child("Products").child(productId).removeValue();
                            database.child("ArchivedProducts").child(productId).setValue(product);
                        }
                        database.child("Users").child(currentUser.getUid()).child("Cart").removeValue();
                        Toast.makeText(CartActivity.this, "Заказ успешно оформлен", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CartActivity.this, "Не удалось сохранить заказ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}
