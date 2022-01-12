package com.example.communitymarketplace.menu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.example.communitymarketplace.R
import com.example.communitymarketplace.marketplace.SearchCard
import com.example.communitymarketplace.marketplace.SearchProductActivity
import com.example.communitymarketplace.marketplace.ViewProductActivity
import com.example.communitymarketplace.marketplace.customer.OrderListCustActivity
import com.example.communitymarketplace.marketplace.seller.SellerMainActivity
import com.example.communitymarketplace.models.News
import com.example.communitymarketplace.models.Order
import com.example.communitymarketplace.models.Product
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_community.bottom_navigation
import kotlinx.android.synthetic.main.activity_marketplace.*

class MarketplaceActivity : AppCompatActivity() {

    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marketplace)

        val comId =intent.getStringExtra("comId")
        //bottom navigation
        bottomNavigation()
        Log.d("Markerplace","$comId")

        img_more.setOnClickListener {
            val intent = Intent(this,SellerMainActivity::class.java)
            intent.putExtra("comId",comId)
            startActivity(intent)
        }

        getItem()
        recycler_featureItem.layoutManager=GridLayoutManager(applicationContext,2)
        recycler_featureItem.adapter=adapter

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ViewProductActivity::class.java)
            val row = item as SearchCard
            intent.putExtra("product",row.productObject)
            startActivity(intent)
        }
        btn_search.setOnClickListener {
            val result = edt_searchProduct.text.toString()
            val intent = Intent(this,SearchProductActivity::class.java)
            intent.putExtra("search",result)
            intent.putExtra("comId",comId)
            startActivity(intent)
        }

        img_cart.setOnClickListener {
            startActivity(Intent(this, OrderListCustActivity::class.java))
        }
    }

    private fun getItem() {
        val ref = FirebaseDatabase.getInstance().getReference("/marketplace/product")
        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val product = snapshot.getValue(Product::class.java)
                adapter.add(SearchCard(product!!))
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
    }


    private fun bottomNavigation() {
        bottom_navigation.selectedItemId = R.id.marketplace

        bottom_navigation.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.community ->{
                    startActivity(Intent(this,CommunityActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }
                R.id.marketplace ->{
                    true
                }
                R.id.profile ->{
                    startActivity(Intent(this,ProfileActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }
                else -> false
            }
        }
    }
}