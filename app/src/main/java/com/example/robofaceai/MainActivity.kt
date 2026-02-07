package com.example.robofaceai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        android.util.Log.d("RoboFaceAI", "═══ RoboFaceAI Starting ═══")

        try {
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

