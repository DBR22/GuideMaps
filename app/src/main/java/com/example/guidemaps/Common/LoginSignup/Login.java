package com.example.guidemaps.Common.LoginSignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.guidemaps.R;
import com.example.guidemaps.User.AllCategories;
import com.example.guidemaps.User.UserDashboard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.auth.User;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;

    ImageView backBtn;
    Button login, signup;
    TextView titleText;
    TextInputLayout email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_retailer_login);
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);

        login = findViewById(R.id.login_button);
        signup = findViewById(R.id.signup_button);
        titleText = findViewById(R.id.signup_title);
        backBtn = findViewById(R.id.login_back_button);
        backBtn.setOnClickListener(view -> Login.super.onBackPressed());

        login.setOnClickListener(view -> {
            userLogin();
        });
    }

    public void callSignup(View view) {

        Intent intent = new Intent(getApplicationContext(),SignUp.class);

        Pair[] pairs = new Pair[4];

        pairs[0] = new Pair<View,String>(backBtn,"transition_back_arrow_btn");
        pairs[1] = new Pair<View,String>(signup,"transition_next_btn");
        pairs[2] = new Pair<View,String>(login,"transition_login_btn");
        pairs[3] = new Pair<View,String>(titleText,"transition_title_btn");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Login.this,pairs);
            startActivity(intent, options.toBundle());
        } else startActivity(intent);

    }

    public void userLogin() {
        String email_text = email.getEditText().getText().toString().trim();
        String password_text = password.getEditText().getText().toString().trim();

        if(TextUtils.isEmpty(email_text)) {
            email.setError("Introduce un nombre de usuario");
            email.requestFocus();
        } else if(TextUtils.isEmpty(password_text)) {
            Toast.makeText(Login.this, "Introduce una contraseña", Toast.LENGTH_SHORT).show();
            password.requestFocus();
        } else {

            mAuth.signInWithEmailAndPassword(email_text, password_text).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(Login.this, "Inicio de Sesión Completado", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Login.this, UserDashboard.class));
                    } else {
                        Log.w("TAG", "Error:",task.getException());
                        Toast.makeText(Login.this, "Usuario no registrado", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }

}