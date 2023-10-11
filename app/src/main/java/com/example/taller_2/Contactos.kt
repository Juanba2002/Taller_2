package com.example.taller_2

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.database.Cursor
import android.widget.ListView
import android.provider.ContactsContract


class Contactos : AppCompatActivity() {
    private lateinit var contactListView: ListView
    private lateinit var mProjection: Array<String>
    private var mCursor: Cursor? = null
    private var mContactosAdapter: ContactsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactos)
        contactListView = findViewById(R.id.ListContactos)
        mProjection = arrayOf(ContactsContract.Profile._ID, ContactsContract.Profile.DISPLAY_NAME_PRIMARY)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                // Puedes mostrar un mensaje explicativo aquí si lo deseas.
            }
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), CONTACTOS)
        } else {
            mContactosAdapter = ContactsAdapter(this, null, 0)
            contactListView.adapter = mContactosAdapter
            initView()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTOS) {
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]
                if (permission == Manifest.permission.READ_CONTACTS) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "PERMISO DE CONTACTOS DADO", Toast.LENGTH_SHORT).show()
                        initView()
                    } else {
                        Toast.makeText(this, "PERMISO DE CONTACTOS NO DADO", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun initView() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            mCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, mProjection, null, null, null)
            mContactosAdapter?.changeCursor(mCursor)
        }
    }

    companion object {
        const val CONTACTOS = 1
    }
}