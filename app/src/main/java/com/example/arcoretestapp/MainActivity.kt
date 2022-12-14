package com.example.arcoretestapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.arcoretestapp.databinding.ActivityMainBinding
import com.example.arcoretestapp.helloar.HelloArActivity
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Session

class MainActivity : AppCompatActivity() {
    lateinit var bindingClass: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_button)
    }

    fun onClick(view: View?) {
        val i: Intent
        i = Intent(this, HelloArActivity::class.java)
        startActivity(i)
    }
}