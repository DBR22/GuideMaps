package com.example.guidemaps.Adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guidemaps.Models.Place;
import com.example.guidemaps.Models.UserMap;
import com.example.guidemaps.User.PostPlaces;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.List;

public class MapsAdapter extends FirebaseRecyclerAdapter<UserMap, MapsAdapter.myViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public MapsAdapter(@NonNull FirebaseRecyclerOptions<UserMap> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull UserMap model) {

    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    class myViewHolder extends RecyclerView.ViewHolder {

        TextView titulo;
        ImageView murl;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);


        }
    }

}
