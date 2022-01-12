package com.example.communitymarketplace.views

import com.example.communitymarketplace.R
import com.example.communitymarketplace.models.News
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.cardview_news.view.*

class LatestNewsCard(val news: News) : Item<GroupieViewHolder>() {

    var newsId : News? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        newsId=news
        viewHolder.itemView.txtView_Card_NewsContent.text = news.content
        viewHolder.itemView.txtView_Card_NewsTitle.text=news.title
        val imgNews = viewHolder.itemView.img_newsCard
        Picasso.get().load(news.newsImageUri).into(imgNews)
    }

    override fun getLayout(): Int {
        return R.layout.cardview_news
    }

}