package com.quoc.schedule.feature.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.quoc.schedule.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File

/**
 * MÃ n chá»¥p áº£nh trá»±c tiáº¿p â€” áº£nh chá»¥p xong Ä‘Æ°a vÃ o ImportViewModel.importImage().
 * áº¢nh lÆ°u trong cacheDir cá»§a app, khÃ´ng cáº§n MediaStore.
 */
@Composable
fun CameraScreen(
    onCapture: (Uri) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var capturing by remember { mutableStateOf(false) }
    var permissionRefresh by remember { mutableIntStateOf(0) }

    val hasPermission = remember(permissionRefresh) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionRefresh++          // trigger recompose sau khi user tráº£ lá»i
        if (!granted) errorText = "Cáº§n quyá»n camera Ä‘á»ƒ chá»¥p áº£nh lá»‹ch."
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (hasPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).also { previewView ->
                        val providerFuture = ProcessCameraProvider.getInstance(ctx)
                        providerFuture.addListener({
                            val cameraProvider = providerFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            imageCapture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture
                            )
                        }, ContextCompat.getMainExecutor(ctx))
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(stringResource(R.string.str_unknown), fontSize = 44.sp)
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.str_c___n_quy___n_truy_c_), color = Color.White,
                    style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    shape = RoundedCornerShape(14.dp)
                ) { Text(stringResource(R.string.str_c___p_quy___n)) }
            }
        }

        // Thanh dÆ°á»›i: há»§y + nÃºt chá»¥p
        Row(
            Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.str_h___y), color = Color.White.copy(alpha = 0.8f))
            }
            Box(
                Modifier.size(74.dp).background(Color.White.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        val capture = imageCapture
                        if (capture == null) {
                            errorText = "Camera chÆ°a sáºµn sÃ ng"
                            return@Button
                        }
                        capturing = true
                        val outFile = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(outFile).build()
                        capture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    capturing = false
                                    onCapture(Uri.fromFile(outFile))
                                }
                                override fun onError(e: ImageCaptureException) {
                                    capturing = false
                                    errorText = "Chá»¥p áº£nh tháº¥t báº¡i: ${e.message}"
                                }
                            }
                        )
                    },
                    modifier = Modifier.size(66.dp),
                    shape = CircleShape,
                    enabled = !capturing && hasPermission,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(if (capturing) "â³" else "â—‰", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }

    errorText?.let { msg ->
        AlertDialog(
            onDismissRequest = { errorText = null },
            title = { Text(stringResource(R.string.str_c___v___n)) },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { errorText = null }) { Text(stringResource(R.string.str_hi___u)) } }
        )
    }
}
