package com.example.communitymarketplace.marketplace.seller

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.communitymarketplace.R
import com.example.communitymarketplace.menu.MarketplaceActivity
import kotlinx.android.synthetic.main.activity_seller_main.*

class SellerMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_main)

        setSupportActionBar(toolbarSellerMenu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarSellerMenu.setNavigationOnClickListener {
            val intent = Intent(this, MarketplaceActivity::class.java)
            startActivity(intent)
            finish()
        }

        btn_prodList.setOnClickListener {
            startActivity(Intent(this,ProductListSellerActivity::class.java))
        }
        btn_orderList.setOnClickListener {
            startActivity(Intent(this,OrderListSellerActivity::class.java))
        }
    }
}