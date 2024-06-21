package de.luh.hci.mi.foodfacts.ui.scanning

import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import de.luh.hci.mi.foodfacts.Routes

@Composable
fun ScanningScreen(
    onNavigate: (result: String) -> Unit, // used to navigate to another screen
    viewModel: ScanningViewModel,
) {
    val cameraProviderFuture = viewModel.cameraProviderFuture
    val lifecycleOwner = LocalLifecycleOwner.current
    Box(modifier = Modifier) {
        // Composable function "AndroidView" wraps an android.view.View. Here, it wraps the
        // PreviewView, which is a custom view that displays the camera's live preview.
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    setBackgroundColor(android.graphics.Color.WHITE)
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    scaleType = PreviewView.ScaleType.FILL_START
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    cameraProviderFuture.addListener({
                        viewModel.bindPreview(
                            cameraProviderFuture,
                            lifecycleOwner,
                            this,
                        )
                    }, ContextCompat.getMainExecutor(context))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        // The preview is in the background, the column with the scanned barcode (top) and the
        // button (bottom) is in the foreground.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            val barcode = viewModel.barcode
            Text(
                text = "Barcode: $barcode",
                modifier = Modifier
                    .background(Color(0x77FFFFFF)) // semitransparent white background
                    .padding(8.dp),
                fontSize = 24.sp
            )
            Spacer(Modifier.weight(1f)) // fill remaining space in the middle
            if (barcode.isNotEmpty()) {
                Button(
                    onClick = {
                        Log.d(javaClass.simpleName, "scanned: $barcode")
                        // navigate to the product page, provide it with the scanned EAN
                        onNavigate(Routes.PRODUCT + "?barcode=$barcode")
                    },
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("Get Product Information")
                }
            }
        }
    }
}
