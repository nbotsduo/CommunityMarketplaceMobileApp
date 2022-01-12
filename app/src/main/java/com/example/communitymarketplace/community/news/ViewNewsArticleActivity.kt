package com.example.communitymarketplace.community.news

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.communitymarketplace.R
import com.example.communitymarketplace.models.News
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_view_news_article.*
import java.text.SimpleDateFormat
import java.util.*

class ViewNewsArticleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_news_article)

        val comId = intent.getStringExtra("comId")
        val news = intent.getParcelableExtra<News>("news")
        setSupportActionBar(toolbarViewArticleNews)
        toolbarViewArticleNews.title = news?.title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarViewArticleNews.setNavigationOnClickListener {
            val intent = Intent(this, ViewNewsActivity::class.java)
            intent.putExtra("comId",comId)
            startActivity(intent)
            finish()
        }

        val userId = news?.userId
        val ref = FirebaseDatabase.getInstance().getReference("/users")

        ref.child(userId!!).get().addOnSuccessListener {
            if(it.exists()){
                val name = it.child("username").value as String
                textView_NewsAuthor.text=name
            }
        }

        val imgNews = news?.newsImageUri
        Picasso.get().load(imgNews).into(img_NewsArticle)
        txtView_NewsTitle.text=news?.title
        txtView_NewsContent.text = news?.content
        val date = convertLongToTime(news.date!!)
        textView_NewsDate.text= date

    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd MMMM yyyy hh:mm a",Locale.UK)
        return format.format(date)
    }
}