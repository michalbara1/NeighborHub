package com.example.neighborhub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.firebase.FirebaseApp
import com.example.neighborhub.model.data.AppDatabase

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        // Find the NavHostFragment and NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Optional: Set up the ActionBar to work with the NavController
        NavigationUI.setupActionBarWithNavController(this, navController)

        try {
            val database = AppDatabase.getInstance(applicationContext)
            println("Database initialized successfully. Is Open: ${database.openHelper.writableDatabase.isOpen}")
        } catch (e: Exception) {
            println("Error initializing database: ${e.message}")
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        // Handle back navigation for fragments
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
