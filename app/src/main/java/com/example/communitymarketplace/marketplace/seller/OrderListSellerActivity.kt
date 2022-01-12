package com.example.communitymarketplace.marketplace.seller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.communitymarketplace.R
import com.example.communitymarketplace.marketplace.customer.OrderListCustomer
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
import kotlinx.android.synthetic.main.activity_order_list_seller.*
import kotlinx.android.synthetic.main.row_order_cust.view.*

class OrderListSellerActivity : AppCompatActivity() {

    private lateinit var product: Product
    private lateinit var order: Order
    private val adapter= GroupAdapter<GroupieViewHolder>()
    val itemProduct :MutableList<Product> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list_seller)

        setSupportActionBar(toolbar_orderListSeller)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar_orderListSeller.setNavigationOnClickListener {
            finish()
        }

        recycler_orderListSeller.adapter = adapter

//        fetchProductInfo()
        fetchCustOrder()

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ViewOrderDetailsActivity::class.java)
            val row = item as OrderListSeller
            intent.putExtra("product",row.productObject)
            intent.putExtra("order",row.orderObject)
            startActivity(intent)
        }
    }

    private fun fetchProductInfo(order: Order) {
        val ref = FirebaseDatabase.getInstance().getReference("/marketplace/product")
        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val product = snapshot.getValue(Product::class.java)
//                itemProduct.add(product!!)
//                Log.d("itemProduct1",itemProduct.toString())
                if(product?.productId==order.productId){
                    refreshRecyclerViewOrder(product)
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

    val latestOrder = HashMap<String,Order>()
    private fun fetchCustOrder() {
        val userId = Firebase.auth.currentUser?.uid

        val orderDb = FirebaseDatabase.getInstance().getReference("/marketplace/order/")
        orderDb.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                order=snapshot.getValue(Order::class.java)!!
                if(order.sellerId==userId){
                    latestOrder[snapshot.key!!] =order
                    Log.d("seler",order.toString())
//                    adapter.add(OrderListSeller(order))
                    fetchProductInfo(order)
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

    private fun refreshRecyclerViewOrder(product: Product) {
        adapter.clear()
        val sortedMap = HashMap<Long,Order>()
        latestOrder.forEach {
            sortedMap[0-it.value.orderDateTime!!] = it.value
        }
        sortedMap.toSortedMap().values.forEach {
            adapter.add(OrderListSeller(it,product))
        }
    }
}

class OrderListSeller(private val order: Order, private val product: Product):
    Item<GroupieViewHolder>(){
    var productObject:Product?=null
    var orderObject:Order?=null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        productObject=product
        orderObject=order

        viewHolder.itemView.txt_orderListCust_prodName.text = product.productName
        viewHolder.itemView.txt_orderListCust_price.text="Order Total : RM"+order.order_totPrice.toString()
        viewHolder.itemView.txt_orderListCust_status.text="Status : "+order.orderStatus
        Picasso.get().load(product.productImageUrl).memoryPolicy(MemoryPolicy.NO_CACHE).into(viewHolder.itemView.img_orderListCust_img)
    }

    override fun getLayout()=R.layout.row_order_cust

}