package com.example.flowwidget

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.flowwidget.navigation.FlowDestinations
import com.example.flowwidget.navigation.FlowNavGraph
import com.example.flowwidget.ui.theme.FlowTheme
import com.example.flowwidget.ui.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Lógica após permissão ser concedida ou negada
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        checkNotificationPermission()

        val startDestination = if (intent?.getStringExtra("navigate_to") == "settings") {
            FlowDestinations.SETTINGS_ROUTE
        } else {
            FlowDestinations.HOME_ROUTE
        }

        setContent {
            val viewModel: SettingsViewModel = hiltViewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            FlowTheme(darkTheme = isDarkMode) {
                FlowNavGraph(startDestination = startDestination)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
