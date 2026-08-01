package com.codex.quota.android

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.codex.quota.android.protocol.PairingQrPayloadPolicy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

class PairingQrScannerActivity : ComponentActivity() {
  private lateinit var previewView: PreviewView
  private lateinit var statusView: TextView
  private var cameraController: LifecycleCameraController? = null
  private var barcodeScanner: BarcodeScanner? = null
  private var resultDelivered = false

  private val cameraPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      if (granted) {
        startScanner()
      } else {
        Toast.makeText(this, R.string.pairing_scanner_permission_required, Toast.LENGTH_LONG).show()
        finish()
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(createScannerView())

    if (
      ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
    ) {
      startScanner()
    } else {
      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  override fun onDestroy() {
    cameraController?.clearImageAnalysisAnalyzer()
    previewView.controller = null
    barcodeScanner?.close()
    super.onDestroy()
  }

  private fun createScannerView(): FrameLayout {
    val root = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
    previewView =
      PreviewView(this).apply {
        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        scaleType = PreviewView.ScaleType.FILL_CENTER
      }
    root.addView(
      previewView,
      FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT,
      ),
    )

    statusView =
      TextView(this).apply {
        setText(R.string.pairing_scanner_instruction)
        setTextColor(Color.WHITE)
        textSize = 17f
        gravity = Gravity.CENTER
        setPadding(24.dp, 16.dp, 24.dp, 16.dp)
        background =
          GradientDrawable().apply {
            setColor(0xCC111827.toInt())
            cornerRadius = 16.dp.toFloat()
          }
      }
    root.addView(
      statusView,
      FrameLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        .apply {
          gravity = Gravity.TOP
          setMargins(24.dp, 48.dp, 24.dp, 0)
        },
    )

    val cancel =
      TextView(this).apply {
        setText(R.string.pairing_scanner_cancel)
        setTextColor(Color.WHITE)
        textSize = 17f
        gravity = Gravity.CENTER
        setPadding(28.dp, 14.dp, 28.dp, 14.dp)
        background =
          GradientDrawable().apply {
            setColor(0xCC111827.toInt())
            cornerRadius = 24.dp.toFloat()
          }
        setOnClickListener { finish() }
      }
    root.addView(
      cancel,
      FrameLayout.LayoutParams(
          ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        .apply {
          gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
          bottomMargin = 48.dp
        },
    )
    return root
  }

  private fun startScanner() {
    if (cameraController != null || isFinishing) return

    runCatching {
        val mainExecutor = ContextCompat.getMainExecutor(this)
        val scanner =
          BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build(),
          )
        barcodeScanner = scanner
        val controller =
          LifecycleCameraController(this).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            setImageAnalysisAnalyzer(
              mainExecutor,
              MlKitAnalyzer(
                listOf(scanner),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                mainExecutor,
              ) { result ->
                val barcodes = result?.getValue(scanner).orEmpty()
                val pairingLink =
                  barcodes.firstNotNullOfOrNull { barcode ->
                    PairingQrPayloadPolicy.accept(barcode.rawValue)
                  }
                when {
                  pairingLink != null -> deliverResult(pairingLink)
                  barcodes.isNotEmpty() -> statusView.setText(R.string.pairing_scanner_wrong_code)
                }
              },
            )
            bindToLifecycle(this@PairingQrScannerActivity)
          }
        cameraController = controller
        previewView.controller = controller
      }
      .onFailure { error ->
        Log.e(TAG, "Unable to start pairing QR scanner", error)
        barcodeScanner?.close()
        barcodeScanner = null
        Toast.makeText(this, R.string.pairing_scanner_camera_error, Toast.LENGTH_LONG).show()
        finish()
      }
  }

  private fun deliverResult(pairingLink: String) {
    if (resultDelivered) return
    resultDelivered = true
    setResult(Activity.RESULT_OK, Intent().setData(pairingLink.toUri()))
    finish()
  }

  private val Int.dp: Int
    get() = (this * resources.displayMetrics.density).toInt()

  private companion object {
    const val TAG = "PairingQrScanner"
  }
}
