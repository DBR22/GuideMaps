package com.example.guidemaps.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.guidemaps.Models.User;
import com.example.guidemaps.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class ProfileActivity extends AppCompatActivity {

    private static User usuario;
    private EditText textViewUsuario, textViewEmail;
    private ImageView imageView;
    protected static Uri uriRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        usuario = (User) intent.getSerializableExtra("usuario");
        imageView = findViewById(R.id.imagenPerfil);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        getDownloadUrlUser(storage.getReference().child("users/"+usuario.getIdUsuario()));
        textViewUsuario = findViewById(R.id.nombreUsuario);
        textViewEmail = findViewById(R.id.emailUsuario);
        if ( uriRes != null ) {
            Glide.with(getApplicationContext()).load(uriRes).dontTransform().into(imageView);
        } else {
            Log.d("Test:","Loading");
        }
        getSupportActionBar().setSubtitle("Perfil");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textViewUsuario.setText(usuario.getNombre());
        textViewEmail.setText(usuario.getEmail());
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
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
        }
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
        usuario.setNombre(textViewUsuario.getText().toString());
        usuario.setEmail(textViewEmail.getText().toString());
        PostPlaces.usuario.setNombre(textViewUsuario.getText().toString());
        PostPlaces.usuario.setEmail(textViewEmail.getText().toString());
        myRef.child("nombre").setValue(textViewUsuario.getText().toString());
        myRef.child("email").setValue(textViewEmail.getText().toString());
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}