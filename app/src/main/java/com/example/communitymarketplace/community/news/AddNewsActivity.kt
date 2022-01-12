package com.example.communitymarketplace.community.news

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.communitymarketplace.R
import com.example.communitymarketplace.models.News
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_news.*


class AddNewsActivity : AppCompatActivity() {

    var selectedPhotoUri : Uri? =null


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_news)

        val comId =intent.getStringExtra("comId")

        setSupportActionBar(toolbarAddNews)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarAddNews.setNavigationOnClickListener {
            startActivity(Intent(this, ViewNewsActivity::class.java))
            finish()
        }

        btn_image_add_news.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        btn_post.setOnClickListener {
            postNews(comId!!)
        }
    }

    private fun postNews(comId: String) {
        uploadImageToStorage(comId!!)
    }

    private fun uploadImageToStorage(comId: String) {
        if (selectedPhotoUri == null) return

        val filename = getRandomString(6).toString()
        val ref = FirebaseStorage.getInstance().getReference("/community/$comId/news/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    saveDataToFirebaseDatabase(it.toString(),filename,comId!!)
                }
            }

    }

    private fun saveDataToFirebaseDatabase(toString: String, filename: String,comId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/community/$comId/news/$filename")
        val title = edt_newsTitle.text.toString()
        val content = edt_newsContent.text.toString()
        val time = System.currentTimeMillis()
        val userId= FirebaseAuth.getInstance().uid
        val news = News(filename,title,content,toString,time,userId!!)

        ref.setValue(news)
            .addOnSuccessListener {
                Toast.makeText(this,"News has been published",Toast.LENGTH_SHORT).show()
                val intent = Intent(this,ViewNewsActivity::class.java)
                intent.putExtra("comId",comId)
                startActivity(intent)
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // proceed and check what the selected image was....

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            //Circle Image View - https://github.com/hdodenhof/CircleImageView
            image_add_news.setImageBitmap(bitmap)

            btn_image_add_news.alpha = 0f

            val currentLayout = btn_post.layoutParams as ConstraintLayout.LayoutParams
            currentLayout.topToBottom=R.id.image_add_news
            btn_post.layoutParams =currentLayout

        }
    }

    fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}