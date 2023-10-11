package com.example.taller_2

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val con:ImageView=findViewById(R.id.contactos)
        val cam:ImageView=findViewById(R.id.camara)
        val map:ImageView=findViewById(R.id.map)
        val originalBitmap = BitmapFactory.decodeResource(resources, R.drawable.osmap)
        val width = 200 // Ancho deseado
        val height = 200 // Alto deseado
        val redimensionadaBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true)
        map.setImageBitmap(redimensionadaBitmap)
        con.setOnClickListener {
            var intent= Intent(this,Contactos::class.java)
            startActivity(intent)
        }
        cam.setOnClickListener {
            var intent= Intent(this,Camara::class.java)
            startActivity(intent)
        }
        map.setOnClickListener {
            var intent= Intent(this,Maps::class.java)
            startActivity(intent)
        }
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSIONS)
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSIONS) {
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "PERMISO $permission DADO", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "PERMISO $permission NO DADO", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        const val STORAGE_PERMISSIONS = 2
    }

}