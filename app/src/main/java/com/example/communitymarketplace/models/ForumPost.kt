package com.example.communitymarketplace.models

import android.os.Parcelable
import au.com.console.kassava.kotlinToString
import kotlinx.android.parcel.Parcelize

@Parcelize
class ForumPost (
    val postId:String,
    val content:String,
    val forumId:String,
    val userId:String,
    val timestamp:Long
        ): Parcelable {
            constructor():this("","","","",0)
    companion object{
        private val properties = arrayOf(ForumPost::postId,ForumPost::content,ForumPost::forumId,ForumPost::userId,ForumPost::timestamp)
    }
    override fun toString() = kotlinToString(properties = properties)
}