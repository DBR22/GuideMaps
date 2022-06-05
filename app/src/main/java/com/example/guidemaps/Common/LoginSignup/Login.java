package com.example.guidemaps.Common.LoginSignup;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.guidemaps.R;
import com.example.guidemaps.User.AllCategories;

public class Login extends AppCompatActivity {

    ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_retailer_login);

        backBtn = findViewById(R.id.login_back_button);
        backBtn.setOnClickListener(view -> Login.super.onBackPressed());
    }
}