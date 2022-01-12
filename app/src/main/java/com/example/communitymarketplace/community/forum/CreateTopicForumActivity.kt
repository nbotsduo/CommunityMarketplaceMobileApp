package com.example.communitymarketplace.community.forum

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.communitymarketplace.R
import com.example.communitymarketplace.community.news.ViewNewsActivity
import com.example.communitymarketplace.models.Community
import com.example.communitymarketplace.models.Forum
import com.example.communitymarketplace.models.ForumPost
import com.example.communitymarketplace.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_add_news.*
import kotlinx.android.synthetic.main.activity_add_news.toolbarAddNews
import kotlinx.android.synthetic.main.activity_create_topic_forum.*

class CreateTopicForumActivity : AppCompatActivity() {

    private lateinit var user: User
    private lateinit var com:Community

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_topic_forum)

        com = intent.getParcelableExtra<Community>("com")!!
        user=intent.getParcelableExtra<User>("user")!!

        setSupportActionBar(toolbarAddTopic)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarAddTopic.setNavigationOnClickListener {
            val intent = Intent(this,ViewForumMenuActivity::class.java)
            intent.putExtra("com",com)
            intent.putExtra("user",user)
            startActivity(intent)
            finish()
        }

        btn_AddTopic_post.setOnClickListener {
            createTopic()
        }
    }

    private fun createTopic() {
        val forumId = getRandomString(8)
        val category = edt_AddTopic_category.text.toString()
        val title = edt_AddTopic_title.text.toString()
        val comId = com.communityId
        val createdId = FirebaseAuth.getInstance().uid
        val dateCreated = System.currentTimeMillis()
        val lastPost = System.currentTimeMillis()

        val forum = Forum(forumId,category,title,comId,createdId!!,dateCreated,lastPost)

        val postId = getRandomString(5)
        val content = edt_AddTopic_post.text.toString()
        val post = ForumPost(postId,content,forumId,createdId,dateCreated)

        val forumDB =FirebaseDatabase.getInstance().getReference("/community/$comId/forum/$forumId")
        val forumPostDb = FirebaseDatabase.getInstance().getReference("/community/$comId/forum/$forumId/post/$postId")

        forumDB.setValue(forum)
            .addOnSuccessListener {
                forumPostDb.setValue(post)
                    .addOnSuccessListener {
                        Toast.makeText(this,"New Topic has been posted", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this,ViewForumMenuActivity::class.java)
                        intent.putExtra("com",com)
                        intent.putExtra("user",user)
                        startActivity(intent)
                    }
            }

    }

    fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}