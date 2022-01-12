package com.example.communitymarketplace

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.communitymarketplace.loginRegister.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main2.*
import android.widget.Toast

import android.R
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.PopupMenu


class MainActivity2 : AppCompatActivity() {
    var button: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main2)
//        button = findViewById<View>(R.id.button) as Button
//        button.setOnClickListener(View.OnClickListener {
//
//            //Creating the instance of PopupMenu
//            val popup = PopupMenu(this@MainActivity, button!!)
//            //Inflating the Popup using xml file
//            popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)
//
//            //registering popup with OnMenuItemClickListener
//            popup.setOnMenuItemClickListener { item ->
//                Toast.makeText(
//                    this@MainActivity,
//                    "You Clicked : " + item.title,
//                    Toast.LENGTH_SHORT
//                ).show()
//                true
//            }
//            popup.show() //showing popup menu
//        }) //closing the setOnClickListener method
    }
}