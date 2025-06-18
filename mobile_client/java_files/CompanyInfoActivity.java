package com.example.myshop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;

public class CompanyInfoActivity extends AppCompatActivity {

    private ImageButton btnHome, btnCart, btnFav, btnAccount, myButton;
    private Button feedbackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_info);

        myButton = findViewById(R.id.but);
        btnHome = findViewById(R.id.btnHome);
        btnCart = findViewById(R.id.btnCart);
        btnFav = findViewById(R.id.btnFavorite);
        btnAccount = findViewById(R.id.btnAccount);
        feedbackButton = findViewById(R.id.feedbackButton);

        myButton.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
        btnHome.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        btnFav.setOnClickListener(v -> startActivity(new Intent(this, FavoriteActivity.class)));
        btnAccount.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));

        feedbackButton.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:shalaeva.stepanida@gmail.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Отзыв о приложении");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Ваш отзыв...");
            startActivity(Intent.createChooser(emailIntent, "Отправить отзыв через:"));
        });

    }
}
