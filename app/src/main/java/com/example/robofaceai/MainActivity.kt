package com.example.robofaceai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.robofaceai.ui.RoboFaceScreen
import com.example.robofaceai.ui.theme.RoboFaceAITheme
import com.example.robofaceai.viewmodel.RoboViewModel

/**
 * Main Activity - Entry point for RoboFaceAI
 *
 * TASK 2, 3, 6 Integration:
 * - Initializes ViewModel with sensor fusion and AI
 * - Sets up Compose UI with RoboFaceScreen
 * - Manages lifecycle
 */
class MainActivity : ComponentActivity() {

    private lateinit var viewModel: RoboViewModel

    // Permission launcher for BODY_SENSORS (required on some devices)
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("RoboFaceAI", "✓ BODY_SENSORS permission granted")
        } else {
            android.util.Log.w("RoboFaceAI", "⚠️ BODY_SENSORS permission denied - proximity may not work")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        android.util.Log.d("RoboFaceAI", "═══ RoboFaceAI Starting ═══")

        try {
            // CRITICAL: Keep screen on for proximity sensor to work properly
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            android.util.Log.d("RoboFaceAI", "✓ Window flags set: KEEP_SCREEN_ON")

            // Request BODY_SENSORS permission if needed (Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                    != PackageManager.PERMISSION_GRANTED) {
                    android.util.Log.d("RoboFaceAI", "🔐 Requesting BODY_SENSORS permission...")
                    permissionLauncher.launch(Manifest.permission.BODY_SENSORS)
                } else {
                    android.util.Log.d("RoboFaceAI", "✓ BODY_SENSORS permission already granted")
                }
            }

            // Initialize ViewModel (AndroidViewModel with Application context)
            viewModel = ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            )[RoboViewModel::class.java]

            setContent {
                RoboFaceAITheme {
                    RoboFaceScreen(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    )
                }
            }

            android.util.Log.d("RoboFaceAI", "✓ RoboFaceAI launched successfully!")
            android.util.Log.d("RoboFaceAI", "🎯 All systems active: Sensors + AI + State Machine")
        } catch (e: Exception) {
            android.util.Log.e("RoboFaceAI", "✗ ERROR: ${e.message}", e)
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("RoboFaceAI", "🛑 RoboFaceAI shutting down...")
    }
}

