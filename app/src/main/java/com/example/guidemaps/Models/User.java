package com.example.guidemaps.Models;

import android.net.Uri;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {

    @Expose
    @SerializedName("idUsuario")
    private String idUsuario ;

    @Expose
    @SerializedName("nombre")
    private String nombre ;

    @Expose
    @SerializedName("nickname")
    private String nickname ;

    @Expose
    @SerializedName("email")
    private String email ;

    @Expose
    @SerializedName("photo")
    private Uri urlProfilePhoto;

    public User(String idUsuario, String nombre, String nickname, String email, Uri urlProfilePhoto) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.nickname = nickname;
        this.email = email;
        this.urlProfilePhoto = urlProfilePhoto;
    }

    public User() {
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String apellidos) {
        this.nickname = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Uri getUrlProfilePhoto() {
        return urlProfilePhoto;
    }

    public void setUrlProfilePhoto(Uri urlProfilePhoto) {
        this.urlProfilePhoto = urlProfilePhoto;
    }

}
