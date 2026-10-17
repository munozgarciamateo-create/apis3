package com.example.apis3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apis3.presentacion.MainViewModel
import com.example.apis3.ui.App
import com.example.apis3.ui.theme.Apis3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Apis3Theme {
                val vm: MainViewModel = viewModel()
                App(vm)
            }
        }
    }
}