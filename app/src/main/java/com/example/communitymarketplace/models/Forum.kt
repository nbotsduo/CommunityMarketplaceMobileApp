package com.example.communitymarketplace.models

import android.os.Parcelable
import au.com.console.kassava.kotlinToString
import kotlinx.android.parcel.Parcelize

@Parcelize
class Forum(
    val forumId: String,
    val category: String,
    val title: String,
    val comID: String,
    val createdId: String,
    val dateCreated: Long,
    val lastPost:Long
):Parcelable {
    constructor():this("","","","","",0,0)
    companion object{
        private val properties = arrayOf(Forum::forumId,Forum::category,Forum::title,Forum::comID,Forum::createdId,Forum::dateCreated,Forum::lastPost)
    }
    override fun toString() = kotlinToString(properties = properties)

}