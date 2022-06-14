package com.example.guidemaps.User;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.guidemaps.Adapters.PlaceAdapter;
import com.example.guidemaps.Common.LoginSignup.Login;
import com.example.guidemaps.Location.CreateMapActivity;
import com.example.guidemaps.Models.Favourites;
import com.example.guidemaps.Models.Place;
import com.example.guidemaps.Models.User;
import com.example.guidemaps.R;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostPlaces extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView;
    int SELECT_PICTURE = 200;
    public static final String EXTRA_USER_MAP = "EXTRA_USER_MAP";
    public static final String EXTRA_MAP_TITLE = "EXTRA_MAP_TITLE";
    public static PlaceAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    public static List<Place> lugares = new ArrayList<Place>();
    private static List<String> lugaresDownloadUrl = new ArrayList<String>();

    protected static User usuario = null;

    private static Favourites lugaresFavoritos = null;

    ImageView menuIcon;
    LinearLayout contentView;

    //Drawer Menu
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    //Variables
    static final float END_SCALE = 0.7f;

    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    /*Intent intent = new Intent(getApplicationContext(), NewPlace.class);
                    Intent data = result.getData();
                    intent.putExtras(data);
                    if (data != null && data.getData() != null) {
                        //startActivity(intent);
                        Log.i("Datos Error", " " + data);
                    }*/
                    Intent intent = new Intent(getApplicationContext(), CreateMapActivity.class);
                    startActivity(intent);
                }
            });
            /*new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result != null && result.getResultCode() == RESULT_OK) {
                        if(result.getData() != null) {
                            Intent intent = new Intent(getApplicationContext(), NewPlace.class);
                            Bundle extras = result.getData().getExtras();
                            if(extras == null)Toast.makeText(getApplicationContext(), "Extras NULL: " + result, Toast.LENGTH_SHORT).show();
                            Log.i("Extras", "" + result.getData().getExtras());
                            intent.putExtra("lugarBundle", extras);
                            startActivity(intent);
                        }
                    }
                }
            });*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_places);

        menuIcon = findViewById(R.id.menu_icon);
        contentView = findViewById(R.id.content);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        navigationDrawer();

        final Intent intent = getIntent();

        pedirPermisos();
        usuario = (User) intent.getSerializableExtra("usuario");

        lugares = (List<Place>) intent.getSerializableExtra("lugares");

        //getSupportActionBar().setSubtitle("Todos los lugares");

        recyclerView = findViewById(R.id.recyclerLugares);

        layoutManager = new LinearLayoutManager(this);

        /*floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iGallery = new Intent(Intent.ACTION_PICK);
                iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startForResult.launch(iGallery);

                Intent iGallery = new Intent();
                iGallery.setType("image/*");
                iGallery.setAction(Intent.ACTION_GET_CONTENT);

                launchSomeActivity.launch(iGallery);
                Intent intMap = new Intent(getApplicationContext(), CreateMapActivity.class);
                //intMap.putExtra(EXTRA_MAP_TITLE, "new map name");
                startForResult.launch(intMap);
            }
        });*/

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

    /*@Override
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
    }*/

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
    protected void onResume() { super.onResume(); }





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
                intent_home.putExtra("lugares", (Serializable) lugares);
                intent_home.putExtra("lugaresDownload", (Serializable) lugaresDownloadUrl);
                intent_home.putExtra("botonGoogle", false);
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

    public void addPlace(View view) {
        startActivity(new Intent(getApplicationContext(), CreateMapActivity.class));
    }
}