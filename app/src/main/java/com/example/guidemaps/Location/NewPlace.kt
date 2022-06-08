package com.example.guidemaps.Location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.Debug.getLocation
import com.example.guidemaps.Models.Place
import com.example.guidemaps.R
import com.example.guidemaps.User.PostPlaces
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class NewPlace : AppCompatActivity() {

    private var lugar: Place? = null
    private var imagenUrl = ""
    private var cancelarBoton: Button? = null
    private var anyadirBoton: Button? = null
    private var imageView: ImageView? = null
    private var nombre: EditText? = null
    private lateinit var locationManager: LocationManager
    private var hasGPS = false
    private var hasNetwork = false
    private var locationGPS: Location? = null
    private var locationNetwork: Location? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_place)
        val intent = intent

        imageView = findViewById(R.id.imageView2)
        var imageView2 = imageView
        nombre = findViewById(R.id.editText)
        cancelarBoton = findViewById(R.id.cancelarFoto)
        anyadirBoton = findViewById(R.id.anyadirFotoBoton)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var bundle = intent.getBundleExtra("lugarBundle")
        var imagen: Bitmap = bundle?.get("data") as Bitmap
        imageView2!!.setImageBitmap(imagen)
        cancelarBoton?.setOnClickListener {
            super.finish()
        }
        anyadirBoton?.setOnClickListener {
            getLocation()
            subirAFirebase(imagen)
        }
        getLocation()
    }

    private fun addLugarToFB (lugar: Place) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("lugar")
        myRef.child(lugar.nombre).setValue(lugar)
    }

    private fun subirAFirebase(imageBitmap: Bitmap) {
        var firebaseStorage = FirebaseStorage.getInstance()
        var storageReference = firebaseStorage.reference.child("lugar/${nombre?.text.toString()}.jpg")

        var byteArrayOutputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        var datas = byteArrayOutputStream.toByteArray()
        var uploadTask = storageReference.putBytes(datas).addOnSuccessListener {
            imagenUrl = it.metadata!!.toString()
            if (locationNetwork != null) {
                lugar = Place(nombre!!.text.toString(), locationNetwork!!.latitude, locationNetwork!!.longitude, imagenUrl, "desc")
            } else if (locationGPS != null) {
                lugar = Place(nombre!!.text.toString(), locationGPS!!.latitude, locationGPS!!.longitude, imagenUrl, "desc")
            }
            if (lugar != null) {
                if (!PostPlaces.lugares.contains(lugar)) {
                    addLugarToFB(lugar!!)
                    PostPlaces.lugares.add(lugar)
                    PostPlaces.adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "${lugar!!.nombre} no se ha podido añadir, ya existe en la aplicación.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(applicationContext, "Error al obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
            super.finish()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGPS || hasNetwork) {
            if (hasGPS) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (location != null) {
                            locationGPS = location
                        }
                    }

                    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

                    }

                    override fun onProviderEnabled(provider: String) {

                    }

                    override fun onProviderDisabled(provider: String) {

                    }

                })
                val localGPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGPSLocation != null) {
                    locationGPS = localGPSLocation
                }
            }
            if (hasNetwork) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0F, object : LocationListener{
                    override fun onLocationChanged(location: Location) {
                        if (location != null) {
                            locationNetwork = location
                        }
                    }

                    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

                    }

                    override fun onProviderEnabled(provider: String) {

                    }

                    override fun onProviderDisabled(provider: String) {

                    }

                })
                val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null) {
                    locationNetwork = localNetworkLocation
                }
                if (locationGPS != null && locationNetwork != null) {
                    if (locationGPS!!.accuracy > locationNetwork!!.accuracy) {
                        println(locationGPS.toString())
                    } else {
                        println(locationNetwork.toString())
                    }
                }
            }
        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

}