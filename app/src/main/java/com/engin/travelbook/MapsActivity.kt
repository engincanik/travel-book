package com.engin.travelbook

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intentToMain = Intent(this, MainActivity::class.java)
        startActivity(intentToMain)
        finish()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(mOnMapLongClickListener)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = LocationListener { location ->
            val sharedPreferences =
                this@MapsActivity.getSharedPreferences("com.engin.travelbook", Context.MODE_PRIVATE)
            val firstTimeCheck = sharedPreferences.getBoolean("firstTime", true)
            if (firstTimeCheck) {
                mMap.clear()
                val newUserLocation = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newUserLocation, 15f))
                sharedPreferences.edit().putBoolean("firstTime", false).apply()
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 2, 2f, locationListener
            )
            val intent = intent
            var place = intent.getSerializableExtra("selectedPlace")
            if (place != null) {
                place = place as Place
                mMap.clear()
                val selectedLocation = LatLng(place.latitude!!, place.longitude!!)
                mMap.addMarker(MarkerOptions().position(selectedLocation).title(place.address))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15f))
            } else {
                val lastLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastLocationLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocationLatLng, 15f))
                }
            }
        }
    }

    private val mOnMapLongClickListener = GoogleMap.OnMapLongClickListener { latLng ->
        val geocoder = Geocoder(this, Locale.getDefault())
        var address = ""
        if (latLng != null) {
            try {

                val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addressList != null && addressList.isNotEmpty()) {
                    if (addressList[0].thoroughfare != null) {
                        address += "${addressList[0].thoroughfare} "
                        if (addressList[0].subThoroughfare != null) {
                            address += addressList[0].subThoroughfare
                        }
                    }
                } else {
                    address = "New Place"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title(address))

            val newPlace = Place(address, latLng.latitude, latLng.longitude)

            val dialog = AlertDialog.Builder(this)
            dialog.setCancelable(false)
            dialog.setTitle("Are you sure?")
            dialog.setMessage(newPlace.address)
            dialog.setPositiveButton("Yes") { dialog, which ->
                try {
                    val database = openOrCreateDatabase("Places", Context.MODE_PRIVATE, null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS places (address VARCHAR, latitude DOUBLE, longitude DOUBLE)")
                    val toCompile =
                        "INSERT INTO places (address, latitude, longitude) VALUES (?, ?, ?)"
                    val sqLiteStatement = database.compileStatement(toCompile)
                    sqLiteStatement.bindString(1, newPlace.address)
                    sqLiteStatement.bindDouble(2, newPlace.latitude!!)
                    sqLiteStatement.bindDouble(3, newPlace.longitude!!)
                    sqLiteStatement.execute()
                    Toast.makeText(this, "New place has been added.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.setNegativeButton("No") { dialog, which ->
                Toast.makeText(this, "Canceled!", Toast.LENGTH_LONG).show()
            }
            dialog.show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty()) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 2, 2f, locationListener
                    )
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}