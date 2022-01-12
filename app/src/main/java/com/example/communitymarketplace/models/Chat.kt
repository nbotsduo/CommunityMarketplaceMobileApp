package com.example.communitymarketplace.models

class Chat(val chatId:String,val text:String,val senderId:String,val receiverId:String,val timestamp:Long) {
    constructor():this("","","","",-1)
}