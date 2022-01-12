package com.example.communitymarketplace.loginRegister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.communitymarketplace.MainActivity2
import com.example.communitymarketplace.R
import com.example.communitymarketplace.menu.CommunityActivity
import com.example.communitymarketplace.models.Community
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //      Personalize Action Bar
        supportActionBar?.title = "Login Account"

        txtView_link_signUp.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
        }

        btn_login.setOnClickListener {
            when{
                TextUtils.isEmpty(edt_ComName.text.toString().trim{it <= ' '}) ->{
                    Toast.makeText(this,"Please enter Email Address",Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(edt_Password.text.toString().trim{it <= ' '}) ->{
                    Toast.makeText(this,"Please enter Password",Toast.LENGTH_SHORT).show()
                }
                else ->{

                    val email = edt_ComName.text.toString().trim{it <= ' '}
                    val password = edt_Password.text.toString().trim{it <= ' '}

//                    Log in Firebase Auth
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                        .addOnCompleteListener { task ->

                            if (task.isSuccessful){
                                Toast.makeText(
                                    this,
                                    "You Are Logged in Successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent =
                                    Intent(this, CommunityActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("user_id", FirebaseAuth.getInstance().currentUser!!.uid)
                                intent.putExtra("email_id", email)
                                startActivity(intent)
                                finish()
                            }else{
                                //If login not successful
                                Toast.makeText(
                                    this,
                                    task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                }
            }
        }

    }
}