package com.example.guidemaps.Admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.example.guidemaps.User.FavsActivity;
import com.example.guidemaps.User.PlaceActivity;
import com.example.guidemaps.User.PostPlaces;
import com.example.guidemaps.User.ProfileActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PostPlacesAdmin extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView = null;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_places_admin);
        Intent intent = getIntent();
        lugares = (List<Place>) intent.getSerializableExtra("lugares");

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        menuIcon = findViewById(R.id.menu_icon);
        contentView = findViewById(R.id.content);

        navigationDrawer();

        recyclerView = findViewById(R.id.recyclerLugares);

        layoutManager = new LinearLayoutManager(this);
        if(layoutManager != null) recyclerView.setLayoutManager(layoutManager);
        adapter = new PlaceAdapter(R.layout.lugar, this, lugares, new PlaceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Place place, int position) {
                Intent intent1 = new Intent(getApplicationContext(), PlaceActivity.class);
                intent1.putExtra("lugar", place);
                startActivity(intent1);
            }
        });
        if(adapter != null) recyclerView.setAdapter(adapter);
        registerForContextMenu(recyclerView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.borrar_lugar, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Place lugar = lugares.get(adapter.getPosition());
        borrarLugar(lugar);
        return super.onContextItemSelected(item);
    }

    private void borrarLugar(Place lugar) {
        AlertDialog builder = new AlertDialog.Builder(PostPlacesAdmin.this)
                .setTitle("Borrar lugar")
                .setMessage("¿Estás seguro de querer borrar " + lugar.getNombre())
                .setNegativeButton("Cancelar",null)
                .setPositiveButton("Sí", null).show();

        builder.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference("lugar/"+lugar.getNombre());
                if(adapter != null) {
                    lugares.remove(adapter.getPosition());
                    ref.removeValue();
                    adapter.notifyItemRemoved(adapter.getPosition());
                }
                Toast.makeText(getApplicationContext(), "Lugar eliminado", Toast.LENGTH_SHORT).show();
            }
        });

        builder.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Borrar lugar cancelado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cerrar_sesion_solo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.cerrarsesionmenu:
                FirebaseAuth.getInstance().signOut();
                super.finish();
                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    public void onBackPressed() {
        lugares.clear();
        FirebaseAuth.getInstance().signOut();
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
                intent_home.putExtra("lugares", (Serializable) lugares);
                intent_home.putExtra("lugaresDownload", (Serializable) lugaresDownloadUrl);
                intent_home.putExtra("botonGoogle", false);
                startActivity(intent_home) ;
                break;
            case R.id.nav_add_missing_place:
                Intent intent_place = new Intent(getApplicationContext(), CreateMapActivity.class);
                startActivity(intent_place);
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