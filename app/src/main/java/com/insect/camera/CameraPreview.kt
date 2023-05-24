import android.content.Context
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
) {
    val context = LocalContext.current
    val cameraExecutor = LocalContext.current.cameraExecutor
    val imageCapture = remember { ImageCapture.Builder().build() }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                this.scaleType = scaleType
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            val cameraSelector = cameraSelector

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                bindCameraUseCases(
                    cameraProvider,
                    cameraSelector,
                    previewView,
                    imageCapture,
                    cameraExecutor
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

private fun bindCameraUseCases(
    cameraProvider: ProcessCameraProvider,
    cameraSelector: CameraSelector,
    previewView: PreviewView,
    imageCapture: ImageCapture,
    cameraExecutor: ExecutorService
) {
    val preview = Preview.Builder().build().also {
        it.setSurfaceProvider(previewView.surfaceProvider)
    }

    val camera = cameraProvider.bindToLifecycle(
        context as LifecycleOwner,
        cameraSelector,
        preview,
        imageCapture
    )

    // Additional configuration and setup for camera preview and image capture
    // ...

    // Example: Capture photo on button click
    capturePhoto(cameraExecutor, imageCapture)
}

private fun capturePhoto(
    cameraExecutor: ExecutorService,
    imageCapture: ImageCapture
) {
    // Example: Capture photo and save to file
    val outputFile = createPhotoOutputFile()
    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

    imageCapture.takePicture(
        outputOptions,
        cameraExecutor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Photo capture successful, handle the saved photo
                val savedUri = outputFileResults.savedUri
                // Process the saved photo URI or file
            }

            override fun onError(exception: ImageCaptureException) {
                // Photo capture failed, handle the error
                exception.printStackTrace()
            }
        }
    )
}

// Extension property to get the camera executor from the context
val Context.cameraExecutor: ExecutorService
    get() = Executors.newSingleThreadExecutor()
