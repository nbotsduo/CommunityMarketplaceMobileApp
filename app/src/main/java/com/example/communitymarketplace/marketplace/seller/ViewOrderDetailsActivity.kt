package com.example.communitymarketplace.marketplace.seller

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
import com.example.communitymarketplace.models.Order
import com.example.communitymarketplace.models.Product
import com.example.communitymarketplace.models.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_order_prod_status_cust.*
import kotlinx.android.synthetic.main.activity_order_prod_status_cust.toolbar_OrderProdStatusCust
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_view_order_details.*
import java.text.SimpleDateFormat
import java.util.*

class ViewOrderDetailsActivity : AppCompatActivity() {

    private lateinit var order: Order
    private lateinit var product: Product
    private lateinit var customer:User
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_order_details)

        setSupportActionBar(toolbar_orderDetailSeller)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar_orderDetailSeller.setNavigationOnClickListener {
            finish()
        }

        order=intent.getParcelableExtra<Order>("order")!!
        product=intent.getParcelableExtra<Product>("product")!!
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        setProductStatusInfo()
        getCustomerInfo()
        checkOrderStatus()
        getCustLocation()
        getUserLocation()

        btn_orderDetailSeller_order.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            //set title for alert dialog
            builder.setTitle("Order Delivered")
            //set message for alert dialog
            builder.setMessage("Please confirm that the order has been deliver to the customer!")
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

    private fun getCustLocation() {
        val userDb = FirebaseDatabase.getInstance().getReference("/users")
        userDb.child(order.customerId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                customer = snapshot.getValue(User::class.java)!!
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
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

            btn_orderDetailSeller_navi.setOnClickListener {
                val uri = Uri.parse("http://maps.google.com/maps?saddr=${latitude},${longitude} &daddr=${product.sell_Latitude},${product.sell_Longitude} &dirflg=w")
                val gmmIntentUri =
                    Uri.parse("google.navigation:q=${customer.gpsLatitude},${customer.gpsLongitude}&mode=w")
                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }

        }
    }

    private fun updateOrderStatus() {
        val status = "Seller has delivered the items"
        val id = order.orderId
        order.orderStatus=status

        val delivered = mapOf<String,String>(
            "orderStatus" to status
        )

        val orderDb = FirebaseDatabase.getInstance().getReference("/marketplace/order/$id")

        orderDb.updateChildren(delivered)
            .addOnSuccessListener {
                btn_orderDetailSeller_navi.visibility=View.GONE
                btn_orderDetailSeller_order.visibility=View.GONE
                txtView_orderDetailSeller_OrderReceived.visibility=View.VISIBLE
            }
    }

    private fun checkOrderStatus() {
        val status = order.orderStatus
        if((status == "Seller has delivered the items")||(status == "Item has been delivered")){
            btn_orderDetailSeller_navi.visibility= View.GONE
            btn_orderDetailSeller_order.visibility= View.GONE
        }else{
            txtView_orderDetailSeller_OrderReceived.visibility= View.GONE
        }
    }

    private fun getCustomerInfo() {
        val custId = order.customerId

        val userDb = FirebaseDatabase.getInstance().getReference("/users")
        userDb.child(custId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                txt_orderDetailSeller_CustName.text = "Name : "+user?.name
                txt_orderDetailSeller_CustPhone.text = "Phone : "+user?.phone
                txt_orderDetailSeller_CustAddress.text=user?.address
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun setProductStatusInfo() {
        val tot = order.order_totPrice
        val deliCharge = product.deliveryCharges
        val merchPrice = tot - deliCharge

        Log.d ("orderseller",order.toString())
        Log.d ("orderseller",product.toString())
        Glide.with(this).load(product.productImageUrl).signature(ObjectKey(System.currentTimeMillis())).into(img_orderDetailSeller)
        txt_orderDetailSeller_ProductName.text = product.productName
        txt_orderDetailSeller_ProductPrice.text="RM "+product.price.toString()
        register_orderDetailSeller_productQuantity.text=order.orderQuantity.toString()
        txtView_orderDetailSeller_orderRequest.text=order.orderDescription
        txtView_orderDetailSeller_deliveryType.text=order.order_DeliveryType
        txtView_orderDetailSeller_totPaymentRM.text= "RM "+ String.format("%.2f", tot)
        txtView_orderDetailSeller_totOrderRM.text="RM "+ String.format("%.2f",merchPrice)
        txtView_orderDetailSeller_totShipRM.text="RM "+ String.format("%.2f", deliCharge)
        txtView_orderDetailSeller_orderDate.text="Order Date : "+convertLongToTime(order.orderDateTime!!)

    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd MMMM yyyy hh:mm a", Locale.UK)
        return format.format(date)
    }
}