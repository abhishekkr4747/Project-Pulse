package com.example.projectpulse.activities

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import com.example.projectpulse.databinding.ActivitySplashBinding
import com.example.projectpulse.firebase.FirestoreClass

class Splash_activity : AppCompatActivity() {
    private var binding: ActivitySplashBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(binding?.root)

        val typeface: Typeface = Typeface.createFromAsset(assets , "carbon bl.ttf")
        binding?.tvSplash?.typeface = typeface

        Handler().postDelayed({
            var currentUserID = FirestoreClass().getCurrentUserId()

            if(currentUserID.isNotEmpty()) {
                startActivity(Intent(this , MainActivity::class.java))
            } else {
                startActivity(Intent(this , IntroActivity::class.java))
            }
            finish()
        },2000)
    }
}