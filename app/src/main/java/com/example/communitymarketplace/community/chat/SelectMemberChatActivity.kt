package com.example.communitymarketplace.community.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.communitymarketplace.R
import com.example.communitymarketplace.community.chat.ViewMessagesActivity.Companion.currentUser
import com.example.communitymarketplace.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

import kotlinx.android.synthetic.main.activity_select_member_chat.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class SelectMemberChatActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_member_chat)

        val comId =intent.getStringExtra("comId")
        Log.d("selectmember","$comId")
        recyclerview_newmessage.adapter=adapter
        recyclerview_newmessage.addItemDecoration(
            DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL)
        )

        setSupportActionBar(toolbarSelectMemberMessage)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarSelectMemberMessage.setNavigationOnClickListener {
            val intent = Intent(this, ViewMessagesActivity::class.java)
            intent.putExtra("comId",comId)
            startActivity(intent)
            finish()
        }

        fetchUsers(comId)
    }

    companion object{
        val USER_KEY = "USER_KEY"
    }

    //Groupie - https://github.com/lisawray/groupie
    private fun fetchUsers(comId: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("/community/$comId/members")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        if (user.username != currentUser?.username){
                            adapter.add(UserItem(user))
                        }
                    }
                }

                adapter.setOnItemClickListener{ item, view ->

                    val userItem = item as UserItem //Get username of contact

                    val intent = Intent(view.context,ChatLogActivity::class.java)
                    intent.putExtra(USER_KEY,item.user) //send username object
                    intent.putExtra("comId",comId)
                    startActivity(intent)
                    finish() //back to main screen
                }
                recyclerview_newmessage.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}

class UserItem(val user: User): Item<GroupieViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        //will be called in our list for each user object later on....
        viewHolder.itemView.username_textview_new_message.text = user.username
//Picasso - https://github.com/square/picasso
        Picasso.get().load(user.profileImageUri).into(viewHolder.itemView.imageview_new_message)
    }
}