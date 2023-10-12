package com.example.taller_2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.MapStyleOptions
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import kotlin.math.*
import java.text.SimpleDateFormat
import java.util.Date

class Maps : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentLocation: LatLng
    private var lastLocation: LatLng? = null
    private val MIN_DISTANCE_METERS = 30.0
    private val JSON_FILE_NAME = "locations.json"
    private val locationList = JSONArray()
    private var lightSensor: Sensor? = null
    private var sensorManager: SensorManager? = null
    private val LIGHT_LIMIT = 1500.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION
        if (ContextCompat.checkSelfPermission(this, fineLocationPermission) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, coarseLocationPermission) != PackageManager.PERMISSION_GRANTED) {

            // Mostrar explicación si es necesario (esto es opcional)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, fineLocationPermission) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this, coarseLocationPermission)) {
                // Puedes mostrar una explicación aquí si lo deseas.
            }

            // Solicitar permisos de ubicación
            ActivityCompat.requestPermissions(this, arrayOf(fineLocationPermission, coarseLocationPermission), LOCALIZACION)
        } else {

        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar el callback de ubicación
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onLocationChanged(locationResult.lastLocation)
            }
        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
    }
    override fun onStart() {
        super.onStart()
        // Registrar el escuchador del sensor de luz
        sensorManager?.registerListener(lightSensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onStop() {
        super.onStop()
        // Detener el escuchador del sensor de luz
        sensorManager?.unregisterListener(lightSensorEventListener)
    }
    private val lightSensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(sensorEvent: SensorEvent) {
            if (mMap != null) {
                if (sensorEvent.values[0] < LIGHT_LIMIT) {
                    // Cambia el estilo del mapa a estilo nocturno
                    mMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@Maps, R.raw.map_night_style))
                } else {
                    // Cambia el estilo del mapa a estilo de día
                    mMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@Maps, R.raw.map_day_style))
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // No se necesita una implementación aquí
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCALIZACION) {
            var fineLocationGranted = false
            var coarseLocationGranted = false
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]
                if (permission == Manifest.permission.ACCESS_FINE_LOCATION) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        fineLocationGranted = true
                    }
                } else if (permission == Manifest.permission.ACCESS_COARSE_LOCATION) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        coarseLocationGranted = true
                    }
                }
            }

            if (fineLocationGranted && coarseLocationGranted) {
                Toast.makeText(this, "PERMISOS DE LOCALIZACIÓN DADOS", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "ALGUNO DE LOS PERMISOS DE LOCALIZACIÓN NO FUE DADO", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableLocation()
    }

    private fun enableLocation() {
        // Verificar y solicitar permisos de ubicación si es necesario

        // Configurar la solicitud de ubicación
        val locationRequest = LocationRequest.create()
            .setInterval(10000) // Intervalo de actualización de ubicación en milisegundos
            .setFastestInterval(5000) // Intervalo más rápido en milisegundos
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        // Comenzar la actualización de ubicación
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun onLocationChanged(location: android.location.Location?) {
        location?.let {
            // Convertir la ubicación a LatLng
            currentLocation = LatLng(it.latitude, it.longitude)

            // Agregar un marcador en la ubicación actual
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))

            // Mover la cámara al nuevo marcador
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))

            // Verificar si se ha movido más de la distancia mínima
            if (lastLocation == null || calculateDistance(currentLocation, lastLocation!!) >= MIN_DISTANCE_METERS) {
                // Registrar la nueva ubicación en JSON
                lastLocation = currentLocation
                recordLocation(it)
            }
        }
    }


    private fun recordLocation(location: android.location.Location) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentDate = dateFormat.format(Date())

        val locationData = JSONObject()
        locationData.put("latitude", location.latitude)
        locationData.put("longitude", location.longitude)
        locationData.put("timestamp", currentDate)

        locationList.put(locationData)

        // Guardar el JSON en la memoria interna del dispositivo
        val file = File(filesDir, JSON_FILE_NAME)
        file.writeText(locationList.toString(4))

        // Notificar al usuario que se ha registrado una nueva ubicación
        // Esto puedes personalizarlo según tus necesidades
        showToast("New location recorded at $currentDate")
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@Maps, message, Toast.LENGTH_SHORT).show()
        }
    }
    private fun calculateDistance(latlng1: LatLng, latlng2: LatLng): Double {
        val lat1 = latlng1.latitude
        val lon1 = latlng1.longitude
        val lat2 = latlng2.latitude
        val lon2 = latlng2.longitude

        val radius = 6371 // Radio de la Tierra en kilómetros

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return radius * c
    }
    companion object {
        const val LOCALIZACION = 1
    }
}