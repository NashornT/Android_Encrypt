package com.example.permission_app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.location.LocationListener
import android.net.Uri
import android.nfc.Tag
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import java.nio.charset.StandardCharsets
import java.util.jar.Manifest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.example.permission_app.Auth.Login
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.*


class MainActivity : AppCompatActivity() {

    val CREATE_FILE = 0
    var lat: Double = 0.0
    var long: Double = 0.0
    val date = getCurrentDateTime()
    val dateInString = date.toString("yyyy/MM/dd  HH:mm:ss")
    val onlydate = date.toString("yyyy_MM_dd")
    val TAG = "ReadWrite"
    var nomeArquivo = ""
    val TAGB = "Arquivos"





    private lateinit var singlePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    override fun onStart() {
        super.onStart()
        getLastLocation()






    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_save_file).isEnabled = true
        findViewById<Button>(R.id.btn_result).isEnabled = true

        setupCheckPermissions()


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<EditText>(R.id.txt_titulo).addTextChangedListener{

            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),101)

            }

        }


//        findViewById<Button>(R.id.btn_get_location).setOnClickListener {
//
//            getLastLocation()
//            findViewById<Button>(R.id.btn_save_loc).isEnabled = true
//            findViewById<Button>(R.id.btn_save_file).isEnabled = true
//        }

//        findViewById<Button>(R.id.btn_save_loc).setOnClickListener {
//
//            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED
//            ){
//                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),101)
//
//            }
//
//            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
//                addCategory(Intent.CATEGORY_OPENABLE)
//                type = "text/plain"
//                putExtra(Intent.EXTRA_TITLE, "${dateInString}")
//                putExtra(DocumentsContract.EXTRA_INITIAL_URI,"")
//
//            }
//            startActivityForResult(intent,CREATE_FILE)
//            findViewById<Button>(R.id.btn_result).isEnabled = true
//        }

        findViewById<Button>(R.id.btn_result).setOnClickListener {

            val intent = Intent(this,ResultActivity::class.java)
            startActivity(intent)
        }


        findViewById<Button>(R.id.btn_save_file).setOnClickListener {
//            createFile()
            requestWritePermission()
            findViewById<Button>(R.id.btn_result).isEnabled = true


        }

        findViewById<TextView>(R.id.sair).setOnClickListener{
            val intent = Intent(this,Login::class.java)
            startActivity(intent)
        }
        



    }



//    fun deleteIfExists(nomeArquivo: String){
//
//        val arquivoTexto = File(filesDir, "${(nomeArquivo + onlydate)}.txt")
//        val arquivoFoto = File(filesDir, "${(nomeArquivo + onlydate)}.fig")
//
//        val txtExists = arquivoTexto.exists()
//        val figExists = arquivoFoto.exists()
//
//        if(txtExists){
//            arquivoTexto.delete()
//        }
//        if (figExists){
//            arquivoFoto.delete()
//        }
//
//
//    }



    fun isExternalStorageWritable(): Boolean {
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            Log.i(TAG, "Pode escrever no diretorio externo.")
            return true
        } else {
            Log.i(TAG, "Não pode escrever no diretorio externo.")
        }
        return false
    }


    private fun setupCheckPermissions() {
        // Lançador de requisição de uma permissão:
        singlePermissionLauncher = registerForActivityResult(
            ActivityResultContracts
                .RequestPermission() // Requer uma permissão
        ) { // Retorna verdadeiro se a permissão foi concedida
                isGranted: Boolean ->
            if (isGranted) {
                Log.i(TAG, "Granted")
            } else {
                Log.i(TAG, "Denied")
            }
        }
    }

    private fun onGravarClick() {
        getLastLocation()

        val nomeArquivo = findViewById<EditText>(R.id.txt_titulo).text.toString().trim()

        if(!nomeArquivo.isNullOrBlank()){


            writeToEncryptedFile(
                getEncryptedFile(
                    "${(nomeArquivo + onlydate)}.txt"
                ),
                "Localização:${lat},${long}\n ${findViewById<EditText>(R.id.txt_anotações).text.toString()}\n" +
                        "${(nomeArquivo + onlydate)}"
            )

//            if(foto!= null){
//                savePhotoToEncryptedFile(
//                    getEncryptedFile("${(nomeArquivo + onlydate)}.fig"),
//                    foto!!
//                )
//            }

        }

    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            if (data != null) {
//                foto = data.extras?.get("data") as Bitmap
//                setFotoToView(foto!!)
//            }
//
//        }
//    }

    fun writeToEncryptedFile(
        encryptedFile: EncryptedFile,
        fileContent: String
    ) {
        val encryptedOutputStream: FileOutputStream = encryptedFile.openFileOutput()

        // Escreve no arquivo encriptado
        encryptedOutputStream.apply {
            write(fileContent.toByteArray(StandardCharsets.UTF_8))
            close()
        }
    }

    fun getEncryptedFile(fileName: String): EncryptedFile {
        // Cria a especificação de criptografia para a chave mestra:
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC

        // Cria ou pega a chave mestra:
        val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        Log.i(TAG, "$mainKeyAlias")

//         Criar um arquivo seguro no diretório interno

        val encryptedFile = EncryptedFile.Builder(
            File(filesDir,fileName),
            applicationContext,
            mainKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()




        return encryptedFile
    }


    private fun requestWritePermission() {

        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
//                writeFile()
                onGravarClick()
            }
            else -> {
                singlePermissionLauncher.launch(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }


    // METODO  para salvar um Doc_FILE
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//
//        if(requestCode == CREATE_FILE && resultCode == RESULT_OK){
//            val url = data!!.data
//            try {
//                val outputStream = this.contentResolver.openOutputStream(url!!)
//                outputStream?.write("Latitude:${lat}\nLongitude:${long}".toByteArray())
//                outputStream?.close()
//            } catch (e:Exception){
//                print(e.localizedMessage)
//            }
//        }
//
//
//    }

    // METODO  para pegar a GeoLocalização
    private fun getLastLocation() {
        val task = fusedLocationProviderClient.lastLocation

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),101)
            return
        }
     task.addOnSuccessListener {
         if(it != null){
             lat = it.latitude
             long = it.longitude
//             Toast.makeText(applicationContext,"${it.latitude} ${it.longitude}",Toast.LENGTH_SHORT).show()
         }
     }


    }

    // METODO  para formatar
    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    // METODO  para pegar  a data
    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    // METODO #1 para escrever em arquivos
    fun writeFile() {
        if (isExternalStorageWritable()) {
            val endereco = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            nomeArquivo = date.toString("yyyy_MM_dd__HH_mm_ss")
            val file = File("$endereco/$nomeArquivo")
            Log.i(TAG, "Criando arquivo em")
            Log.i(TAG, "${file.absolutePath}")
            try {
                file.writeText("Latitude:${lat}\n" +
                        "Longitude:${long}")
            } catch (e: java.lang.Exception) {
                Log.i(TAG, e.message!!)
            }
        } else {
            Toast.makeText(
                this,
                "Não foi possível escrever no disco",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    // METODO #2 para escrever em arquivos
    private fun createFile(){
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ){

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),101)
            return
        }

        val path = this.getExternalFilesDir(null)
        val folder = File(path,"Android_Writer")
        folder.mkdirs()
        val file = File(folder,"${date.toString("yyyy_MM_dd__HH_mm_ss")}.crd")
        file.appendText("Data:${onlydate}\nLatitude:${lat}\nLongitude:${long}\n")
        Toast.makeText(this,"Arquivo Salvo",Toast.LENGTH_SHORT).show()



    }

    // METODO #3 para escrever em arquivos
    private fun createDeleteFile() {
        val file = File(getExternalFilesDir(null), "tp1.txt")
        val os: OutputStream = FileOutputStream(file)
        os.write("Data:${onlydate}\nLatitude:${lat}\nLongitude:${long}".toByteArray())
        os.close()



    }

}