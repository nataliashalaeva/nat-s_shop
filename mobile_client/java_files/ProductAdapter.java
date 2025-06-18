package com.example.myshop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.rey.material.widget.ImageButton;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Загружаем изображение из imageUrl
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .into(holder.imageView);


        holder.nameTextView.setText(product.getName());
        holder.priceTextView.setText(product.getPrice());

        holder.addToCartButton.setOnClickListener(v -> addToCart(product));
        holder.addToFavoritesButton.setOnClickListener(v -> addToFavorites(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageButton addToFavoritesButton;
        ImageView imageView;
        TextView nameTextView, priceTextView;
        Button addToCartButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
            addToFavoritesButton = itemView.findViewById(R.id.addToFavoritesButton);
        }
    }

    private void addToCart(Product product) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userCartRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("Cart");

            userCartRef.child(String.valueOf(product.getId())).setValue(product);
        }
    }

    private void addToFavorites(Product product) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userFavRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("Favorites");

            userFavRef.child(String.valueOf(product.getId())).setValue(product);
        }
    }

}
