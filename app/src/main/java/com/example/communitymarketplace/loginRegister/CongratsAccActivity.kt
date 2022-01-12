package com.example.communitymarketplace.loginRegister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.communitymarketplace.MainActivity2
import com.example.communitymarketplace.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_congrats_acc.*

class CongratsAccActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_congrats_acc)

        //      Personalize Action Bar
        supportActionBar?.title = "Welcome to Community Marketplace!"

        btn_Congrats.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
    }
}