package com.example.communitymarketplace.marketplace.customer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.communitymarketplace.R
import com.example.communitymarketplace.menu.MarketplaceActivity
import com.example.communitymarketplace.models.Order
import com.example.communitymarketplace.models.Product
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_order_list_cust.*
import kotlinx.android.synthetic.main.row_order_cust.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.communitymarketplace.community.chat.LatestMessageRow
import com.example.communitymarketplace.marketplace.seller.ProductListSellerActivity
import com.example.communitymarketplace.marketplace.seller.ViewProductSellerActivity
import com.example.communitymarketplace.models.Chat


class OrderListCustActivity : AppCompatActivity() {

    private lateinit var product:Product
    private lateinit var order: Order
    private val adapter=GroupAdapter<GroupieViewHolder>()
    val latestCustOrder = HashMap<String,Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list_cust)

        setSupportActionBar(toolbar_orderListCust)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar_orderListCust.setNavigationOnClickListener {
            val intent = Intent(this, MarketplaceActivity::class.java)
            startActivity(intent)
            finish()
        }

        recycler_orderListCust.adapter=adapter
        fetchCustOrder()
//        adapter.removeGroupAtAdapterPosition(-1)
//        val linearLayoutManager = LinearLayoutManager(this)
//        linearLayoutManager.reverseLayout = true
//        linearLayoutManager.stackFromEnd = true
//        recycler_orderListCust.layoutManager=linearLayoutManager

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, OrderProdStatusCustActivity::class.java)
            val row = item as OrderListCustomer
            intent.putExtra("product",row.productObject)
            intent.putExtra("order",row.orderObject)
            startActivity(intent)
        }
    }
    private fun fetchCustOrder() {
        val userId = Firebase.auth.currentUser?.uid

        val orderDb = FirebaseDatabase.getInstance().getReference("/marketplace/order/")
        orderDb.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                order=snapshot.getValue(Order::class.java)!!
                if(order.customerId==userId){
//                    fetchProdDetail(order)
                    latestCustOrder[snapshot.key!!] = order
                    refreshRecyclerViewOrderCust()
                }
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

    private fun fetchProdDetail(order: Order) {
        val productDb = FirebaseDatabase.getInstance().getReference("/marketplace/product/")
        productDb.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                product=snapshot.getValue(Product::class.java)!!
                if(product.productId==order.productId){
//                    adapter.add(OrderListCustomer(order,product))
                }
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

    private fun refreshRecyclerViewOrderCust() {

        adapter.clear()
        val sortedMap = HashMap<Long, Order>()
        //new hashMap
        latestCustOrder.forEach {
            sortedMap[0 - it.value.orderDateTime!!] = it.value
            //add to new hashMap with 0 - timestamp as key
        }
        sortedMap.toSortedMap().values.forEach {
            adapter.add(OrderListCustomer(it))
        }
    }

}

class OrderListCustomer(private val order: Order) : Item<GroupieViewHolder>(){

    var productObject:Product?=null
    var orderObject:Order?=null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        orderObject=order

        val productDb = FirebaseDatabase.getInstance().getReference("/marketplace/product/")
        productDb.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val product=snapshot.getValue(Product::class.java)!!
                if(product.productId==order.productId){
                    productObject=product
                    viewHolder.itemView.txt_orderListCust_prodName.text = product.productName
                    Picasso.get().load(product.productImageUrl).memoryPolicy(MemoryPolicy.NO_CACHE).into(viewHolder.itemView.img_orderListCust_img)
                }
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

        viewHolder.itemView.txt_orderListCust_price.text="Order Total : RM"+order.order_totPrice.toString()
        viewHolder.itemView.txt_orderListCust_status.text="Status : "+order.orderStatus
    }

    override fun getLayout()=R.layout.row_order_cust

}