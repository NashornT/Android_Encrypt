package com.example.permission_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class ResultActivity : AppCompatActivity() {

    val date = getCurrentDateTime()
    val stringFiles: MutableList<Any> = mutableListOf()
    val TAG = "ReadWrite"
    var nomeArquivo = ""
    val onlydate = date.toString("yyyy_MM_dd")
    val REQUEST_IMAGE_CAPTURE = 1

    private lateinit var singlePermissionLauncher: ActivityResultLauncher<String>

    override fun onStart() {
        super.onStart()
        requestReadPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        findViewById<Button>(R.id.btn_ver).setOnClickListener {
            onLerClick()
        }

        findViewById<FloatingActionButton>(R.id.flt_btn).setOnClickListener{
            dispatchTakePictureIntent()
        }
        findViewById<Button>(R.id.btn_salvar_foto).setOnClickListener {
            onGravarClick()
            Toast.makeText(this,"foto salva",Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btn_foto_ver).setOnClickListener{
            onGetFoto()
        }
        //listFiles()




    }
    var foto: Bitmap? = null

    private fun onLerClick() {

        val nomeArquivo = findViewById<EditText>(R.id.txt_nome).text.toString().trim()


            if (!nomeArquivo.isNullOrBlank()) {
                val texto = readFromEncryptedFile(
                    getEncryptedFile(
                        "${(nomeArquivo + onlydate)}.txt"
                    )
                )
                findViewById<TextView>(R.id.txt_result).setText(texto)


            }


    }

    private  fun onGetFoto(){
        val nomeArquivo = findViewById<EditText>(R.id.txt_foto).text.toString().trim()

        foto = getPhotoFromEncryptedFile(
            getEncryptedFile("${(nomeArquivo + onlydate)}.fig")
        )

        foto?.let { setFotoToView(it) }
    }

    private fun onGravarClick() {

        val nomeArquivo = findViewById<EditText>(R.id.txt_foto).text.toString().trim()

        if(!nomeArquivo.isNullOrBlank()){
            if(foto!= null){
                savePhotoToEncryptedFile(
                    getEncryptedFile("${(nomeArquivo + onlydate)}.fig"),
                    foto!!
                )
            }

        }

    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            //takePictureIntent.resolveActivity(packageManager)?.also {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            //}
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null) {
                foto = data.extras?.get("data") as Bitmap
                setFotoToView(foto!!)
            }

        }
    }

    fun savePhotoToEncryptedFile(
        encryptedFile: EncryptedFile,
        fileContent: Bitmap
    ) {
        val encryptedOutputStream: FileOutputStream = encryptedFile.openFileOutput()

        val stream = ByteArrayOutputStream()
        fileContent.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val imageByteArray = stream.toByteArray()

        encryptedOutputStream.apply {
            write(imageByteArray)
            close()
        }
    }

    fun setFotoToView(foto: Bitmap) {
        findViewById<ImageView>(R.id.img_result).setImageBitmap(foto)
    }

//    fun verifyFileExists(nomeArquivo: String): Boolean {
//        val arquivoTexto = File(filesDir, "${(nomeArquivo + onlydate)}.txt")
//        val arquivoFoto = File(filesDir, "${(nomeArquivo + onlydate)}.fig")
//
//        val txtExists = arquivoTexto.exists()
//        val figExists = arquivoFoto.exists()
//
//        if (!txtExists) {
//            toast("${(nomeArquivo + onlydate)}.txt não existe")
//        }
//        if (!figExists) {
//            toast("${(nomeArquivo + onlydate)}.fig não existe")
//        }
//
//        return (txtExists && figExists)
//    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun readFromEncryptedFile(encryptedFile: EncryptedFile): String {
        val encryptedInputStream: FileInputStream = encryptedFile.openFileInput()
        encryptedInputStream.apply {
            val bytes = readBytes()
            val resposta = bytes.decodeToString()
            Log.i(TAG, resposta)
            return resposta
        }
    }
    fun getEncryptedFile(fileName: String): EncryptedFile {
        // Cria a especificação de criptografia para a chave mestra:
        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC

        // Cria ou pega a chave mestra:
        val mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        Log.i(TAG, "$mainKeyAlias")

        // Criar um arquivo seguro no diretório interno

        val encryptedFile = EncryptedFile.Builder(
            File(filesDir,fileName),
            applicationContext,
            mainKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        return encryptedFile
    }

    fun getPhotoFromEncryptedFile(
        encryptedFile: EncryptedFile
    ): Bitmap? {


        val encryptedInputStream: FileInputStream = encryptedFile.openFileInput()
        encryptedInputStream.apply {
            val bytes = readBytes()
            val arrayInputStream = ByteArrayInputStream(bytes as ByteArray?)
            val bitmap = BitmapFactory.decodeStream(arrayInputStream)
            return bitmap
        }
    }

    // METODO #1 para ler arquivos
    private fun readfile() {
        val file = File(getExternalFilesDir(null), "tp1.txt")
        if(!file.exists()) {
            Toast.makeText(this@ResultActivity,
                "Arquivo não encontrado",
                Toast.LENGTH_SHORT).show()
            return
        }
        val text = StringBuilder()
        try {
            val br = BufferedReader(FileReader(file))
            var line: String?
            while (br.readLine().also { line = it } != null) {
                text.append(line)
                text.append('\n')
            }
            br.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        findViewById<TextView>(R.id.txt_result).setText(text)
        Toast.makeText(this@ResultActivity,
            text.toString(),
            Toast.LENGTH_SHORT).show()
    }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    fun isExternalStorageReadable(): Boolean {
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
            ||
            Environment.MEDIA_MOUNTED_READ_ONLY == Environment.getExternalStorageState()
        ) {
            Log.i(TAG, "Pode ler do diretorio externo.")
            return true
        } else {
            Log.i(TAG, "Não pode ler do diretorio externo.")
        }
        return false
    }

    private fun requestReadPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                readFile()
            }
            else -> {
                singlePermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    // METODO #2 para ler arquivos
    fun readFile() {
        if (isExternalStorageReadable()) {
            val endereco =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            nomeArquivo = date.toString("yyyy_MM_dd__HH_mm_ss")

            val path = "$endereco/$nomeArquivo"
            Log.i(TAG, "Lendo do arquivo em")
            Log.i(TAG, "${path}")

            try {
                findViewById<TextView>(R.id.txt_result).text = File(path).readText()
            } catch (e: Exception) {
                Log.i(TAG, e.message!!)
            }
        } else {
            Toast.makeText(
                this,
                "Não foi possível ler do disco",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    // METODO #1 para listar arquivos
    fun listFiles() {
        if (isExternalStorageReadable()) {
            val endereco =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            var listaArquivos = ""
            for (f in endereco.listFiles()) {
                listaArquivos += "${f.name} \n"
            }
            try {
                val msg = "Lista de arquivos da pasta downloads no SD CARD:\n $listaArquivos"
                findViewById<TextView>(R.id.txt_result).text = msg
            } catch (e: Exception) {
                Log.i(TAG, e.message!!)
            }
        } else {
            Toast.makeText(
                this,
                "Não foi possível ler do disco",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    // METODO #2 para listar arquivos
    fun listfiles(){
                val path = this.getExternalFilesDir(null)
        val folder = File(path,"Android_Writer")
        for (file in folder.list()){
            Log.d("Files","$file")
            stringFiles.add(file)

        }
        Log.d("Files", " ${stringFiles}")
        findViewById<TextView>(R.id.txt_result).setText(stringFiles.toString())

    }

    // METODO #1 para abrir arquivos
    private fun openFile(){

        val path = this.getExternalFilesDir(null)
        val folder = File(path,"Android_Writer")
        folder.mkdirs()
        val file = File(folder,"writer_test.txt")

        if(file.exists()){
            val inputAsString = FileInputStream(file).bufferedReader().use { it.readText() }
            findViewById<TextView>(R.id.txt_result).setText(inputAsString)
        }else{
            findViewById<TextView>(R.id.txt_result).setText("Lista Vazia")
        }

    }
}