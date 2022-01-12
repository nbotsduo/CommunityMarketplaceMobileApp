package com.example.communitymarketplace.community.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.communitymarketplace.R
import com.example.communitymarketplace.menu.CommunityActivity
import com.example.communitymarketplace.models.Chat
import com.example.communitymarketplace.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_view_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class ViewMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser: User? = null
    }

    private val adapter = GroupAdapter<GroupieViewHolder>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_messages)

        val comId =intent.getStringExtra("comId")
        Log.d("viewmassage","$comId")

        recyclerview_latest_messages.adapter = adapter
        recyclerview_latest_messages.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        setSupportActionBar(toolbarViewMessage)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarViewMessage.setNavigationOnClickListener {
            val intent = Intent(this, CommunityActivity::class.java)
            intent.putExtra("comId",comId)
            startActivity(intent)
            finish()
        }

        //set item click listener on your adapter
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)
            intent.putExtra("comId",comId)
            ///get chat partner user
            val row = item as LatestMessageRow
            intent.putExtra(SelectMemberChatActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }

        floatingActionButton.setOnClickListener {
            val intent=Intent(this,SelectMemberChatActivity::class.java)
            intent.putExtra("comId",comId)
            startActivity(intent)
        }

        listenForLatestMessages(comId)

        fetchCurrentUser()

    }

    val latestMessagesMap = HashMap<String, Chat>() //hashmap

    //Display latest message on the main activity screen
    private fun listenForLatestMessages(comId: String?) {
        val fromId = FirebaseAuth.getInstance().uid
        Log.d(ChatLogActivity.TAG,"$fromId")
        val ref = FirebaseDatabase.getInstance().getReference("/community/$comId/chat/latest-messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(Chat::class.java) ?: return
                Log.d(ChatLogActivity.TAG,"${chatMessage?.text}")
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages(comId!!)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(Chat::class.java) ?: return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages(comId!!)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun refreshRecyclerViewMessages(comId: String) {

        adapter.clear()
        val sortedMap = HashMap<Long, Chat>()
        //new hashMap
        latestMessagesMap.forEach {
            sortedMap[0 - it.value.timestamp] = it.value
            //add to new hashMap with 0 - timestamp as key
        }
        sortedMap.toSortedMap().values.forEach {
            adapter.add(LatestMessageRow(it,comId))
        }
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref =
            FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                Log.d("latest Messages", "Current user ${currentUser?.username}")
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


}
class LatestMessageRow(val chatMessage: Chat,val comId: String?) : Item<GroupieViewHolder>() {

    var chatPartnerUser : User?= null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.message_textview_latest_message.text = chatMessage.text
        //Get username and password of the person who chatting with
        val chatPartnerId: String
        if (chatMessage.senderId == FirebaseAuth.getInstance().uid) {
            chatPartnerId = chatMessage.receiverId
        } else {
            chatPartnerId = chatMessage.senderId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/community/$comId/members/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)
                viewHolder.itemView.username_textview_latest_message.text = chatPartnerUser?.username
                val targetImageView = viewHolder.itemView.imageview_latest_message
                Picasso.get().load(chatPartnerUser?.profileImageUri).into(targetImageView)
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        })
    }
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}


