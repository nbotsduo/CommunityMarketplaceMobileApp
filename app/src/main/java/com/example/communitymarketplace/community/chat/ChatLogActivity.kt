package com.example.communitymarketplace.community.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.communitymarketplace.R
import com.example.communitymarketplace.models.Chat
import com.example.communitymarketplace.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG = "Chatlog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    var toUser : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter=adapter
        val comId =intent.getStringExtra("comId")
        Log.d("chatlog","$comId")

        toUser = intent.getParcelableExtra<User>(SelectMemberChatActivity.USER_KEY)

        chatLog_userName.text = toUser?.username
        Picasso.get().load(toUser?.profileImageUri).into(chatLog_userImg)

        setSupportActionBar(toolbarViewUserMessage)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarViewUserMessage.setNavigationOnClickListener {
            val intent = Intent(this, ViewMessagesActivity::class.java)
            intent.putExtra("comId",comId)
            startActivity(intent)
            finish()
        }

        listenForMessages(comId)

        send_button_chat_log.setOnClickListener{
            performSendMessage(comId)
        }
    }

    //    Get message and sender/receiver image
    private fun listenForMessages(comId: String?) {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.userId
        val ref= FirebaseDatabase.getInstance().getReference("/community/$comId/chat/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(Chat::class.java)
                Log.d(TAG,"${chatMessage?.text}")

                if(chatMessage != null){
                    if(chatMessage.senderId == FirebaseAuth.getInstance().uid) {
                        val currentUser = ViewMessagesActivity.currentUser
                        adapter.add(ChatToItem(chatMessage.text,currentUser!!))
                    } else {
                        if(toUser == null) return
                        adapter.add(ChatFromItem(chatMessage.text,toUser!!))
                    }
                }
                recyclerview_chat_log.scrollToPosition(adapter.itemCount-1)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun performSendMessage(comId: String?) {
//        How do we sent message to firebase
//      Add fromIdLastSeen(timestamp) and toIdLastSeen(timestamp) for notification icon TBD
        val text = edittext_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(SelectMemberChatActivity.USER_KEY)
        val toId =user?.userId

        if(fromId == null) return
        if(toId == null) return

//        val reference = FirebaseDatabase.getInstance("https://kotlinmessenger-7dd8a-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("/messages").push()
//        Sender the message
        val reference = FirebaseDatabase.getInstance().getReference("/community/$comId/chat/user-messages/$fromId/$toId").push()
//        Receiver the message
        val toReference = FirebaseDatabase.getInstance().getReference("/community/$comId/chat/user-messages/$toId/$fromId").push()

        val chatMessage = Chat(reference.key!!,text,fromId,toId,System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                edittext_chat_log.text.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
            }
        toReference.setValue(chatMessage)

        val latestMessageFromRef =FirebaseDatabase.getInstance().getReference("/community/$comId/chat/latest-messages/$fromId/$toId")
        val latestMessageToRef =FirebaseDatabase.getInstance().getReference("/community/$comId/chat/latest-messages/$toId/$fromId")
        latestMessageFromRef.setValue(chatMessage)
        latestMessageToRef.setValue(chatMessage)
    }


}
class ChatFromItem(val text: String, val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_from_row.text=text

        //load user image
        val uri = user.profileImageUri
        val targetImageView = viewHolder.itemView.imageView_from_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}
class ChatToItem(val text: String, val user: User): Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.textView_to_row.text=text

        //load user image
        val uri = user.profileImageUri
        val targetImageView = viewHolder.itemView.imageView_to_row
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}