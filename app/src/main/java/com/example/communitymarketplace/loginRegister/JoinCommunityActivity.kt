package com.example.communitymarketplace.loginRegister

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.SearchView
import com.example.communitymarketplace.databinding.ActivityJoinCommunityBinding
import com.example.communitymarketplace.models.Community
import com.example.communitymarketplace.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_join_community.*

class JoinCommunityActivity : AppCompatActivity() {

    lateinit var binding : ActivityJoinCommunityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var commList : ArrayList<String> = arrayListOf()

        var comm : ArrayList<Community> = arrayListOf()

        setSupportActionBar(toolbarViewJoinComm)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarViewJoinComm.setNavigationOnClickListener {
            startActivity(Intent(this, CommunityIntroductionActivity::class.java))
            finish()
        }


        val ref = FirebaseDatabase.getInstance().getReference("community")

        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val community = snapshot.getValue(Community::class.java)
                val communityName = community?.communityName
                commList.add(communityName!!)
                comm.add(community)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

//        user.add("Abhay")
//        user.add("Rusell")
//        user.add("Vladimir")
//        user.add("Zack")
//        val user = arrayOf("Abhay","Joseph","Maria","Avni","Apoorva","Chris","David","Kaira","Dwayne","Christopher",
//            "Jim","Russel","Donald","Brack","Vladimir")

        val comAdapter : ArrayAdapter<String> = ArrayAdapter(
            this,android.R.layout.simple_list_item_1,
            commList
        )

        binding.commList.adapter = comAdapter;

        binding.searchViewCom.setOnQueryTextListener(object  : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchViewCom.clearFocus()
                if (commList.contains(query)){

                    comAdapter.filter.filter(query)

                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                comAdapter.filter.filter(newText)
                return false
            }


        })
        binding.commList.setOnItemClickListener { parent, view, position, id ->
            val name = comAdapter.getItem(position)
//            Toast.makeText(this,"$name", Toast.LENGTH_SHORT).show()
            Log.d("test","$name")
            updateUserComDb(name,comm)

        }
    }

    private fun updateUserComDb(name: String?, comm: ArrayList<Community>) {

        var comId: String? = ""

//        Update user profile
        var count = 0
        while (comm.size > count) {
            if (name.equals(comm[count].communityName)) {
                comId = comm[count].communityId
                break
            }
            count++
        }
        Log.d("test", "$comId")

        var userId = FirebaseAuth.getInstance().uid
        val userDb = FirebaseDatabase.getInstance().getReference("users")
        val user = mapOf<String, String>(
            "communityId" to comId!!
        )
        userDb.child(userId!!).updateChildren(user)
//        var userClass: User? = null

        //repar
        userDb.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userClass= snapshot.getValue(User::class.java)
                updateCommDb(userClass!!,userId,comId)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Join",error.message)
            }

        })

//      register user into the community


    }

    private fun updateCommDb(userClass: User, userId: String, comId: String){
        var commDb = FirebaseDatabase.getInstance().getReference("/community/$comId/members/$userId")
        val userComm = mapOf<String, String>(
            "userId" to userId,
            "name" to userClass.name,
            "username" to userClass.username,
            "profileImageUri" to userClass.profileImageUri
        )
        commDb.setValue(userComm)

        startActivity(Intent(this,CongratsAccActivity::class.java))
    }

}