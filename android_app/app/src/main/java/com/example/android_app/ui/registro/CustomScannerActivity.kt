package com.example.android_app.ui.registro

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.android_app.R
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class CustomScannerActivity : AppCompatActivity() {

    private lateinit var capture: CaptureManager
    private lateinit var barcodeScannerView: DecoratedBarcodeView
    private var isTorchOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_scanner)

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner)

        capture = CaptureManager(this, barcodeScannerView)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.setShowMissingCameraPermissionDialog(false)
        capture.decode()

        findViewById<ImageButton>(R.id.btn_cerrar_escaner).setOnClickListener {
            finish()
        }

        val btnFlash = findViewById<ImageButton>(R.id.btn_flash)
        btnFlash.setOnClickListener {
            if (isTorchOn) {
                barcodeScannerView.setTorchOff()
                isTorchOn = false
                btnFlash.setImageResource(android.R.drawable.ic_menu_compass) // Icono default off
            } else {
                barcodeScannerView.setTorchOn()
                isTorchOn = true
                btnFlash.setImageResource(android.R.drawable.ic_menu_always_landscape_portrait) // Icono default on
            }
        }

        findViewById<Button>(R.id.btn_manual_search_escaner).setOnClickListener {
            // Retorna un resultado específico para forzar la búsqueda manual si se desea
            val intent = Intent()
            intent.putExtra("MANUAL_SEARCH", true)
            setResult(RESULT_CANCELED, intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }
}
