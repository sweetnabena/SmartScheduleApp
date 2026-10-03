package com.example.f329723_app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.f329723_app.ui.screens.home.HomeScreen
import com.example.f329723_app.ui.theme.F329723_AppTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.example.f329723_app.ui.navigation.MainScreen
import dagger.hilt.android.AndroidEntryPoint




@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_F329723_App) //switch to real theme after splash

        FirebaseFirestore.getInstance().clearPersistence().addOnSuccessListener {
            Log.d("Firestore", "Cleared cache successfully")
        }

        //test: Firebase write
        val db = FirebaseFirestore.getInstance()
        val testData = hashMapOf("hello" to "firebase")
        db.collection("debug").add(testData)

        // Set Compose UI
        setContent {
            F329723_AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}
