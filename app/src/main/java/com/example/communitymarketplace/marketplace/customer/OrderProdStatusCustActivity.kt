package com.example.communitymarketplace.marketplace.customer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.communitymarketplace.R
import com.example.communitymarketplace.marketplace.OrderCustomer
import com.example.communitymarketplace.marketplace.seller.ProductListSellerActivity
import com.example.communitymarketplace.menu.MarketplaceActivity
import com.example.communitymarketplace.models.Order
import com.example.communitymarketplace.models.Product
import com.example.communitymarketplace.models.ProductSellerInfo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_order_confirm_cust.*
import kotlinx.android.synthetic.main.activity_order_confirm_cust.toolbarOrderConCust
import kotlinx.android.synthetic.main.activity_order_prod_status_cust.*
import kotlinx.android.synthetic.main.activity_order_prod_status_cust.txt_OrderProdStatusCust_ProductName
import kotlinx.android.synthetic.main.activity_view_order_details.*
import kotlinx.android.synthetic.main.activity_view_product.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class OrderProdStatusCustActivity : AppCompatActivity() {

    private lateinit var order: Order
    private lateinit var product: Product
    private lateinit var orderCust: OrderCustomer
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_prod_status_cust)

        setSupportActionBar(toolbar_OrderProdStatusCust)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar_OrderProdStatusCust.setNavigationOnClickListener {
            finish()
        }

        order=intent.getParcelableExtra<Order>("order")!!
        product=intent.getParcelableExtra<Product>("product")!!
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        setProductStatusInfo()
        checkOrderStatus()
        getUserLocation()

        btn_OrderProdStatusCust_order.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            //set title for alert dialog
            builder.setTitle("Order Received")
            //set message for alert dialog
            builder.setMessage("Please confirm that the order has been delivered!")
            builder.setIcon(android.R.drawable.ic_dialog_alert)

            //performing positive action
            builder.setPositiveButton("Yes") { dialogInterface, which ->
                updateOrderStatus()
//                updateItemSold()
            }
            builder.setNegativeButton("No"){dialogInterface, which ->

            }
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getUserLocation() {
        val task: Task<Location> = fusedLocationProviderClient.lastLocation
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
        task.addOnSuccessListener {
//            txtView_currentCoordinate.text ="Current Coordinate: ${it.latitude},${it.longitude}"
            val latitude=it.latitude
            val longitude=it.longitude

            btn_OrderProdStatusCust_navi.setOnClickListener {
                val uri = Uri.parse("http://maps.google.com/maps?saddr=${latitude},${longitude} &daddr=${product.sell_Latitude},${product.sell_Longitude} &dirflg=w")
                val gmmIntentUri =
                    Uri.parse("google.navigation:q=${product.sell_Latitude},${product.sell_Longitude}&mode=w")
                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }

        }
    }

    private fun updateOrderStatus() {
        val status = "Item has been delivered"
        val id = order.orderId
        order.orderStatus=status

        val delivered = mapOf<String,String>(
            "orderStatus" to status
        )

        val orderDb = FirebaseDatabase.getInstance().getReference("/marketplace/order/$id")

        orderDb.updateChildren(delivered)
            .addOnSuccessListener {
                btn_OrderProdStatusCust_navi.visibility=View.GONE
                btn_OrderProdStatusCust_order.visibility=View.GONE
                txtView_OrderCustPay_OrderReceived.visibility=View.VISIBLE
                updateItemSold()
            }
    }

    private fun updateItemSold() {

        val tot = order.order_totPrice
        val deliCharge = product.deliveryCharges
        val merchPrice = tot - deliCharge
        val id =order.productId
//        var prodMgt:ProductSellerInfo? = null

        Log.d("prodMgt","$id")

        val ref = FirebaseDatabase.getInstance().getReference("/marketplace/product/$id/")
            .child("productManagement").addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val prodMgt = snapshot.getValue(ProductSellerInfo::class.java)
                    Log.d("prodMgt",prodMgt.toString())
                    val totSale = prodMgt?.totSales
                    val purchased = prodMgt?.purchasedProduct
                    val totDeli = prodMgt?.totDeliveryCharges

                    val orderSale = totSale?.plus(merchPrice)
                    val orderPurchased = purchased?.plus(order.orderQuantity)
                    val orderDeli =totDeli?.plus(deliCharge)

                    val doubleProduct = mapOf<String,Double>(
                        "totSales" to orderSale!!,
                        "totDeliveryCharges" to orderDeli!!
                    )

                    val intProduct = mapOf<String,Int>(
                        "purchasedProduct" to orderPurchased!!
                    )

                    val prodMgtDb = FirebaseDatabase.getInstance().getReference("/marketplace/product/$id/productManagement")
                    prodMgtDb.updateChildren(doubleProduct)
                        .addOnSuccessListener {
                            Log.d("prodMgt","double")
                            prodMgtDb.updateChildren(intProduct).addOnSuccessListener {
                                Log.d("prodMgt","int")
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

//        Log.d("prodMgt",prodMgt.toString())

    }

    private fun checkOrderStatus() {
        val status = order.orderStatus
        if(status == "Item has been delivered"){
            btn_OrderProdStatusCust_navi.visibility=View.GONE
            btn_OrderProdStatusCust_order.visibility=View.GONE
        }else{
            txtView_OrderCustPay_OrderReceived.visibility=View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setProductStatusInfo() {

        val tot = order.order_totPrice
        val deliCharge = product.deliveryCharges
        val merchPrice = tot - deliCharge

        Glide.with(this).load(product.productImageUrl).signature(ObjectKey(System.currentTimeMillis())).into(img_OrderProdStatusCust)
        txt_OrderProdStatusCust_ProductName.text = product.productName
        txt_OrderProdStatusCust_ProductPrice.text="RM "+product.price.toString()
        register_OrderProdStatusCust_productQuantity.text=order.orderQuantity.toString()
        txtView_OrderProdStatusCust_orderRequest.text=order.orderDescription
        txtView_OrderProdStatusCust_deliveryType.text=order.order_DeliveryType
        txtView_OrderProdStatusCust_totPaymentRM.text= "RM "+ String.format("%.2f", tot)
        txtView_OrderProdStatusCust_totOrderRM.text="RM "+ String.format("%.2f",merchPrice)
        txtView_OrderProdStatusCust_totShipRM.text="RM "+ String.format("%.2f", deliCharge)
        txtView_OrderProdStatusCust_orderDate.text=convertLongToTime(order.orderDateTime!!)
    }
    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd MMMM yyyy hh:mm a", Locale.UK)
        return format.format(date)
    }
}