package com.example.communitymarketplace.marketplace.customer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.communitymarketplace.R
import com.example.communitymarketplace.marketplace.OrderCustomer
import com.example.communitymarketplace.menu.MarketplaceActivity
import com.example.communitymarketplace.models.Order
import com.example.communitymarketplace.models.Product
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_order_confirm_cust.*
import kotlinx.android.synthetic.main.activity_order_menu_cust.*
import kotlinx.android.synthetic.main.activity_order_menu_cust.toolbarOrderCustPay
import kotlinx.android.synthetic.main.activity_view_product_seller.*
import java.sql.Timestamp

class OrderConfirmCustActivity : AppCompatActivity() {

    private lateinit var order:Order
    private lateinit var product: Product
    private lateinit var orderCust:OrderCustomer
    var tot:Double?=0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirm_cust)

        setSupportActionBar(toolbarOrderConCust)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarOrderConCust.setNavigationOnClickListener {
            finish()
        }

        orderCust=intent.getParcelableExtra<OrderCustomer>("orderCust")!!
        product=intent.getParcelableExtra<Product>("product")!!

        setProductInfo()
        calculatePrice()

        btn_OrderConCust_back.setOnClickListener {
            finish()
        }
        btn_OrderConCust_order.setOnClickListener {
            saveToDB()
        }
    }

    private fun saveToDB() {
        val quantity = orderCust.orderQuantity
        val desc = orderCust.orderDescription
        val deliType=orderCust.order_DeliveryType
        val id =getRandomString(9)
        val dateTime = System.currentTimeMillis()
        val status="To Ship"
        val sellerId = product.sellerId
        val userId = Firebase.auth.currentUser?.uid
        val productId = product.productId

        order=
            Order(id,quantity,desc,dateTime,deliType,status,tot!!,null,sellerId,userId!!,productId)
        Log.d("conOrder",order.toString())

        val orderDb = FirebaseDatabase.getInstance().getReference("/marketplace/order/$id")
        orderDb.setValue(order)
            .addOnSuccessListener {
                updateStockRemaining(quantity,sellerId,productId)
                val builder = AlertDialog.Builder(this)
                //set title for alert dialog
                builder.setTitle("Order has been booked")
                //set message for alert dialog
                builder.setMessage("The order has been sent to the seller")
                builder.setIcon(android.R.drawable.ic_dialog_info)

                //performing positive action
                builder.setPositiveButton("OK") { dialogInterface, which ->
                    startActivity(Intent(this,MarketplaceActivity::class.java))
                }
                val alertDialog: AlertDialog = builder.create()
                // Set other dialog properties
                alertDialog.setCancelable(false)
                alertDialog.show()

            }

    }

    private fun updateStockRemaining(quantity: Int, sellerId: String, productId: String) {

        val stock = product.productQuantity
        val noOfStock = stock-quantity

        val prodManagement = FirebaseDatabase.getInstance().getReference("/marketplace/product/$productId/productManagement")
        val prod = FirebaseDatabase.getInstance().getReference("/marketplace/product/$productId")
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
                    }
            }

    }

    private fun calculatePrice() {
        val perItem:Double=product.price
        var noItem:Double = orderCust.orderQuantity.toDouble()
        val merchandise = perItem*noItem
        val deliPrice = product.deliveryCharges
        tot = merchandise+deliPrice
        txtView_OrderConCust_totOrderRM.text="RM "+ String.format("%.2f", merchandise)
        txtView_OrderConCust_totShipRM.text = "RM "+ String.format("%.2f", deliPrice)
        txtView_OrderConCust_totPaymentRM.text="RM "+ String.format("%.2f", tot)
    }

    private fun setProductInfo() {
        Glide.with(this).load(product.productImageUrl).signature(ObjectKey(System.currentTimeMillis())).into(img_OrderConCust)
        txt_OrderConCust_ProductName.text=product.productName.toString()
        txt_OrderConCust_ProductPrice.text="RM ${product.price.toString()}"
        register_OrderConCust_productQuantity.text= orderCust.orderQuantity.toString()
        txtView_OrderConCust_orderRequest.text=orderCust.orderDescription
        txtView_OrderConCust_deliveryType.text=orderCust.order_DeliveryType
    }

    fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}