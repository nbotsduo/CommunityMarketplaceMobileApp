package com.example.communitymarketplace.community.news

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_view_news.*
import android.content.Intent
import com.example.communitymarketplace.menu.CommunityActivity
import android.view.Menu
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.communitymarketplace.R
import com.example.communitymarketplace.models.News
import com.example.communitymarketplace.views.LatestNewsCard
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder


class ViewNewsActivity : AppCompatActivity() {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val newsId = "news"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.communitymarketplace.R.layout.activity_view_news)

        val comId =intent.getStringExtra("comId")

        setSupportActionBar(toolbarViewNews)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarViewNews.setNavigationOnClickListener {
            startActivity(Intent(this, CommunityActivity::class.java))
            finish()
        }

        toolbarViewNews.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId){
                R.id.addNews ->{
                    val intent = Intent(this,AddNewsActivity::class.java)
                    intent.putExtra("comId",comId)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        listenForNewNews(comId!!)

        recycler_news.adapter = adapter
//        recycler_news.addItemDecoration(
//            DividerItemDecoration(this,DividerItemDecoration.VERTICAL)
//        )

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this,ViewNewsArticleActivity::class.java)
            val row = item as LatestNewsCard
            intent.putExtra(newsId,row.newsId)
            intent.putExtra("comId",comId)
            startActivity(intent)
        }
    }

    val latestNews = HashMap<String, News>()

    private fun listenForNewNews(comId :String) {
        val ref = FirebaseDatabase.getInstance().getReference("/community/$comId/news")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val news = snapshot.getValue(News::class.java) ?: return
                latestNews[snapshot.key!!] = news
                refreshRecyclerViewNews(comId)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val news = snapshot.getValue(News::class.java) ?: return
                latestNews[snapshot.key!!] = news
                refreshRecyclerViewNews(comId)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun refreshRecyclerViewNews(comId :String){
        adapter.clear()
        val sortedMap = HashMap<Long,News>()
        latestNews.forEach {
            sortedMap[0 - it.value.date!!] = it.value
        }
        sortedMap.toSortedMap().values.forEach {
            adapter.add(LatestNewsCard(it))
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.example.communitymarketplace.R.menu.viewnews_top_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }
}