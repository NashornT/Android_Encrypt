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
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
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

    private var _interstitialAd: InterstitialAd? = null
    private var _bannerAd: AdView? = null
    private var _rewardAd: RewardedAd? = null
    private lateinit var singlePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    override fun onStart() {
        super.onStart()
        getLastLocation()

        MobileAds.initialize(this)
        loadInterstitial()
        loadRewardAd()
        loadBannerAd()

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


        findViewById<Button>(R.id.btn_result).setOnClickListener {

            val intent = Intent(this,ResultActivity::class.java)
            startActivity(intent)
        }


        findViewById<Button>(R.id.btn_save_file).setOnClickListener {
//            createFile()
            requestWritePermission()
            Toast.makeText(this,"Arquivo Salvo",Toast.LENGTH_SHORT).show()


        }

        findViewById<TextView>(R.id.sair).setOnClickListener{
            val intent = Intent(this,Login::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()

        }




    }

    private fun loadRewardAd() {
        var adRequest = AdRequest.Builder().build()

        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                _rewardAd = null
            }

            override fun onAdLoaded(p0: RewardedAd) {
                _rewardAd = p0
            }
        })
    }

    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                _interstitialAd = null;
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                _interstitialAd = interstitialAd
            }
        })
    }

    private fun loadBannerAd() {
        _bannerAd = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        _bannerAd?.loadAd(adRequest)
    }

    fun showInterstitial(view: View) {
        _interstitialAd?.show(this)
    }

    fun showReward(view: View) {
        _rewardAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                _rewardAd = null
                loadRewardAd()
            }
        }

        _rewardAd?.show(this, OnUserEarnedRewardListener { rewardItem ->
            var rewardAmount = rewardItem.amount
            var rewardType = rewardItem.type
            println("$rewardAmount $rewardType Awarded")
        })
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


        }

    }


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



}