package com.example.guidemaps.Location;

import static com.example.guidemaps.User.PostPlaces.EXTRA_MAP_TITLE;
import static com.example.guidemaps.User.PostPlaces.lugares;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.widget.SearchView;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.guidemaps.Common.LoginSignup.Login;
import com.example.guidemaps.Models.Favourites;
import com.example.guidemaps.Models.Place;
import com.example.guidemaps.Models.User;
import com.example.guidemaps.R;
import com.example.guidemaps.User.AllCategories;
import com.example.guidemaps.User.FavsActivity;
import com.example.guidemaps.User.PostPlaces;
import com.example.guidemaps.User.ProfileActivity;
import com.example.guidemaps.databinding.ActivityCreateMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateMapActivity extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final String TAG = "CreateMapActivity";
    private GoogleMap mMap;
    private SearchView searchView;
    //private List<Marker> markers = new ArrayList<>();

    public static List<Place> lugares = new ArrayList<Place>();
    private static List<String> lugaresDownloadUrl = new ArrayList<String>();

    private static User usuario = null;

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
        setContentView(R.layout.activity_create_map);

        menuIcon = findViewById(R.id.menu_icon);
        contentView = findViewById(R.id.content);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        navigationDrawer();

        searchView = findViewById(R.id.idSearchView);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String location = searchView.getQuery().toString();

                List<Address> addressList = null;

                if (location != null || location.equals("")) {

                    Geocoder geocoder = new Geocoder(CreateMapActivity.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Address address = addressList.get(0);

                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    //mMap.addMarker(new MarkerOptions().position(latLng).title(location).snippet(location));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

                    showAlertDialog(latLng);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapFragment.getMapAsync(this);

        if(mapFragment.getView() != null) {
            Snackbar snackbar = Snackbar.make(mapFragment.getView(), "Mantén presionado para añadir un marcador", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.setActionTextColor(ContextCompat.getColor(this,android.R.color.white));
            snackbar.show();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        /*mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker markerToDelete) {
                Log.i(TAG, "onWindowClickListener- delete this marker");
                markers.remove(markerToDelete);
                markerToDelete.remove();
            }
        });*/

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.i(TAG, "onMapLongClickListener");
                showAlertDialog(latLng);
            }

        });

        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    private void showAlertDialog(LatLng latLng) {
        View placeFormView = getLayoutInflater().inflate(R.layout.dialog_create_place, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Crear un marcador")
                .setView(placeFormView)
                .setNegativeButton("Cancelar",null)
                .setPositiveButton("OK",null).show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etTitle = placeFormView.findViewById(R.id.etTitle);
                EditText etDesc = placeFormView.findViewById(R.id.etDescription);
                EditText etImg = placeFormView.findViewById(R.id.etImagen);
                String title = etTitle.getText().toString();
                String desc = etDesc.getText().toString();
                String img = etImg.getText().toString();
                if(title.trim().isEmpty() || desc.trim().isEmpty() || img.trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                mMap.addMarker(new MarkerOptions().position(latLng).title(title).snippet(desc));

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("lugar");

                myRef.child(title).setValue(new Place(title, latLng.latitude, latLng.longitude, img, desc));
                //markers.add(marker);
                dialog.dismiss();

                Intent intent_places = new Intent(getApplicationContext(), Login.class);
                intent_places.putExtra("usuario", usuario);
                intent_places.putExtra("lugares", (Serializable) PostPlaces.lugares);
                startActivity(intent_places) ;
            }
        });

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
                startActivity(intent_home);
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

    public void savePlace(View view) {
        /*if(markers.isEmpty()) {
            Toast.makeText(this, "Debe haber al menos un marcador en el mapa", Toast.LENGTH_LONG).show();
        }*/

    }
}