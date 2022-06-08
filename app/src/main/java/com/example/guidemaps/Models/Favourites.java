package com.example.guidemaps.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Favourites implements Serializable {

    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("lugaresFavoritos")
    private List<Place> lugaresFavoritos = new ArrayList<>();

    public Favourites(String id) {
        this.id = id;
    }

    public Favourites(String id, List<Place> lugaresFavoritos) {
        this.id = id;
        this.lugaresFavoritos = lugaresFavoritos;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Place> getFavouritePlaces() {
        return lugaresFavoritos;
    }

    public void setFavouritePlaces(List<Place> lugaresFavoritos) {
        this.lugaresFavoritos = lugaresFavoritos;
    }

    public void addPlace(Place place) {
        this.lugaresFavoritos.add(place);
    }

}
