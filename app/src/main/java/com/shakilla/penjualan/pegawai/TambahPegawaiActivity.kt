package com.shakilla.penjualan.pegawai

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.shakilla.penjualan.R

class TambahPegawaiActivity : AppCompatActivity() {

    private var fotoUri: Uri? = null
    private var fotoUrlLama: String = ""
    private var isEditMode = false
    private var idPegawai: String = ""

    private val dbRef = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("pegawai")
    private val storageRef = FirebaseStorage.getInstance().reference.child("foto_pegawai")

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            fotoUri = it
            Glide.with(this).load(it).into(findViewById(R.id.ivFotoForm))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_pegawai)

        // Cek mode edit
        isEditMode = intent.getStringExtra("mode") == "edit"
        idPegawai = intent.getStringExtra("id") ?: dbRef.push().key ?: ""

        val etNama = findViewById<TextInputEditText>(R.id.etNamaPegawai)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmailPegawai)
        val etTelp = findViewById<TextInputEditText>(R.id.etTelpPegawai)
        val etJabatan = findViewById<TextInputEditText>(R.id.etJabatanPegawai)
        val etGaji = findViewById<TextInputEditText>(R.id.etGajiPegawai)

        if (isEditMode) {
            // Isi form dengan data lama
            etNama.setText(intent.getStringExtra("nama"))
            etEmail.setText(intent.getStringExtra("email"))
            etTelp.setText(intent.getStringExtra("telp"))
            etJabatan.setText(intent.getStringExtra("jabatan"))
            etGaji.setText(intent.getStringExtra("gaji"))
            fotoUrlLama = intent.getStringExtra("fotoUrl") ?: ""

            if (fotoUrlLama.isNotEmpty()) {
                Glide.with(this).load(fotoUrlLama).into(findViewById<ShapeableImageView>(R.id.ivFotoForm))
            }
            findViewById<TextView>(R.id.tvJudulForm).text = "Edit Pegawai"
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<FloatingActionButton>(R.id.btnPilihFoto).setOnClickListener { pickImage.launch("image/*") }

        findViewById<MaterialButton>(R.id.btnSimpanPegawai).setOnClickListener {
            val nama = etNama.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val telp = etTelp.text.toString().trim()
            val jabatan = etJabatan.text.toString().trim()
            val gaji = etGaji.text.toString().trim()

            if (nama.isEmpty() || jabatan.isEmpty()) {
                Toast.makeText(this, "Nama dan Jabatan wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fotoUri != null) {
                // Upload foto dulu ke Firebase Storage
                val ref = storageRef.child("$idPegawai.jpg")
                ref.putFile(fotoUri!!).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        simpanKeDatabase(idPegawai, nama, email, telp, jabatan, gaji, url.toString())
                    }
                }
            } else {
                simpanKeDatabase(idPegawai, nama, email, telp, jabatan, gaji, fotoUrlLama)
            }
        }
    }

    private fun simpanKeDatabase(id: String, nama: String, email: String, telp: String,
                                 jabatan: String, gaji: String, fotoUrl: String) {
        val data = mapOf(
            "id" to id, "nama" to nama, "email" to email,
            "telp" to telp, "jabatan" to jabatan,
            "gaji" to gaji, "fotoUrl" to fotoUrl
        )
        dbRef.child(id).setValue(data)
            .addOnSuccessListener {
                val pesan = if (isEditMode) "Pegawai berhasil diupdate!" else "Pegawai berhasil ditambahkan!"
                Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}