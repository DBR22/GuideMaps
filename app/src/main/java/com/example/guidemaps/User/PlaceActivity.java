package com.example.guidemaps.User;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.guidemaps.Models.Place;
import com.example.guidemaps.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PlaceActivity extends AppCompatActivity implements OnMapReadyCallback {

    TextView tv;
    ImageView iv;

    GoogleMap map;

    Place place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        Intent intent = getIntent();

        place = (Place) intent.getSerializableExtra("lugar");

        getSupportActionBar().setSubtitle(place.getNombre());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tv = findViewById(R.id.nombreLug);
        iv = findViewById(R.id.imagenLugar);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tv.setText(place.getNombre());
        Glide.with(this).load(place.getImagen()).into(iv);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (place != null) {
            LatLng deLugar = new LatLng(place.getLatitud(), place.getLongitud());
            int zoom = 8;
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deLugar, zoom));
            googleMap.addMarker(new MarkerOptions().position(deLugar).title(place.getNombre()).draggable(true));
            map = googleMap;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}
