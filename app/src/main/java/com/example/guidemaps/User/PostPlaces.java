package com.example.guidemaps.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.guidemaps.HelperClasses.PlaceAdapter;
import com.example.guidemaps.Location.NewPlace;
import com.example.guidemaps.Models.Favourites;
import com.example.guidemaps.Models.Place;
import com.example.guidemaps.Models.User;
import com.example.guidemaps.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonParser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostPlaces extends AppCompatActivity {

    RecyclerView recyclerView;
    public static PlaceAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private FloatingActionButton floatingActionButton;

    public static List<Place> lugares = new ArrayList<Place>();

    protected static User usuario = null;

    private static Favourites lugaresFavoritos = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_places);

        final Intent intent = getIntent();

        pedirPermisos();
        usuario = (User) intent.getSerializableExtra("usuario");

        lugares = (List<Place>) intent.getSerializableExtra("lugares");

        getSupportActionBar().setSubtitle("Todos los lugares");

        recyclerView = findViewById(R.id.recyclerLugares);

        layoutManager = new LinearLayoutManager(this);
        floatingActionButton = findViewById(R.id.addPhoto);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        });

        recyclerView.setLayoutManager(layoutManager);
        adapter = new PlaceAdapter(R.layout.lugar, this, lugares, new PlaceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Place lugar, int position) {
                Intent intent1 = new Intent(getApplicationContext(), PlaceActivity.class);
                intent1.putExtra("lugar", lugar);
                startActivity(intent1);
            }
        });
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        Toast.makeText(this, "Mantén presionado en el lugar que quieras para añadirlo a favoritos", Toast.LENGTH_SHORT).show();

        if (usuario != null) Snackbar.make(findViewById(R.id.recyclerLugares), "¡Bienvenido/a " + usuario.getNombre() + "!", Snackbar.LENGTH_LONG).show();

        if (usuario != null) downloadFavsFromFirebase(usuario.getIdUsuario());

        registerForContextMenu(recyclerView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Intent intent = new Intent(getApplicationContext(), NewPlace.class);
            Bundle extras = data.getExtras();
            intent.putExtra("lugarBundle", extras);
            startActivity(intent);
        }
    }

    private void pedirPermisos(){
        Dexter.withContext(getApplicationContext())
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "Todos los permisos garantizados.", Toast.LENGTH_SHORT).show();
                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {

                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "¡Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    @Override
    public void onBackPressed() {
        AlertDialog alertDialog = new AlertDialog.Builder(PostPlaces.this).create();
        alertDialog.setTitle("Cerrar sesión");
        alertDialog.setMessage("¿Estás seguro de cerrar sesión?");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                lugares.clear();
                FirebaseAuth.getInstance().signOut();
                PostPlaces.super.onBackPressed();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_place, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_place_addfav, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Place isAdded = lugares.get(adapter.getPosition());
        addFavoritos(isAdded);
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.cerrarsesionmenu:
                lugaresFavoritos = null;
                FirebaseAuth.getInstance().signOut();
                super.finish();
                break;

            case R.id.mislugaresmenu:
                Intent intent = new Intent(getApplicationContext(), FavsActivity.class);
                intent.putExtra("lugaresFavoritos", lugaresFavoritos);
                intent.putExtra("usuario", usuario);
                startActivity(intent);
                break;
            case R.id.perfilmenu:
                intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("usuario", usuario);
                startActivity(intent);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    protected void downloadFavsFromFirebase(final String idUsuario) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("favoritos/" + idUsuario);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //JsonParser parser = new JsonParser();
                /**
                 * Me crea en la bbdd unos nuevos favoritos si el usuario es nuevo
                 */
                if (dataSnapshot.getValue() == null) {
                    crearNuevosFavsEnFirebase(idUsuario);
                } else {
                    lugaresFavoritos = new Favourites(idUsuario);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (!snapshot.getKey().equals("id")) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Object obj = snapshot1.getValue();
                                String nombre = (String) ((HashMap) obj).get("nombre");
                                String imagen = (String) ((HashMap) obj).get("imagen");
                                String descripcion = (String) ((HashMap) obj).get("descripcion");
                                double latitud = (double) ((HashMap) obj).get("latitud");
                                double longitud = (double) ((HashMap) obj).get("longitud");
                                int posicionFirebase = ((Long) ((HashMap) obj).get("posicionFirebaseFav")).intValue();
                                Place lugar = new Place(nombre, latitud, longitud, imagen, descripcion);
                                lugar.setPosicionFirebaseFav(posicionFirebase);
                                lugaresFavoritos.addPlace(lugar);
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    protected void crearNuevosFavsEnFirebase(String idUsuario) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("favoritos/" + idUsuario);
        lugaresFavoritos = new Favourites(idUsuario);
        myRef.setValue(new Favourites(idUsuario, lugaresFavoritos.getFavouritePlaces()));
    }

    protected void addFavoritos(Place seAnyade) {
        Toast.makeText(getApplicationContext(), "Se añade a favoritos " + seAnyade.getNombre(), Toast.LENGTH_SHORT).show();
        seAnyade.setPosicionFirebaseFav(lugaresFavoritos.getFavouritePlaces().size());
        if (!lugaresFavoritos.getFavouritePlaces().contains(seAnyade)) {
            lugaresFavoritos.addPlace(seAnyade);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("favoritos/" + usuario.getIdUsuario());
            ref.setValue(lugaresFavoritos);
        } else {
            Toast.makeText(this, "No se ha añadido a favoritos, ese lugar ya existe en favoritos.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}