package com.example.guidemaps.User;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidemaps.Adapters.PlaceAdapter;
import com.example.guidemaps.Common.LoginSignup.Login;
import com.example.guidemaps.Location.CreateMapActivity;
import com.example.guidemaps.Models.Favourites;
import com.example.guidemaps.Models.Place;
import com.example.guidemaps.Models.User;
import com.example.guidemaps.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FavsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static Favourites favouritePlaces = null;
    RecyclerView recyclerView;
    private PlaceAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<Place> places = new ArrayList<>();
    private static List<String> lugaresDownloadUrl = new ArrayList<String>();

    private User usuario;

    private static Favourites lugaresFavoritos = null;

    ImageView menuIcon;
    LinearLayout contentView;

    //Drawer Menu
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    //Variables
    static final float END_SCALE = 0.7f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favs);

        menuIcon = findViewById(R.id.menu_icon);
        contentView = findViewById(R.id.content);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        navigationDrawer();

        Intent intent = getIntent();

        favouritePlaces = (Favourites) intent.getSerializableExtra("lugaresFavoritos");
        Log.i("Error", " " + favouritePlaces);

        usuario = (User) intent.getSerializableExtra("usuario");

        places = favouritePlaces.getFavouritePlaces();

        //getSupportActionBar().setSubtitle("Mis lugares favoritos");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerPlaces);

        layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        adapter = new PlaceAdapter(R.layout.lugar, this, places, new PlaceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Place place, int position) {
                Intent intent1 = new Intent(getApplicationContext(), PlaceActivity.class);
                intent1.putExtra("lugar", place);
                startActivity(intent1);
            }
        });
        recyclerView.setAdapter(adapter);

        registerForContextMenu(recyclerView);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.deletefav_place_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Place place = favouritePlaces.getFavouritePlaces().get(adapter.getPosition());
        deleteFromFirebase(place.getPosicionFirebaseFav(), place);
        return super.onContextItemSelected(item);
    }

    private void deleteFromFirebase(int position, Place place) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference("favoritos/" + usuario.getIdUsuario() + "/lugaresFavoritos/" + position);
        AlertDialog alertDialog = new AlertDialog.Builder(FavsActivity.this).create();
        alertDialog.setTitle("Borrar de favoritos");
        alertDialog.setMessage("¿Estás seguro de borrar " + place.getNombre() + " de favoritos?");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "Se borra de favoritos", Toast.LENGTH_SHORT).show();
                favouritePlaces.getFavouritePlaces().remove(adapter.getPosition());
                ref.removeValue();
                adapter.notifyItemRemoved(adapter.getPosition());
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "Borrar lugar favorito cancelado", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();
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
                intent_home.putExtra("lugares", (Serializable) places);
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
}
