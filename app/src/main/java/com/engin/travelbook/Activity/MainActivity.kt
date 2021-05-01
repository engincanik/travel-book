package com.engin.travelbook.Activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.engin.travelbook.Adapter.RecyclerAdapter
import com.engin.travelbook.Model.Place
import com.engin.travelbook.R

class MainActivity : AppCompatActivity() {
    private var places = mutableListOf<Place>()
    private lateinit var recyclerView: RecyclerView
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_place, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_place_option) {
            val intent = Intent(applicationContext, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.rv_recyclerView)

        try {
            val database = openOrCreateDatabase("Places", Context.MODE_PRIVATE, null)
            val cursor = database.rawQuery("SELECT * FROM places", null)

            val addressIndex = cursor.getColumnIndex("address")
            val latitudeIndex = cursor.getColumnIndex("latitude")
            val longitudeIndex = cursor.getColumnIndex("longitude")

            while (cursor.moveToNext()) {
                val addressFromDatabase = cursor.getString(addressIndex)
                val latitudeFromDatabase = cursor.getDouble(latitudeIndex)
                val longitudeFromDatabase = cursor.getDouble(longitudeIndex)

                val myPlace =
                    Place(addressFromDatabase, latitudeFromDatabase, longitudeFromDatabase)
                addToList(myPlace)
            }
            cursor.close()
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = RecyclerAdapter(places)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addToList(place: Place) {
        places.add(place)
    }

}