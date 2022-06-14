package com.example.guidemaps.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.guidemaps.Models.Place;
import com.example.guidemaps.R;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceHolder> {

    private int layout;
    private static Context context;
    private List<Place> places = null;
    private OnItemClickListener listener;
    private int position;

    public PlaceAdapter(int layout, Context context, List<Place> places, OnItemClickListener listener) {
        this.layout = layout;
        this.context = context;
        this.places = places;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaceAdapter.PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(this.layout, null);

        PlaceHolder placeHolder = new PlaceHolder(view);

        return placeHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceAdapter.PlaceHolder holder, int position) {
        holder.bindItem(this.places.get(position), this.listener);
    }

    @Override
    public int getItemCount() {
        return this.places.size();
    }

    public int getPosition() {
        return position;
    }

    public class PlaceHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;
        public Place place;

        public PlaceHolder(@NonNull View itemView) {
            super(itemView);
            //itemView.setOnCreateContextMenuListener(this);

            textView = itemView.findViewById(R.id.nombreLugar);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    position = getBindingAdapterPosition();
                    return false;
                }
            });
            imageView = itemView.findViewById(R.id.imagenLugar);
        }

        public void bindItem(final Place place, final OnItemClickListener listener) {
            textView.setText(place.getNombre());

            Glide.with(context.getApplicationContext()).load(place.getImagen()).into(imageView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(place, getBindingAdapterPosition());
                }
            });
        }

    }

    /**
     * Implementamos una interface que nos permitirá definir el evento que responderá
     * a una pulsación del usuario sobre uno de los ítems del RecyclerView. El evento
     * recibirá el ítem sobre el que hemos pulsado y la posición de éste en el adapta-
     * dor.
     */
    public interface OnItemClickListener {
        void onItemClick(Place place, int position) ;
    }

}
