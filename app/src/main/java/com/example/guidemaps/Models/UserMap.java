package com.example.guidemaps.Models;

import java.util.List;

public class UserMap {

    String titulo, murl;
    List<Place> lugares;

    public UserMap(String titulo, String murl, List<Place> lugares) {
        this.titulo = titulo;
        this.murl = murl;
        this.lugares = lugares;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMurl() {
        return murl;
    }

    public void setMurl(String murl) {
        this.murl = murl;
    }

    public List<Place> getLugares() {
        return lugares;
    }

    public void setLugares(List<Place> lugares) {
        this.lugares = lugares;
    }
}
