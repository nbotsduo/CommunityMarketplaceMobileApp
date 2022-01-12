package com.example.communitymarketplace.community.forum

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.communitymarketplace.R
import com.example.communitymarketplace.community.chat.ChatLogActivity
import com.example.communitymarketplace.community.chat.LatestMessageRow
import com.example.communitymarketplace.community.chat.SelectMemberChatActivity
import com.example.communitymarketplace.menu.CommunityActivity
import com.example.communitymarketplace.models.Community
import com.example.communitymarketplace.models.Forum
import com.example.communitymarketplace.models.ForumPost
import com.example.communitymarketplace.models.User
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_community.*
import kotlinx.android.synthetic.main.activity_view_forum_menu.*
import kotlinx.android.synthetic.main.row_forum_topic.view.*

class ViewForumMenuActivity : AppCompatActivity() {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var com: Community
    private lateinit var user: User
    val latestForumMap = HashMap<String, Forum>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_forum_menu)

        com = intent.getParcelableExtra<Community>("com")!!
        user = intent.getParcelableExtra<User>("user")!!

        setSupportActionBar(toolbarViewForumMain)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarViewForumMain.setNavigationOnClickListener {
            val intent = Intent(this, CommunityActivity::class.java)
            intent.putExtra("comId", com.communityId)
            startActivity(intent)
            finish()
        }

        recyclerview_ViewForumMain.adapter = adapter
        fetchForumTopic()

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ViewTopicForumActivity::class.java)
            ///get chat partner user
            val row = item as TopicList
            intent.putExtra("user", user)
            intent.putExtra("topic", row.forumObject)
            startActivity(intent)
        }

        floatingActionButton_ViewForumMain.setOnClickListener {
            val intent = Intent(this, CreateTopicForumActivity::class.java)
            intent.putExtra("com", com)
            intent.putExtra("user", user)
            startActivity(intent)
        }
    }

    var forumList: ArrayList<Forum>? = ArrayList()
    val postList: ArrayList<ForumPost>? = ArrayList()
    val userList: ArrayList<User>? = ArrayList()

    private fun fetchForumTopic() {
        val comId = com.communityId

        val topicDb = FirebaseDatabase.getInstance().getReference("/community/$comId/forum")
        topicDb.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val topic = snapshot.getValue(Forum::class.java)!!
                latestForumMap[snapshot.key!!] = topic
//                forumList?.add(topic)
//                getLatestPost(topic, comId)
                refreshRecyclerViewForum()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val topic = snapshot.getValue(Forum::class.java)!!
                latestForumMap[snapshot.key!!]=topic
                refreshRecyclerViewForum()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getLatestPost(topic: Forum, comId: String) {

        val topicId = topic.forumId
        val postDb =
            FirebaseDatabase.getInstance().getReference("/community/$comId/forum/$topicId/post")

        postDb.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val post = snapshot.getValue(ForumPost::class.java)!!
                if (post.timestamp == topic.lastPost) {
                    postList?.add(post)
                    getUser(topic, comId, post)
                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                val post = snapshot.getValue(ForumPost::class.java)!!
//                if(post.timestamp==topic.lastPost){
//                    getUser(topic,comId,post)
//                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun getUser(topic: Forum, comId: String, post: ForumPost) {

        val userId = post.userId
        val ref = FirebaseDatabase.getInstance().getReference("/community/$comId/members")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val userPost = snapshot.getValue(User::class.java)!!
                if (userPost.userId == userId) {
                    Log.d("topic", userPost.toString())
                    Log.d("topic", "test")
                    userList?.add(userPost)
//                    sortPost()
                    refreshRecyclerViewForum()
//                    adapter.add(TopicList(topic,com, user,post,userPost))
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

    private fun refreshRecyclerViewForum() {
        adapter.clear()
        val sortedMap = HashMap<Long, Forum>()
        //new hashMap
        latestForumMap.forEach {
            sortedMap[0 - it.value.lastPost] = it.value
            //add to new hashMap with 0 - timestamp as key
        }
        sortedMap.toSortedMap().values.forEach {
            adapter.add(TopicList(it))
        }
    }

}

class TopicList(
    private val topic: Forum,
) : Item<GroupieViewHolder>() {

    var forumObject: Forum? = null
    var comObject: Community? = null
    var userObject: User? = null
    var postContent:String?=null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        forumObject=topic
        viewHolder.itemView.txtView_Card_Topic.text = topic.title
        val topicId = topic.forumId
        val comId = topic.comID
        val postDb =
            FirebaseDatabase.getInstance().getReference("/community/$comId/forum/$topicId/post")

        postDb.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val post = snapshot.getValue(ForumPost::class.java)!!
                if (post.timestamp == topic.lastPost) {
                    val userId = post.userId
                    postContent=post.content
                    val ref = FirebaseDatabase.getInstance().getReference("/community/$comId/members")

                    ref.addChildEventListener(object : ChildEventListener {
                        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                            val userPost = snapshot.getValue(User::class.java)!!

                            if (userPost.userId == userId) {
                                userObject = userPost
                                viewHolder.itemView.txtView_Card_LastPost.text = userPost.username + ": " + post.content
                                Log.d("topic", userPost.toString())
                                Log.d("topic", "test")
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

    override fun getLayout(): Int {
        return R.layout.row_forum_topic
    }

}