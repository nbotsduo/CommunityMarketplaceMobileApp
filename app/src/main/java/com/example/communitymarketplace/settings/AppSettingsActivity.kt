package com.example.communitymarketplace.settings

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import com.example.communitymarketplace.R
import com.example.communitymarketplace.loginRegister.LoginActivity
import com.example.communitymarketplace.menu.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_app_settings.*

class AppSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_settings)

        setSupportActionBar(toolbarViewSettings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarViewSettings.setNavigationOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        val user = arrayOf("Abhay","Joseph","Maria","Avni","Apoorva","Chris","David","Kaira","Dwayne","Christopher",
            "Jim","Russel","Donald","Brack","Vladimir")

        val settings= arrayOf("Edit Profile","Change email or password","Sign Out")

        val settingAdapter : ArrayAdapter<String> = ArrayAdapter(
            this,R.layout.row_item_settings,R.id.textRow,settings
        )

        listSetting.adapter=settingAdapter

        listSetting.setOnItemClickListener { parent, view, position, id ->
            val name = settingAdapter.getItem(position)
//            Toast.makeText(this,"$name", Toast.LENGTH_SHORT).show()
            Log.d("test","$name")
            if(name.equals("Edit Profile")){
                val intent = Intent(this,EditProfileActivity::class.java)
                startActivity(intent)
            }
            if(name.equals("Sign Out")){
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

        }

    }
}