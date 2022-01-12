package com.example.communitymarketplace.marketplace.seller

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.communitymarketplace.R
import com.example.communitymarketplace.models.Community
import com.example.communitymarketplace.models.Product
import com.example.communitymarketplace.models.ProductSellerInfo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_view_product_seller.*
import kotlinx.android.synthetic.main.dialog_addproduct.*
import kotlinx.android.synthetic.main.dialog_addproduct.view.*
import java.text.SimpleDateFormat
import java.util.*

class ViewProductSellerActivity : AppCompatActivity() {

    private lateinit var product:Product
    private var productId:String?=null
    private lateinit var productManagement:ProductSellerInfo
    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    private lateinit var dialogBuilderDeleteProduct: MaterialAlertDialogBuilder
    private lateinit var customAlertDialogView : View
    private lateinit var deleteAlertDialogView : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_product_seller)

        setSupportActionBar(toolbarSellerProductInfo)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarSellerProductInfo.setNavigationOnClickListener {
            val intent= Intent(this, ProductListSellerActivity::class.java)
            intent.putExtra("comId",product.communityId)
            startActivity(intent)
            finish()
        }

        product=intent.getParcelableExtra<Product>("product")!!
        productManagement=intent.getParcelableExtra<ProductSellerInfo>("prodMgr")!!
        Log.d("textViewProd","${product.toString()}")

        getProductData()
        getProductManagementData()

        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
        dialogBuilderDeleteProduct = MaterialAlertDialogBuilder(this)

        btn_addStock.setOnClickListener {
            customAlertDialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_addproduct,null,false)
            launchAddStockDialogBox()
        }
        btn_SellerProdMen.setOnClickListener {
            val popup = PopupMenu(this,btn_SellerProdMen)
            popup.menuInflater.inflate(R.menu.view_product_seller_menu,popup.menu)

            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

                when (item!!.itemId) {
                    R.id.editProduct -> {
                        val intent=Intent(this,EditProductActivity::class.java)
                        intent.putExtra("product",product)
                        intent.putExtra("productMgt",productManagement)
                        startActivity(intent)
                    }
                    R.id.deleteProduct -> {
                        val builder = AlertDialog.Builder(this)
                        //set title for alert dialog
                        builder.setTitle("Delete ${product.productName}")
                        //set message for alert dialog
                        builder.setMessage("Delete this item will remove the item from the Marketplace and your product list")
                        builder.setIcon(android.R.drawable.ic_dialog_alert)

                        //performing positive action
                        builder.setPositiveButton("Yes"){dialogInterface, which ->
                            deleteProduct()
                        }
                        //performing cancel action
                        builder.setNeutralButton("Cancel"){dialogInterface , which ->
//                            Toast.makeText(applicationContext,"clicked cancel\n operation cancel",Toast.LENGTH_LONG).show()
                        }
                        //performing negative action
                        builder.setNegativeButton("No"){dialogInterface, which ->
//                            Toast.makeText(applicationContext,"clicked No",Toast.LENGTH_LONG).show()
                        }
                        // Create the AlertDialog
                        val alertDialog: AlertDialog = builder.create()
                        // Set other dialog properties
                        alertDialog.setCancelable(false)
                        alertDialog.show()
                    }

                }

                true
            })
            popup.show()
        }
    }

    private fun deleteProduct(){
        val name = product.productName
        val del = FirebaseDatabase.getInstance().getReference("/marketplace/product/${product.productId}")
        del.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this,"$name has been removed",Toast.LENGTH_LONG).show()
                startActivity(Intent(this,ProductListSellerActivity::class.java))
            }
    }

    @SuppressLint("SetTextI18n")
    private fun launchAddStockDialogBox() {
        val prodId=product.productId
        val addStock = customAlertDialogView.findViewById<View>(R.id.addStock)
        materialAlertDialogBuilder.setView(customAlertDialogView)
            .setTitle("Add Stocks")
            .setPositiveButton("Add"){ dialog, _ ->
                val availableStock = productManagement.remainingStock
                val addNoStock: String =addStock.addNumberStocks.text.toString()
                Log.d("addStock","$addNoStock")
                val noOfStock:Int = addNoStock.toInt()+availableStock.toInt()

                val prodManagement = FirebaseDatabase.getInstance().getReference("/marketplace/product/$prodId/productManagement")
                val prod = FirebaseDatabase.getInstance().getReference("/marketplace/product/$prodId")
                val addStocks = mapOf<String,Int>(
                    "remainingStock" to noOfStock
                )
                val addStocksProduct = mapOf<String,Int>(
                    "productQuantity" to noOfStock
                )
                prodManagement.updateChildren(addStocks)
                    .addOnSuccessListener {
                        prod.updateChildren(addStocksProduct)
                            .addOnSuccessListener {
                                Toast.makeText(this,"$noOfStock has been added into inventory",Toast.LENGTH_SHORT).show()
                                txtProductSellerInfoItemRemain.text= "Item Available : $noOfStock"
                            }
                    }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getProductManagementData() {
        txtProductSellerInfoItemSold.text="Item Sold : "+ productManagement.purchasedProduct.toString()
        txtProductSellerInfoItemRemain.text="Item Available :"+productManagement.remainingStock.toString()
        txtProductSellerInfoItemTotSole.text="Total Sales : RM"+productManagement.totSales.toString()
        txtProductSellerInfoItemTotDeli.text="Total Delivery Charges Collected :"+productManagement.totDeliveryCharges.toString()
    }

    private fun getProductData() {
        toolbarSellerProductInfo.title=product.productName
        Glide.with(this).load(this.product.productImageUrl).signature(ObjectKey(System.currentTimeMillis())).into(img_ProductSellerInfo)
        txtProductSellerInfoName.text= this.product.productName
        txtProductSellerInfoPrice.text= "RM "+this.product.price.toString()
        txtProductSellerInfoItemDesc.text="Product Description :\n"+ this.product.productDesciption+"\n"
        txtProductSellerInfoItemDeliType.text="Delivery Option :\n"+ this.product.deliveryType+"\n"
        txtProductSellerInfoItemDeliCharge.text="Delivery Charge : RM"+ this.product.deliveryCharges.toString()+"\n"
        txtProductSellerInfoItemLocation.text="Selling Location :\n"+ this.product.prodAddress+"\n"
        val date = convertLongToTime(product.dateAdded!!)
        txtProductSellerInfoItemDate.text= "Date Added :$date"
        getComName(product.communityId)
        txtProductSellerInfoItemStatus.text="Product Availability : "+ this.product.status
    }
    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd MMMM yyyy hh:mm a", Locale.UK)
        return format.format(date)
    }

    private fun getComName(communityId: String) {
        Log.d("conName","$communityId")
        val comDb = FirebaseDatabase.getInstance().getReference("/community")

        comDb.child(communityId).addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                val community = snapshot.getValue(Community::class.java)
                val comName=community?.communityName
                txtProductSellerInfoItemCommunity.text= "Community Name : $comName"
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }
}