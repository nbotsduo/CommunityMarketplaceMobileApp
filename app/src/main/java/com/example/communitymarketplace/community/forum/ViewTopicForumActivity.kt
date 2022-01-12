package com.example.communitymarketplace.community.forum

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.communitymarketplace.R
import com.example.communitymarketplace.marketplace.seller.OrderListSeller
import com.example.communitymarketplace.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_view_topic_forum.*
import kotlinx.android.synthetic.main.post_from_others.view.*
import kotlinx.android.synthetic.main.post_from_user.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ViewTopicForumActivity : AppCompatActivity() {

    private val adapter = GroupAdapter<GroupieViewHolder>()
//    private lateinit var com: Community
    private lateinit var user: User
    private lateinit var topic:Forum
    var latestPost = HashMap<String,ForumPost>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_topic_forum)

//        com = intent.getParcelableExtra<Community>("com")!!
        user=intent.getParcelableExtra<User>("user")!!
        topic=intent.getParcelableExtra<Forum>("topic")!!

        setSupportActionBar(toolbarViewTopicForum)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarViewTopicForum.setNavigationOnClickListener {
            finish()
        }
//        txt_ViewTopicForum_topic.text=topic.title
        toolbarViewTopicForum.title=topic.title

        recyclerview_ViewTopicForum.adapter=adapter

        listenForPost()

        btn_ViewTopicForum_post.setOnClickListener {
            publishPost()
        }
    }
//    private fun refreshRecyclerViewPost(product: Product) {
//        adapter.clear()
//        val sortedMap = HashMap<Long, Order>()
//        latestPost.forEach {
//            sortedMap[0-it.value.timestamp!!] = it.value
//        }
//        sortedMap.toSortedMap().values.forEach {
//            adapter.add(OrderListSeller(it,product))
//        }
//    }

    private fun publishPost() {

        val comId = topic.comID
        val postContent = edt_ViewTopicForum_post.text.toString()
        val postId = getRandomString(5)
        val forumId = topic.forumId
        val userId =FirebaseAuth.getInstance().uid
        val timeStamp = System.currentTimeMillis()

        val postedContent = ForumPost(postId,postContent,forumId,userId!!,timeStamp)
        val upLastPost = mapOf<String,Long>(
            "lastPost" to timeStamp
        )

        val forumDB =FirebaseDatabase.getInstance().getReference("/community/$comId/forum/$forumId")
        val forumPostDb = FirebaseDatabase.getInstance().getReference("/community/$comId/forum/$forumId/post/$postId")

       forumPostDb.setValue(postedContent)
           .addOnSuccessListener {
               forumDB.updateChildren(upLastPost)
                   .addOnSuccessListener {
                       edt_ViewTopicForum_post.text.clear()
                       recyclerview_ViewTopicForum.scrollToPosition(adapter.itemCount - 1)
                   }
           }
    }

    private fun listenForPost() {
        val comId = topic.comID
        val topicId = topic.forumId

        val postDb = FirebaseDatabase.getInstance().getReference("/community/$comId/forum/$topicId/post")

        Log.d("post",topic.toString())
        postDb.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val posted=snapshot.getValue(ForumPost::class.java)!!

                if(posted!=null){
                    if(posted.userId== FirebaseAuth.getInstance().uid){
                        adapter.add(PostFromUser(posted,user))
                    }else{
                        getMemberDetails(posted,comId)
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getMemberDetails(post: ForumPost, comId: String) {
        val userId = post.userId
        val ref = FirebaseDatabase.getInstance().getReference("/community/$comId/members")

        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val userPost = snapshot.getValue(User::class.java)!!
                if(userPost.userId == userId){
                    adapter.add(PostFromMembers(post,userPost))
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}

class PostFromUser(private val post: ForumPost,private val userPost:User):Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.txt_postContent_from_user.text = post.content
        Picasso.get().load(userPost.profileImageUri).into(viewHolder.itemView.imageView_from_user)
        viewHolder.itemView.txt_username_from_user.text = userPost.username
        viewHolder.itemView.txt_postDate_from_user.text=convertLongToTime(post.timestamp)
    }
    override fun getLayout(): Int {
        return R.layout.post_from_user
    }
    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd MMMM yyyy hh:mm a", Locale.UK)
        return format.format(date)
    }
}

class PostFromMembers(private val post: ForumPost,private val userPost:User):Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.txt_postContent_from_other.text = post.content
        Picasso.get().load(userPost.profileImageUri).into(viewHolder.itemView.imageView_from_other)
        viewHolder.itemView.txt_username_from_other.text = userPost.username
        viewHolder.itemView.txt_postDate_from_other.text=convertLongToTime(post.timestamp)
    }

    override fun getLayout(): Int {
        return R.layout.post_from_others
    }
    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd MMMM yyyy hh:mm a", Locale.UK)
        return format.format(date)
    }
}