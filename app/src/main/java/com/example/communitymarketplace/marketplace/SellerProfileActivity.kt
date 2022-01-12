package com.example.communitymarketplace.marketplace

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.communitymarketplace.R
import com.example.communitymarketplace.menu.MarketplaceActivity
import com.example.communitymarketplace.models.Product
import com.example.communitymarketplace.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_seller_profile.*
import kotlinx.android.synthetic.main.activity_view_product.*

class SellerProfileActivity : AppCompatActivity() {

    private lateinit var product: Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_profile)

        product=intent.getParcelableExtra<Product>("product")!!
        val comName=intent.getStringExtra("comName")
        txtView_content_sellerCommunityName.text=comName

        setSupportActionBar(toolbarViewSellerProfile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarViewSellerProfile.setNavigationOnClickListener {
            val intent= Intent(this, ViewProductActivity::class.java)
            intent.putExtra("product",product)
            startActivity(intent)
            finish()
        }

        toolbarViewSellerProfile.title=product.productName

        setProfileInfo()
        setListProduct()

    }

    private fun setListProduct() {
//        TODO("Not yet implemented")
    }

    private fun setProfileInfo() {
        val userDb = FirebaseDatabase.getInstance().getReference("/users")
        userDb.child(product.sellerId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                Glide.with(this@SellerProfileActivity).load(user?.profileImageUri).signature(ObjectKey(System.currentTimeMillis())).into(img_sellerProfilePic)
                txtView_sellerUsername.text=user?.username
                txtView_sellerFullName.text=user?.name
                txtView_sellerAboutMe.text=user?.biography
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}