package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.TchiBackground
import com.example.ui.theme.TchiBorder
import com.example.ui.theme.TchiCard
import com.example.ui.theme.TchiPrimary
import com.example.ui.theme.TchiSecondaryText
import com.example.ui.theme.TchiWhite
import com.example.util.PhotoStorageUtil
import com.example.viewmodel.TchiViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    viewModel: TchiViewModel,
    onPhotoCaptured: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Photo review state
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var capturedFilePath by remember { mutableStateOf<String?>(null) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }

    // Pick from gallery launcher
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val savedPath = PhotoStorageUtil.saveFromUri(context, uri)
                val bitmap = BitmapFactory.decodeFile(savedPath)
                capturedBitmap = bitmap
                capturedFilePath = savedPath
            }
        }
    }

    // CameraX instance references
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // If a photo was captured/chosen, show the PREVIEW and CONFIRMATION mode
    if (capturedBitmap != null && capturedFilePath != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Image(
                bitmap = capturedBitmap!!.asImageBitmap(),
                contentDescription = "Xem trước ảnh",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xem trước ảnh món đồ",
                    color = TchiWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                IconButton(
                    onClick = {
                        capturedBitmap = null
                        capturedFilePath = null
                    },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TchiWhite)
                }
            }

            // Bottom action buttons: "Chụp lại" vs "Dùng ảnh này"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        capturedBitmap = null
                        capturedFilePath = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiCard,
                        contentColor = TchiWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .weight(1f)
                        .padding(end = 8.dp)
                        .testTag("retake_photo_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = TchiSecondaryText)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chụp lại", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        capturedFilePath?.let { path ->
                            onPhotoCaptured(path)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiPrimary,
                        contentColor = Color(0xFF071521)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .weight(1f)
                        .padding(start = 8.dp)
                        .testTag("confirm_photo_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF071521))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dùng ảnh", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    // LIVE CAMERA / PERMISSION VIEW
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // Fullscreen CameraX Preview
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                        val cameraSelector = if (isFrontCamera) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }

                        imageCapture.flashMode = if (isFlashOn) {
                            ImageCapture.FLASH_MODE_ON
                        } else {
                            ImageCapture.FLASH_MODE_OFF
                        }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize(),
                update = {
                    // Update flash / camera flip if needed
                }
            )

            // Top Controls (Flash, Flip, Close)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng camera", tint = TchiWhite)
                }

                Row {
                    // Flash Toggle
                    IconButton(
                        onClick = { isFlashOn = !isFlashOn },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Bật/Tắt Flash",
                            tint = if (isFlashOn) TchiPrimary else TchiWhite
                        )
                    }

                    // Flip camera
                    IconButton(
                        onClick = { isFrontCamera = !isFrontCamera },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Chuyển Camera",
                            tint = TchiWhite
                        )
                    }
                }
            }

            // Bottom Controls (Gallery, Shutter Button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp, start = 32.dp, end = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pick from Gallery
                IconButton(
                    onClick = {
                        galleryPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .border(1.dp, TchiBorder, CircleShape)
                        .testTag("gallery_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Chọn từ thư viện",
                        tint = TchiWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Shutter Button (Glowing circular button)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(4.dp, TchiPrimary, CircleShape)
                        .padding(6.dp)
                        .background(TchiPrimary, CircleShape)
                        .clickable {
                            imageCapture.takePicture(
                                cameraExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val buffer = image.planes[0].buffer
                                        val bytes = ByteArray(buffer.remaining())
                                        buffer.get(bytes)
                                        val rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                                        // Rotate bitmap according to rotationDegrees
                                        val matrix = Matrix().apply {
                                            postRotate(image.imageInfo.rotationDegrees.toFloat())
                                            if (isFrontCamera) postScale(-1f, 1f)
                                        }
                                        val finalBitmap = Bitmap.createBitmap(
                                            rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true
                                        )

                                        scope.launch {
                                            val savedPath = PhotoStorageUtil.saveBitmap(context, finalBitmap)
                                            capturedBitmap = finalBitmap
                                            capturedFilePath = savedPath
                                        }
                                        image.close()
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        exception.printStackTrace()
                                        scope.launch {
                                            viewModel.showToast("Không thể chụp ảnh: ${exception.message}")
                                        }
                                    }
                                }
                            )
                        }
                        .testTag("camera_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF071521), CircleShape)
                    )
                }

                // Empty balancing box
                Spacer(modifier = Modifier.size(52.dp))
            }
        } else {
            // Permission request UI with Gallery fallback
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Quyền truy cập Máy ảnh",
                    color = TchiWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "TCHI cần quyền máy ảnh để bạn có thể chụp ngay món đồ vừa chi tiêu. Bạn cũng có thể chọn ảnh có sẵn từ thư viện.",
                    color = TchiSecondaryText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiPrimary,
                        contentColor = Color(0xFF071521)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("CẤP QUYỀN CAMERA", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        galleryPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TchiCard,
                        contentColor = TchiWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = TchiPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CHỌN ẢNH TỪ THƯ VIỆN")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = TchiSecondaryText
                    )
                ) {
                    Text("Đóng")
                }
            }
        }
    }
}
