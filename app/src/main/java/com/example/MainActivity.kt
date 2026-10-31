package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.ui.components.TchiBottomBar
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.TchiBackground
import com.example.ui.theme.TchiTheme
import com.example.viewmodel.TchiTab
import com.example.viewmodel.TchiViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TchiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TchiTheme {
                TchiApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TchiApp(viewModel: TchiViewModel) {
    val isSplashFinished by viewModel.isSplashFinished.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val capturedPhotoUri by viewModel.capturedPhotoUri.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Toast message handler
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TchiBackground)
    ) {
        if (!isSplashFinished) {
            SplashScreen(
                onSplashFinish = { viewModel.finishSplash() }
            )
        } else if (currentUser == null) {
            AuthScreen(
                viewModel = viewModel,
                onAuthSuccess = { /* Automatically loads user */ }
            )
        } else if (capturedPhotoUri != null) {
            // Screen to enter product details after taking photo
            BackHandler {
                viewModel.setCapturedPhotoUri(null)
            }
            CreatePostScreen(
                photoPath = capturedPhotoUri!!,
                viewModel = viewModel,
                onPostCreated = {
                    viewModel.setCapturedPhotoUri(null)
                    viewModel.selectTab(TchiTab.JOURNAL)
                },
                onBack = {
                    viewModel.setCapturedPhotoUri(null)
                }
            )
        } else if (currentTab == TchiTab.CAMERA) {
            // Fullscreen Camera mode
            BackHandler {
                viewModel.selectTab(TchiTab.JOURNAL)
            }
            CameraScreen(
                viewModel = viewModel,
                onPhotoCaptured = { photoPath ->
                    viewModel.setCapturedPhotoUri(photoPath)
                },
                onClose = {
                    viewModel.selectTab(TchiTab.JOURNAL)
                }
            )
        } else {
            // Main app shell with 5-tab bottom navigation
            BackHandler(enabled = currentTab != TchiTab.JOURNAL) {
                viewModel.selectTab(TchiTab.JOURNAL)
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = TchiBackground,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    TchiBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { tab ->
                            viewModel.selectTab(tab)
                        }
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_transition"
                    ) { tab ->
                        when (tab) {
                            TchiTab.JOURNAL -> JournalScreen(
                                viewModel = viewModel,
                                onNavigateToCamera = { viewModel.selectTab(TchiTab.CAMERA) }
                            )
                            TchiTab.DISCOVER -> DiscoverScreen(
                                viewModel = viewModel
                            )
                            TchiTab.CAMERA -> {
                                // Fallback if reached inside tab container
                                CameraScreen(
                                    viewModel = viewModel,
                                    onPhotoCaptured = { viewModel.setCapturedPhotoUri(it) },
                                    onClose = { viewModel.selectTab(TchiTab.JOURNAL) }
                                )
                            }
                            TchiTab.STATS -> StatsScreen(
                                viewModel = viewModel,
                                onNavigateToCamera = { viewModel.selectTab(TchiTab.CAMERA) }
                            )
                            TchiTab.PROFILE -> ProfileScreen(
                                viewModel = viewModel,
                                onNavigateToDiscover = { viewModel.selectTab(TchiTab.DISCOVER) }
                            )
                        }
                    }
                }
            }
        }
    }
}
