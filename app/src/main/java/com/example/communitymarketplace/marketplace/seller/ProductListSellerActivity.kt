package com.example.communitymarketplace.marketplace.seller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.communitymarketplace.R
import com.example.communitymarketplace.menu.MarketplaceActivity
import com.example.communitymarketplace.models.Product
import com.example.communitymarketplace.models.ProductSellerInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_product_list_seller.*
import kotlinx.android.synthetic.main.activity_product_list_seller.floatingActionButton
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.row_product_seller.view.*

class ProductListSellerActivity : AppCompatActivity() {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    private val sellerId = FirebaseAuth.getInstance().uid
    private var product:Product?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list_seller)
        setSupportActionBar(toolbarListProductSeller)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarListProductSeller.setNavigationOnClickListener {
            val intent = Intent(this, MarketplaceActivity::class.java)
            startActivity(intent)
            finish()
        }

        floatingActionButton.setOnClickListener {
            startActivity(Intent(this,CreateNewProductActivity::class.java))
        }
        recycler_ProdListSeller.adapter=adapter
        fetchProductSellerData()
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this,ViewProductSellerActivity::class.java)
            val row = item as ProductListSellerItem
            intent.putExtra("product",row.productObject)
            intent.putExtra("prodMgr",row.prodManager)
            startActivity(intent)
        }

    }

    private fun fetchProductSellerData() {
        val ref = FirebaseDatabase.getInstance().getReference("/marketplace/product/")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                product = snapshot.getValue(Product::class.java)!!
                Log.d("testProduct","${product?.productName}")
                if(product?.sellerId == sellerId){
                    val productId=product?.productId
                    Log.d("prodId","$productId")
                    fetchProdManager(productId, product!!)
//                    adapter.add(ProductListSellerItem(product!!))

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

    private fun fetchProdManager(productId: String?, product: Product) {
        val ref = FirebaseDatabase.getInstance().getReference("/marketplace/product/$productId/")
            .child("productManagement").addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val prodMgr = snapshot.getValue(ProductSellerInfo::class.java)
                    adapter.add(ProductListSellerItem(product,prodMgr))
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

}
class ProductListSellerItem(private val product: Product,private val prodMgr: ProductSellerInfo?) : Item<GroupieViewHolder>(){
    var productObject:Product?=null
    var prodManager:ProductSellerInfo?=null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        productObject=product
        prodManager=prodMgr
        Log.d("listOrder","${product.toString()}")
        Log.d("listOrder","${prodMgr.toString()}")
        viewHolder.itemView.txtProductSellerName.text=product.productName
        Picasso.get().load(product.productImageUrl).memoryPolicy(MemoryPolicy.NO_CACHE).into(viewHolder.itemView.imgProductSeller)
        viewHolder.itemView.txtProductSellerSold.text="Item sold: "+prodMgr?.purchasedProduct.toString()
        viewHolder.itemView.txtProductSellerRemaining.text="Item remaining: "+prodMgr?.remainingStock.toString()
    }

    override fun getLayout()=R.layout.row_product_seller

}}