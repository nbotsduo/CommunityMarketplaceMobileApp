package com.example.communitymarketplace.loginRegister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.communitymarketplace.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        //      Personalize Action Bar
        supportActionBar?.title = "Sign Up"

        txtView_link_signIn.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            when{
                TextUtils.isEmpty(edt_ComName.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this,
                        "Please enter Email",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(edt_Password.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this,
                        "Please enter Password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(edt_PasswordConfirm.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this,
                        "Please enter Confirm Password",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {

                    val email: String = edt_ComName.text.toString().trim { it <= ' ' }
                    val password: String = edt_Password.text.toString().trim { it <= ' ' }
                    val passwordCon: String = edt_PasswordConfirm.text.toString().trim { it <= ' ' }

                    if(password!=passwordCon){
                        Toast.makeText(
                            this,
                            "Password and Confirm password is unmatch",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    //Create an instance and create a register a user with email and password.
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            OnCompleteListener<AuthResult> { task ->

                                // If the registration is successfully done
                                if (task.isSuccessful) {

                                    //Firebase registered user
                                    val firebaseUser: FirebaseUser = task.result!!.user!!

                                    Toast.makeText(
                                        this,
                                        "You are registered successfully.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val intent =
                                        Intent(this, SetupAccountActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    intent.putExtra("user_id", firebaseUser.uid)
                                    intent.putExtra("email_id", email)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    //If the registering is not successful
                                    Toast.makeText(
                                        this,
                                        task.exception!!.message.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                }
            }
        }
    }
}