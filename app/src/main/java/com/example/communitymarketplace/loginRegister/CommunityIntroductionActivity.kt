package com.example.communitymarketplace.loginRegister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.communitymarketplace.R
import kotlinx.android.synthetic.main.activity_community_introduction.*

class CommunityIntroductionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_introduction)

        btn_JoinCom.setOnClickListener {
            startActivity(Intent(this,JoinCommunityActivity::class.java))
        }
        btn_CreateNewCom.setOnClickListener {
            startActivity(Intent(this,CreateCommunityActivity::class.java))
        }
    }
}