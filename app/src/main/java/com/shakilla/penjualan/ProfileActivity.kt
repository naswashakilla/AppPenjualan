package com.shakilla.penjualan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivAvatar: ImageView
    private val uid = FirebaseAuth.getInstance().currentUser?.uid
    private val storageRef = FirebaseStorage.getInstance().reference.child("foto_profil")
    private val dbRef = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("users")

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadFoto(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        ivAvatar = findViewById(R.id.ivAvatarProfile)
        val btnUbahFoto = findViewById<FloatingActionButton>(R.id.btnUbahFotoProfil)

        ambilDataProfil()

        // Tombol Back
        findViewById<android.widget.ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnUbahFoto.setOnClickListener {
            pickImage.launch("image/*")
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

    private fun uploadFoto(uri: Uri) {
        if (uid == null) return
        
        Toast.makeText(this, "Mengupload foto...", Toast.LENGTH_SHORT).show()
        val ref = storageRef.child("$uid.jpg")
        ref.putFile(uri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { downloadUri ->
                dbRef.child(uid).child("fotoUrl").setValue(downloadUri.toString())
                    .addOnSuccessListener {
                        Glide.with(this).load(downloadUri).into(ivAvatar)
                        Toast.makeText(this, "Foto profil diperbarui!", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal upload: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ambilDataProfil() {
        if (uid == null) return

        dbRef.child(uid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val nama = snapshot.child("nama").getValue(String::class.java) ?: "-"
                    val email = snapshot.child("email").getValue(String::class.java) ?: "-"
                    val telp = snapshot.child("telp").getValue(String::class.java) ?: "-"
                    val fotoUrl = snapshot.child("fotoUrl").getValue(String::class.java)

                    findViewById<TextView>(R.id.tvNamaProfil).text = nama
                    findViewById<TextView>(R.id.tvNamaHeader).text = nama
                    findViewById<TextView>(R.id.tvEmailHeader).text = email
                    findViewById<TextView>(R.id.tvEmailProfil).text = email
                    findViewById<TextView>(R.id.tvTelpProfil).text = telp

                    if (!fotoUrl.isNullOrEmpty()) {
                        Glide.with(this).load(fotoUrl).into(ivAvatar)
                    }
                }
            }
    }
}
