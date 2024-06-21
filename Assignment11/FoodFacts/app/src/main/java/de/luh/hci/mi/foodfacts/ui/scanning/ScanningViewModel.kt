// https://developers.google.com/ml-kit/vision/barcode-scanning/android

package de.luh.hci.mi.foodfacts.ui.scanning

import android.annotation.SuppressLint
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import de.luh.hci.mi.foodfacts.FoodFacts
import de.luh.hci.mi.foodfacts.data.Repository
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// ViewModel for the start screen.
class ScanningViewModel(
    app: FoodFacts,
    private val repository: Repository, // the underlying repository (data model)
) : ViewModel() {

    // Camera operations are executed on a separate thread (because they are expensive).
    // An executor executes Runnable tasks on a specific thread.
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // The barcode scanner engine.
    private val barcodeScanner: BarcodeScanner

    val cameraProviderFuture = ProcessCameraProvider.getInstance(app)

    var barcode by mutableStateOf("")
        private set

    init {
        log("init")
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E
            )
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)
    }

    fun bindPreview(
        cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
    ) {
        // The camera provider (one per Android process) binds the lifecycle of cameras
        // (open, started, stopped, closed) to the lifecycle of the activity. Also allows
        // us to specify "use cases" (e.g., preview, photo capture, image analysis).
        val cameraProvider = cameraProviderFuture.get()

        // Use case: preview (live viewfinder)
        val preview: Preview = Preview.Builder().build()
        // where to show the preview
        preview.setSurfaceProvider(previewView.surfaceProvider)

        // Use case: barcode scanning
        val ba = BarcodeAnalyzer()
        val barcodeAnalysis = ImageAnalysis.Builder()
            //.setTargetResolution(Size(480, 640))
            .setTargetResolution(Size(768, 1024))
            .build()
        barcodeAnalysis.setAnalyzer(cameraExecutor, ba)

        // Select the back camera
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            // Unbind existing use cases (if any), before rebinding
            cameraProvider.unbindAll()

            // Bind the specified use cases of the camera to the lifecycle of the activity
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, barcodeAnalysis
            )

        } catch (ex: Exception) {
            log("Use case binding failed: $ex")
        }
    }

    private inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {
        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image: InputImage =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                barcodeScanner.process(image)
                    .addOnSuccessListener { barcodes: List<Barcode> ->
                        for (bc: Barcode in barcodes) {
                            val value = bc.displayValue ?: "(null)"
                            val format = when (bc.format) {
                                Barcode.FORMAT_EAN_13 -> "FORMAT_EAN_13"
                                else -> "unknown format"
                            }
                            val valueType = when (bc.valueType) {
                                Barcode.TYPE_PRODUCT -> "product"
                                else -> "unknown value type"
                            }
                            log("barcode: $value $format, $valueType")
                            if (value.isNotEmpty() && bc.valueType == Barcode.TYPE_PRODUCT) {
                                barcode = value
                            }
                        }

                        // it is very important to close the image
                        // once this image is closed, the next one will be delivered
                        imageProxy.close()
                    }
                    .addOnFailureListener { ex: Exception ->
                        Log.d(javaClass.simpleName, "BarcodeScanner failed", ex)
                        // it is very important to close the image
                        // once this image is closed, the next one will be delivered
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }

    // Called when this ViewModel is no longer used and will be destroyed. Can be used for cleanup.
    override fun onCleared() {
        log("onCleared")
        barcodeScanner.close()
        cameraExecutor.shutdown()
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }

    companion object {
        // Companion object for creating the view model in the right lifecycle scope.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as FoodFacts
                val repository = app.repository
                ScanningViewModel(app, repository)
            }
        }
    }

}

