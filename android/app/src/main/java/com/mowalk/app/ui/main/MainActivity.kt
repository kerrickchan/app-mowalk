package com.mowalk.app.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.mowalk.app.di.ViewModelFactory
import com.mowalk.app.ui.navigation.AppNavHost
import com.mowalk.app.ui.theme.MoWalkTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startStepCounterService()
        }
    }

    private val activityRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Permission denied - app can still function with limited accuracy
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MoWalkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val factory = remember {
                        ViewModelFactory(context.applicationContext as android.app.Application)
                    }
                    AppNavHost(viewModelFactory = factory)
                }
            }
        }

        requestPermissionsAndStartService()
    }

    override fun onResume() {
        super.onResume()
        requestPermissionsAndStartService()
    }

    private fun requestPermissionsAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, notificationPermission) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(notificationPermission)
                return
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.ACTIVITY_RECOGNITION
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                activityRecognitionLauncher.launch(permission)
            }
        }

        startStepCounterService()
    }

    private fun startStepCounterService() {
        val serviceIntent = Intent(this, com.mowalk.app.service.StepCounterService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
}
