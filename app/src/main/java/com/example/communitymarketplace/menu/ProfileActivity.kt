package com.example.communitymarketplace.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.communitymarketplace.R
import com.example.communitymarketplace.models.Community
import com.example.communitymarketplace.models.User
import com.example.communitymarketplace.settings.AppSettingsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_community.bottom_navigation
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //bottom navigation
        bottomNavigation()

        img_Settings.setOnClickListener {
            val intent = Intent(this, AppSettingsActivity::class.java)
            startActivity(intent)
        }
//        Glide.get(this@ProfileActivity).clearMemory();
        displayProfile(intent)

    }

//    var comId:String? = null
    var user:User? = null
    var community:Community? =null

    private fun displayProfile(intent: Intent) {
        val userId = FirebaseAuth.getInstance().uid
        val userDb = FirebaseDatabase.getInstance().getReference("/users")
        userDb.child(userId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)
                val comId = user?.communityId.toString()
                val img = user?.profileImageUri
                getCommunityInfo(comId,intent)
//                Picasso.get().load(img).into(img_profilePic)
                Glide.with(this@ProfileActivity).load(img).signature(ObjectKey(System.currentTimeMillis())).into(img_profilePic);
                Log.d("img","$img")
                txtView_username.text=user?.username
                txtView_fullName.text=user?.name
                txtView_aboutMe.text=user?.biography

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    private fun getCommunityInfo(comId: String, intent: Intent) {
        val commDb = FirebaseDatabase.getInstance().getReference("/community")
        commDb.child(comId!!).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                community=snapshot.getValue(Community::class.java)
                txtView_content_CommunityName.text=community?.communityName

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun bottomNavigation() {
        bottom_navigation.selectedItemId = R.id.profile

        bottom_navigation.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.community ->{
                    startActivity(Intent(this,CommunityActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }
                R.id.marketplace ->{
                    startActivity(Intent(this,MarketplaceActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }
                R.id.profile ->{
                    true
                }
                else -> false
            }
        }
    }

}