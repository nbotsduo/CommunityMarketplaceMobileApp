package com.example.communitymarketplace.models

import android.os.Parcelable
import au.com.console.kassava.kotlinToString
import kotlinx.android.parcel.Parcelize

@Parcelize
class News (val newsId:String, val title:String, val content:String, val newsImageUri:String, val date: Long?, val userId:String) :
    Parcelable {
    constructor():this("","","","",null,"")
    companion object{
        private val properties = arrayOf(News::newsId,News::title,News::content,News::newsImageUri,News::date,News::userId)
    }
    override fun toString() = kotlinToString(properties = News.properties)
}