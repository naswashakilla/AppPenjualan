package com.shakilla.penjualan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val dbRef = FirebaseDatabase.getInstance("https://penjualan-595b9f54-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val etNama = findViewById<TextInputEditText>(R.id.etNamaDaftar)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmailDaftar)
        val etTelp = findViewById<TextInputEditText>(R.id.etTelpDaftar)
        val etPass = findViewById<TextInputEditText>(R.id.etPassDaftar)
        val btnDaftar = findViewById<MaterialButton>(R.id.btnSimpanDaftar)

        // Di dalam RegisterActivity.kt
        btnDaftar.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val telp = etTelp.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (nama.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        val userMap = mapOf("nama" to nama, "email" to email, "telp" to telp)

                        uid?.let { id ->
                            // Pastikan URL Database sesuai dengan milikmu
                            dbRef.child(id).setValue(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()

                                    // PINDAH KE MAIN ACTIVITY
                                    val intent = Intent(this, MainActivity::class.java)
                                    // Flag ini agar user tidak bisa klik 'Back' kembali ke Register
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Gagal simpan data: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Gagal Daftar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}