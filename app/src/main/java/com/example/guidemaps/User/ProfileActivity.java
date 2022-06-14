package com.example.guidemaps.User;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.guidemaps.Common.LoginSignup.Login;
import com.example.guidemaps.Location.CreateMapActivity;
import com.example.guidemaps.Models.Favourites;
import com.example.guidemaps.Models.Place;
import com.example.guidemaps.Models.User;
import com.example.guidemaps.R;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static User usuario;
    private TextInputLayout textViewUsuario, textViewEmail;
    private ImageView imageView;
    protected static Uri uriRes;

    public static List<Place> lugares = new ArrayList<Place>();
    private static List<String> lugaresDownloadUrl = new ArrayList<String>();

    private static Favourites lugaresFavoritos = null;

    ImageView menuIcon, editIcon;
    LinearLayout contentView;

    //Drawer Menu
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    //Variables
    static final float END_SCALE = 0.7f;

    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result != null && result.getResultCode() == RESULT_OK) {
                if(result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    final Bitmap imageBitmap = (Bitmap) extras.get("data");

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    String idUsuario = usuario.getIdUsuario();

                    // Creamos una referencia a la carpeta y el nombre de la imagen donde se guardara
                    StorageReference mountainImagesRef = storage.getReference().child("users/" + idUsuario);
                    getDownloadUrlUser(mountainImagesRef);

                    //Pasamos la imagen a un array de byte
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] datas = baos.toByteArray();
                    // Empezamos con la subida a Firebase
                    UploadTask uploadTask = mountainImagesRef.putBytes(datas);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            imageView.setImageBitmap(imageBitmap);
                        }
                    });
                    /*Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        String idUsuario = usuario.getIdUsuario();

                        // Creamos una referencia a la carpeta y el nombre de la imagen donde se guardara
                        StorageReference mountainImagesRef = storage.getReference().child("users/" + idUsuario);
                        getDownloadUrlUser(mountainImagesRef);

                        Uri selectedImageUri = data.getData();
                        Bitmap selectedImageBitmap = null;
                        try {
                            selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                    getApplicationContext().getContentResolver(),
                                    selectedImageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        imageView.setImageBitmap(selectedImageBitmap);
                    }*/
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        menuIcon = findViewById(R.id.menu_icon);
        contentView = findViewById(R.id.content);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        navigationDrawer();

        Intent intent = getIntent();
        usuario = (User) intent.getSerializableExtra("usuario");
        imageView = findViewById(R.id.imagenPerfil);
        editIcon = findViewById(R.id.imageView3);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        getDownloadUrlUser(storage.getReference().child("users/"+usuario.getIdUsuario()));
        textViewUsuario = findViewById(R.id.nombreUsuario);
        textViewEmail = findViewById(R.id.emailUsuario);
        if ( uriRes != null ) {
            Glide.with(getApplicationContext()).load(uriRes).dontTransform().into(imageView);
        } else {
            Log.d("Test:","Loading");
        }
        //getSupportActionBar().setSubtitle("Perfil");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textViewUsuario.getEditText().setText(usuario.getNombre());
        textViewEmail.getEditText().setText(usuario.getEmail());
        editIcon.setOnClickListener(view -> {
            Toast.makeText(this,"Prueba Edit Perfil",Toast.LENGTH_LONG).show();
            /*Intent takePictureIntent = new Intent();
            takePictureIntent.setType("image/*");
            takePictureIntent.setAction(Intent.ACTION_GET_CONTENT);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startForResult.launch(takePictureIntent);
            }*/
            showAlertDialog(imageView);
        });
    }

    private void showAlertDialog(ImageView imageView) {
        View placeFormView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Nueva imagen de perfil")
                .setView(placeFormView)
                .setNegativeButton("Cancelar",null)
                .setPositiveButton("OK",null).show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etImg = placeFormView.findViewById(R.id.etImagen);
                String img = etImg.getText().toString();
                if(img.trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                imageView.setImageResource(R.drawable.profile_test);
                dialog.dismiss();
            }
        });

    }

    protected void getDownloadUrlUser(StorageReference storageReference) {

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).dontTransform().into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("usuario/" + usuario.getIdUsuario());
        usuario.setNombre(textViewUsuario.getEditText().getText().toString());
        usuario.setEmail(textViewEmail.getEditText().getText().toString());
        PostPlaces.usuario.setNombre(textViewUsuario.getEditText().getText().toString());
        PostPlaces.usuario.setEmail(textViewEmail.getEditText().getText().toString());
        myRef.child("nombre").setValue(textViewUsuario.getEditText().getText().toString());
        myRef.child("email").setValue(textViewEmail.getEditText().getText().toString());
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }




    //Navigation Drawers Functions
    private void navigationDrawer() {

        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        menuIcon.setOnClickListener((view) -> {
            if(drawerLayout.isDrawerVisible(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START);
            else drawerLayout.openDrawer(GravityCompat.START);
        });

        animateNavigationDrawer();

    }

    private void animateNavigationDrawer() {

        drawerLayout.setScrimColor(getResources().getColor(R.color.colorPrimary));
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                // Scale the View based on current slide offset
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                contentView.setScaleX(offsetScale);
                contentView.setScaleY(offsetScale);

                // Translate the View, accounting for the scaled width
                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                contentView.setTranslationX(xTranslation);
            }
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_home:
                Intent intent_home = new Intent(getApplicationContext(), PostPlaces.class);
                intent_home.putExtra("usuario", usuario);
                intent_home.putExtra("lugares", (Serializable) PostPlaces.lugares);
                intent_home.putExtra("lugaresDownload", (Serializable) lugaresDownloadUrl);
                startActivity(intent_home) ;
                break;
            case R.id.nav_add_missing_place:
                Intent intent_place = new Intent(getApplicationContext(), CreateMapActivity.class);
                startActivity(intent_place);
                break;
            case R.id.nav_favourite_place:
                Intent intent_fav = new Intent(getApplicationContext(), FavsActivity.class);
                intent_fav.putExtra("lugaresFavoritos", lugaresFavoritos);
                intent_fav.putExtra("usuario", usuario);
                startActivity(intent_fav);
                break;
            case R.id.nav_profile:
                Intent intent_prof = new Intent(getApplicationContext(), ProfileActivity.class);
                intent_prof.putExtra("usuario", usuario);
                startActivity(intent_prof);
                break;
            case R.id.nav_logout:
                Intent intent_logout = new Intent(getApplicationContext(), Login.class);
                FirebaseAuth.getInstance().signOut();
                startActivity(intent_logout);
                break;
        }

        return true;
    }

}