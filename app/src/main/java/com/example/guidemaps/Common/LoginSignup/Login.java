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
import com.example.guidemaps.User.PostPlaces;
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

    private static List<Place> lugares = new ArrayList<Place>();
    private static List<String> lugaresDownloadUrl = new ArrayList<String>();

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
        fillFirebaseDB();

        lugares.clear();
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
                    /*if(task.isSuccessful()) {
                        Toast.makeText(Login.this, "Inicio de Sesión Completado", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Login.this, UserDashboard.class));
                    } else {
                        Log.w("TAG", "Error:",task.getException());
                        Toast.makeText(Login.this, "Usuario no registrado", Toast.LENGTH_SHORT).show();
                    }*/

                    if (task.isSuccessful()) {

                        FirebaseDatabase db = FirebaseDatabase.getInstance() ;

                        DatabaseReference ref = db.getReference("usuario") ;

                        ref.child(mAuth.getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {

                                            User usuario = dataSnapshot.getValue(User.class) ;
                                            Intent intent = null;
                                            if (usuario.getEmail().equals("admin@gmail.com")) {
                                                intent = new Intent(Login.this, UserDashboard.class);
                                            } else {
                                                intent = new Intent(Login.this, PostPlaces.class) ;
                                            }
                                            intent.putExtra("usuario", usuario);
                                            intent.putExtra("lugares", (Serializable) lugares);
                                            intent.putExtra("lugaresDownload", (Serializable) lugaresDownloadUrl);

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
                    }
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

    private void fillFirebaseDB() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("lugar");

        myRef.child("Malaga").setValue(new Place("Malaga", 36.7182015,-4.519307, "https://firebasestorage.googleapis.com/v0/b/tourism-cities.appspot.com/o/lugar%2Fmalaga.jpg?alt=media&token=d8842715-74a0-4170-bb13-88460b5c7050", "descripcion"));
        myRef.child("Madrid").setValue(new Place("Madrid", 40.4378698,-3.8196207, "https://firebasestorage.googleapis.com/v0/b/tourism-cities.appspot.com/o/lugar%2Fmadrid.jpg?alt=media&token=b5dfc1e7-61a2-4170-93d2-41cbe05ec349", "descripcion"));
        myRef.child("Barcelona").setValue(new Place("Barcelona", 41.3947688,2.0787279, "https://firebasestorage.googleapis.com/v0/b/tourism-cities.appspot.com/o/lugar%2Fbarcelona.jpg?alt=media&token=6826d2b0-fe85-41c2-a8fb-1a437c993481", "descripcion"));
        myRef.child("Sevilla").setValue(new Place("Sevilla", 37.3753501,-6.0250983, "https://firebasestorage.googleapis.com/v0/b/tourism-cities.appspot.com/o/lugar%2Fsevilla.jpg?alt=media&token=49c11879-97c1-4b46-9cb8-1a7eec5c3f86", "descripcion"));
        myRef.child("Cadiz").setValue(new Place("Cadiz", 36.5163813,-6.3174866, "https://firebasestorage.googleapis.com/v0/b/tourism-cities.appspot.com/o/lugar%2Fcadiz.jpg?alt=media&token=5b1ca5d8-2057-49ad-bea1-048a5ef00c19", "descripcion"));
        myRef.child("Valencia").setValue(new Place("Valencia", 39.4077013,-0.5015956, "https://firebasestorage.googleapis.com/v0/b/tourism-cities.appspot.com/o/lugar%2Fvalencia.png?alt=media&token=34372fd2-5a09-4ba4-af1a-7282605be027", "descripcion"));
        myRef.child("Andorra").setValue(new Place("Andorra", 42.5421846,1.4575882, "https://firebasestorage.googleapis.com/v0/b/tourism-cities.appspot.com/o/lugar%2Fandorra.jpg?alt=media&token=7e8d0adb-a892-453a-a518-06209972c338", "descripcion"));


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
                    lugares.add(lugar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Error:", "Database Error Lugares");
            }
        });

    }

}