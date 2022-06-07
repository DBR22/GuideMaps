package com.example.guidemaps.Common.LoginSignup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.SettingInjectorService;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.guidemaps.Models.Place;
import com.example.guidemaps.R;
import com.example.guidemaps.User.AllCategories;
import com.example.guidemaps.User.UserDashboard;
import com.example.guidemaps.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAnalytics firebaseAnalytics;

    private static List<Place> places = new ArrayList<Place>();
    private static List<String> placesDownloadUrl = new ArrayList<String>();

    ImageView backBtn;
    Button login, signup;
    TextView titleText;
    TextInputLayout email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_retailer_login);
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putSerializable(FirebaseAnalytics.Param.START_DATE, new Date());
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
        mAuth = FirebaseAuth.getInstance();

        places.clear();
        readFromFirebase();

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

        if(!isConnected(this)) {
            showCustomDialog();
        }

        String email_text = email.getEditText().getText().toString().trim();
        String password_text = password.getEditText().getText().toString().trim();

        if(TextUtils.isEmpty(email_text)) {
            email.setError("Introduce un email");
            email.requestFocus();
        } else if(TextUtils.isEmpty(password_text)) {
            password.setError("Introduce una contraseña");
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

                    /*if (task.isSuccessful()) {

                        FirebaseDatabase db = FirebaseDatabase.getInstance() ;

                        DatabaseReference ref = db.getReference("users") ;

                        // Obtenemos la información del usuario
                        ref.child(mAuth.getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {

                                            // Rescatamos la información devuelta por Firebase
                                            User user = dataSnapshot.getValue(User.class) ;

                                            Intent intent = null;
                                            if (user.getEmail().equals("admin@gmail.com")) {
                                                Toast.makeText(Login.this, "Eres admin", Toast.LENGTH_LONG).show();
                                                intent = new Intent(Login.this, UserDashboard.class);
                                            } else {
                                                Toast.makeText(Login.this, "Test", Toast.LENGTH_LONG).show();
                                                intent = new Intent(Login.this, UserDashboard.class) ;
                                            }

                                            intent.putExtra("user", user);
                                            intent.putExtra("lugares", (Serializable) places);
                                            intent.putExtra("lugaresDownload", (Serializable) placesDownloadUrl);

                                            intent.putExtra("botonGoogle", false);
                                            // Lanzar la actividad ListActivity
                                            startActivity(intent) ;

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    } else {
                        Toast.makeText(Login.this, "El usuario o la contraseña son erróneos", Toast.LENGTH_LONG).show();
                    }*/
                }
            });

        }

    }

    private boolean isConnected(Login login) {

        ConnectivityManager connectivityManager = (ConnectivityManager) login.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected())) {
            return true;
        } else {
            return false;
        }

    }

    private void showCustomDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setMessage("Por favor, conéctese a Internet para continuar")
                .setCancelable(false)
                .setPositiveButton("Conectar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), Login.class));
                        finish();
                    }
                });

        builder.show();

    }

    private void readFromFirebase() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("lugar");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Object obj = ds.getValue();
                    String nombre = (String) ((HashMap) obj).get("nombre");
                    String imagen = (String) ((HashMap) obj).get("imagen");
                    String descripcion = (String) ((HashMap) obj).get("descripcion");
                    double latitud = (double) ((HashMap) obj).get("latitud");
                    double longitud = (double) ((HashMap) obj).get("longitud");
                    Place lugar = new Place(nombre, latitud, longitud, imagen, descripcion);
                    places.add(lugar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error:", "Database Error Lugares");
            }
        });

    }

}