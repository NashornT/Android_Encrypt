package com.example.permission_app.Auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.permission_app.MainActivity
import com.example.permission_app.R
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btn = findViewById<Button>(R.id.btn_entrar)
        val txt_cadastar = findViewById<TextView>(R.id.txt_btn_cadastar)


        firebaseAuth = FirebaseAuth.getInstance()
        txt_cadastar.setOnClickListener{
            val intent = Intent(this,Cadastro::class.java)
            startActivity(intent)
        }

        btn.setOnClickListener {

            val email = findViewById<EditText>(R.id.txt_email).text.toString()
            val senha = findViewById<EditText>(R.id.txt_senha).text.toString()
            val path = email.split("@")[0]


            if( email.isNotEmpty() && senha.isNotEmpty()){

                firebaseAuth.signInWithEmailAndPassword(email,senha).addOnSuccessListener {
                    //Todo() pode dar erro aqui

                    val intent = Intent(this, MainActivity::class.java )
                    Log.d("value","$path")
                    intent.putExtra("User","${path}")
                    startActivity(intent)

                }

            }else{
                Toast.makeText(this,"Campos Vazios não são permitidos", Toast.LENGTH_SHORT).show()
            }

            Toast.makeText(this,"Entrando...", Toast.LENGTH_SHORT).show()

        }


    }
}