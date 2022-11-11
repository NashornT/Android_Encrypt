package com.example.permission_app.Auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.permission_app.R
import com.google.firebase.auth.FirebaseAuth

class Cadastro : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        val btn = findViewById<Button>(R.id.btn_cadastro_salvar)
        val txt_btn = findViewById<TextView>(R.id.txt_ja_cadastro)

        firebaseAuth = FirebaseAuth.getInstance()
        txt_btn.setOnClickListener{
            val intent = Intent(this, Login::class.java )
            startActivity(intent)
        }

        btn.setOnClickListener {
            val email = findViewById<EditText>(R.id.txt_cadastrar_email).text.toString()
            val senha = findViewById<EditText>(R.id.txt_cadastrar_senha).text.toString()
            val confirma = findViewById<EditText>(R.id.txt_cadastar_confirma).text.toString()

            if( email.isNotEmpty() && senha.isNotEmpty() && confirma.isNotEmpty()){

                if(senha == confirma){
                    firebaseAuth.createUserWithEmailAndPassword(email,senha).addOnSuccessListener {
                        //Todo() pode dar erro aqui

                        val intent = Intent(this, Login::class.java )
                        startActivity(intent)

                    }
                }else{
                    Toast.makeText(this,"As senhas precisam ser iguais", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Campos Vazios não são permitidos", Toast.LENGTH_SHORT).show()
            }

        }

    }
}