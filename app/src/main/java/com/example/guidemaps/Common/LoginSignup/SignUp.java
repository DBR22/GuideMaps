package com.example.guidemaps.Common.LoginSignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.guidemaps.Models.User;
import com.example.guidemaps.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    ImageView backBtn;
    Button next, login;
    TextView titleText;

    TextInputLayout fullName, username, email, password;

    private String userID;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_retailer_sign_up);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        backBtn = findViewById(R.id.signup_back_button);
        next = findViewById(R.id.signup_next_button);
        login = findViewById(R.id.signup_login_button);
        titleText = findViewById(R.id.signup_title_text);

        fullName = findViewById(R.id.signup_fullname);
        username = findViewById(R.id.signup_username);
        email = findViewById(R.id.signup_email);
        password = findViewById(R.id.signup_password);

        backBtn.setOnClickListener(view -> SignUp.super.onBackPressed());

    }

    public void createUser(View view) {

        if(!validateFullName() | !validateUsername() | !validateEmail() | !validatePassword()){
            return;
        }

        mAuth.createUserWithEmailAndPassword(email.getEditText().getText().toString().trim(), password.getEditText().getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                /*if(task.isSuccessful()) {
                    userID = mAuth.getCurrentUser().getUid();
                    DocumentReference documentReference = db.collection("users").document(userID);

                    Map<String,Object> user = new HashMap<>();
                    user.put("Nombre",fullName.getEditText().getText().toString().trim());
                    user.put("Nickname",username.getEditText().getText().toString().trim());
                    user.put("Email",email.getEditText().getText().toString().trim());
                    user.put("Contraseña",password.getEditText().getText().toString().trim());

                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("TAG", "onSuccess: Datos Registrados"+userID);
                        }
                    });

                    Toast.makeText(SignUp.this, "Usuario Registrado", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUp.this, Login.class));
                } else {
                    Toast.makeText(SignUp.this, "Usuario no registrado"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }*/

                if (task.isSuccessful()) {

                    db = FirebaseDatabase.getInstance();

                    DatabaseReference ref = db.getReference("usuario");

                    FirebaseUser fbUser = mAuth.getCurrentUser();

                    String uid = fbUser.getUid();

                    User miUsuario = new User(uid, fullName.getEditText().getText().toString().trim(),
                            username.getEditText().getText().toString().trim(),
                            email.getEditText().getText().toString().trim(), null);

                    ref.child(uid).setValue(miUsuario);

                    Toast.makeText(getApplicationContext(), "Se ha creado con éxito", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), Login.class);

                    intent.putExtra("email", email.getEditText().getText().toString().trim());

                    finish();
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Error en el registro"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void callNextLoginScreen(View view) {

        Intent intent = new Intent(getApplicationContext(),Login.class);

        Pair[] pairs = new Pair[4];

        pairs[0] = new Pair<View,String>(backBtn,"transition_back_arrow_btn");
        pairs[1] = new Pair<View,String>(next,"transition_next_btn");
        pairs[2] = new Pair<View,String>(login,"transition_login_btn");
        pairs[3] = new Pair<View,String>(titleText,"transition_title_btn");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SignUp.this,pairs);
            startActivity(intent,options.toBundle());
        } else startActivity(intent);

    }

    // Validation Functions
    private boolean validateFullName() {
        String val = fullName.getEditText().getText().toString().trim();

        if(val.isEmpty()){
            fullName.setError("El campo no puede estar vacío");
            return false;
        } else {
            fullName.setError(null);
            fullName.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateUsername() {
        String val = username.getEditText().getText().toString().trim();
        String checkspaces = "\\A\\w{1,20}\\z";

        if(val.isEmpty()) {
            username.setError("El campo no puede estar vacío");
            return false;
        } else if(val.length()>20) {
            username.setError("¡El nombre de usuario es demasiado largo!");
            return false;
        } else if(!val.matches(checkspaces)) {
            username.setError("¡No se permiten espacios en blanco!");
            return false;
        } else {
            fullName.setError(null);
            fullName.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateEmail() {
        String val = email.getEditText().getText().toString().trim();

        if (!val.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(val).matches()) {
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        } else {
            email.setError("¡Introduce un email válido!");
            return false;
        }

    }

    private boolean validatePassword() {
        String val = password.getEditText().getText().toString().trim();
        String checkPassword =  "^" +
                "(?=\\S+$)" +            // no white spaces
                ".{6,}" +                // at least 6 characters
                "$";

        if(val.isEmpty()) {
            password.setError("El campo no puede estar vacío");
            return false;
        } else if(!val.matches(checkPassword)) {
            password.setError("¡La contraseña debe contener al menos 6 caracteres!");
            return false;
        } else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }

}