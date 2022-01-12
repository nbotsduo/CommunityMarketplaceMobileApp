package com.example.communitymarketplace.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.communitymarketplace.R
import com.example.communitymarketplace.models.News

class ComNewsRecyclerAdapter(
    private val newsList: ArrayList<News>
) : RecyclerView.Adapter<ComNewsRecyclerAdapter.MyViewHolder>() {

    private var mListener: onItemClickListener? = null

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener){
      mListener=listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.row_item_comnews,
            parent,false)
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentitem = newsList[position]

        holder.txtNews.text = currentitem.title
        holder.newsId=currentitem.newsId


    }

    override fun getItemCount(): Int {

        if(newsList.size>3){
            return 3
        }
        else{
        return newsList.size}
    }


    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val txtNews : TextView = itemView.findViewById(R.id.textOne)
        var newsId:String?=null
//        init {
//            itemView.setOnClickListener{
//                listener.onItemClick(adapterPosition)
//            }
//        }
    }

}