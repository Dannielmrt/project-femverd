package com.example.femverd
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.compose.FemVerdTheme
import com.example.femverd.ui.navigation.FemVerdApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This enables the app to use the full screen behind status bar
        enableEdgeToEdge()

        setContent {
            // Apply your custom Material3 theme
            FemVerdTheme {
                // The root the application UI
                FemVerdApp()
            }
        }
    }
}