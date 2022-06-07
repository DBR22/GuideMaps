package com.example.guidemaps.User;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidemaps.HelperClasses.PlaceAdapter;
import com.example.guidemaps.Models.Favourites;
import com.example.guidemaps.Models.Place;
import com.example.guidemaps.Models.User;
import com.example.guidemaps.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FavsActivity extends AppCompatActivity {

    private static Favourites favouritePlaces = null;
    RecyclerView recyclerView;
    private PlaceAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<Place> places = new ArrayList<Place>();

    private User usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favs);

        Intent intent = getIntent();

        favouritePlaces = (Favourites) intent.getSerializableExtra("lugaresFavoritos");

        usuario = (User) intent.getSerializableExtra("usuario");

        places = favouritePlaces.getFavouritePlaces();

        getSupportActionBar().setSubtitle("Mis lugares favoritos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

}
