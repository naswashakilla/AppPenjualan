package com.shakilla.penjualan

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        ambilDataProfil()

        // Tombol Back
        findViewById<android.widget.ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Tombol Logout
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Keluar Akun")
                .setMessage("Apakah kamu yakin ingin keluar?")
                .setPositiveButton("Ya, Keluar") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun ambilDataProfil() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("users")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val nama = snapshot.child("nama").getValue(String::class.java) ?: "-"
                    val email = snapshot.child("email").getValue(String::class.java) ?: "-"
                    val telp = snapshot.child("telp").getValue(String::class.java) ?: "-"

                    findViewById<TextView>(R.id.tvNamaProfil).text = nama
                    findViewById<TextView>(R.id.tvNamaHeader).text = nama
                    findViewById<TextView>(R.id.tvEmailHeader).text = email
                    findViewById<TextView>(R.id.tvEmailProfil).text = email
                    findViewById<TextView>(R.id.tvTelpProfil).text = telp

                }
            }
    }
}