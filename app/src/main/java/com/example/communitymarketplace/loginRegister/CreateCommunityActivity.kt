package com.example.communitymarketplace.loginRegister

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.communitymarketplace.R
import com.example.communitymarketplace.menu.CommunityActivity
import com.example.communitymarketplace.models.Community
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_create_community.*


class CreateCommunityActivity : AppCompatActivity() {

    companion object {
        val TAG = "CreateCommunityActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_community)
        setSupportActionBar(toolbarViewCreateNewComm)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarViewCreateNewComm.setNavigationOnClickListener {
            startActivity(Intent(this, JoinCommunityActivity::class.java))
            finish()
        }

        btn_CreateCom.setOnClickListener {
            createCom()
        }

    }

    private fun createCom() {

        val comId = getRandomString(6)
        val comName = edt_ComName.text.toString()
        val comAddress = edt_ComAddress.text.toString()
        val comPostCode = edt_ComPostCode.text.toString()
        val comState = edt_ComPostState.text.toString()
        val gps = ""

        val ref = FirebaseDatabase.getInstance().getReference("/community/$comId")
        val comm = Community(comId, comName, comAddress, comPostCode, comState, gps)

        ref.setValue(comm)
            .addOnSuccessListener {
                Toast.makeText(this, "Successfully Created $comName community", Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(this, JoinCommunityActivity::class.java)
                startActivity(intent)
                Log.d(TAG, "Save to database")
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to set value to database: ${it.message}")
            }
    }

    fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

}