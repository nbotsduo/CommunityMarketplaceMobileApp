package com.example.communitymarketplace.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.communitymarketplace.R
import com.example.communitymarketplace.community.chat.LatestMessageRow
import com.example.communitymarketplace.community.chat.ViewMessagesActivity
import com.example.communitymarketplace.community.forum.ViewForumMenuActivity
import com.example.communitymarketplace.community.news.ViewNewsActivity
import com.example.communitymarketplace.loginRegister.LoginActivity
import com.example.communitymarketplace.models.Chat
import com.example.communitymarketplace.models.Community
import com.example.communitymarketplace.models.News
import com.example.communitymarketplace.models.User
import com.example.communitymarketplace.views.ComNewsRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_community.*

class CommunityActivity : AppCompatActivity() {

    private lateinit var newsRecyclerView: RecyclerView
    private var newsArrayList: ArrayList<News> = arrayListOf<News>()
    var userObject: User? = null
    var comObject: Community? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        //check for already signed-in user
        val user = Firebase.auth.currentUser?.uid
        Log.d("user id: ", "$user")
        if (user == null) {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        //setup view
        userInfo()

    }

    private fun userInfo() {
        val id = FirebaseAuth.getInstance().uid
        val userDb = FirebaseDatabase.getInstance().getReference("/users")
        val commDb = FirebaseDatabase.getInstance().getReference("/community")
        var comId: String = ""

        userDb.child(id!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                userObject = user
                comId = user?.communityId.toString()
                Log.d("test", "$comId")
                comInfo(comId!!)
                //bottom navigation
                bottomNavigation(comId)

//                initNewsRecyclerView(comId)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun comInfo(comId: String) {
        val comDb = FirebaseDatabase.getInstance().getReference("/community")

        comDb.child(comId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val community = snapshot.getValue(Community::class.java)
                comName.text = community?.communityName
                comAddress.text = community?.address
                comObject = community
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        constraintNews.setOnClickListener {
            val intent = Intent(this, ViewNewsActivity::class.java)
            intent.putExtra("comId", comId)
            startActivity(intent)
        }
        constraintChat.setOnClickListener {
            val intent = Intent(this, ViewMessagesActivity::class.java)
            intent.putExtra("comId", comId)
            startActivity(intent)
        }
        constraintForum.setOnClickListener {
            val intent = Intent(this, ViewForumMenuActivity::class.java)
            intent.putExtra("com", comObject)
            intent.putExtra("user", userObject)
            startActivity(intent)
        }

        newsMore.setOnClickListener {
            val intent = Intent(this, ViewNewsActivity::class.java)
            intent.putExtra("comId", comId)
            startActivity(intent)
        }

        chatsMore.setOnClickListener {
            val intent = Intent(this, ViewMessagesActivity::class.java)
            intent.putExtra("comId", comId)
            startActivity(intent)
        }
        forumsMore.setOnClickListener {
            val intent = Intent(this, ViewForumMenuActivity::class.java)
            intent.putExtra("com", comObject)
            intent.putExtra("user", userObject)
            startActivity(intent)
        }
    }

    private fun bottomNavigation(comId: String) {
        bottom_navigation.selectedItemId = R.id.community

        bottom_navigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.community -> {
                    true
                }
                R.id.marketplace -> {
                    val intent = Intent(this, MarketplaceActivity::class.java)
                    intent.putExtra("comId", comId)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    val latestNews = HashMap<String, News>()
    private fun initNewsRecyclerView(comId: String) {
//        newsRecyclerView = recyclerView_hNews
        val lim = LinearLayoutManager(this)
        newsRecyclerView.layoutManager = lim
        newsRecyclerView.setHasFixedSize(true)
        newsRecyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )


        val ref = FirebaseDatabase.getInstance().getReference("/community/$comId/news")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val news = snapshot.getValue(News::class.java) ?: return
                Log.d("recNews", "${news.title}")
                latestNews[snapshot.key!!] = news

//                newsArrayList.add(news)
                refreshecyclerViewNews(newsRecyclerView)
//                var adapter = ComNewsRecyclerAdapter(newsArrayList)
//                newsRecyclerView.adapter=adapter

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                val news = snapshot.getValue(News::class.java) ?: return
//                latestNews[snapshot.key!!] = news
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun refreshecyclerViewNews(newsRecyclerView: RecyclerView) {
        val sortedMap = HashMap<Long, News>()
        //new hashMap
        latestNews.forEach {
            sortedMap[0 - it.value.date!!] = it.value
            //add to new hashMap with 0 - timestamp as key
        }
        sortedMap.toSortedMap().values.forEach {
//            adapter.add(LatestMessageRow(it, comId))
            newsArrayList.add(it)

        }
        var i=0
        while (i<newsArrayList.size){
            Log.d("news", newsArrayList[i].toString())
            i++
        }
        var adapter = ComNewsRecyclerAdapter(newsArrayList)
        newsRecyclerView.adapter = adapter
    }
}