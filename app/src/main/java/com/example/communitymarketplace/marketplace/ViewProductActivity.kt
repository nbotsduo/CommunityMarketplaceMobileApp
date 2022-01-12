package com.example.communitymarketplace.marketplace

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.communitymarketplace.R
import com.example.communitymarketplace.models.Community
import com.example.communitymarketplace.models.Product
import com.example.communitymarketplace.models.ProductSellerInfo
import com.example.communitymarketplace.models.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_view_product.*
import kotlinx.android.synthetic.main.activity_view_product_seller.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class ViewProductActivity : AppCompatActivity() {

    private lateinit var product: Product
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var comName:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_product)

        product=intent.getParcelableExtra<Product>("product")!!

        setSupportActionBar(toolbarViewProductInfo)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarViewProductInfo.setNavigationOnClickListener {
//            val intent= Intent(this, MarketplaceActivity::class.java)
//            intent.putExtra("comId",product.communityId)
//            startActivity(intent)
            finish()
        }

        toolbarViewProductInfo.title=product.productName
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setProductInfo()
        linear_sellerInfo.setOnClickListener {
            val intent =Intent(this,SellerProfileActivity::class.java)
            intent.putExtra("product",product)
            intent.putExtra("comName",comName)
            startActivity(intent)
        }

        getUserLocation()

        btn_addOrder.setOnClickListener {
            val intent =Intent(this,OrderMenuCustActivity::class.java)
            intent.putExtra("product",product)
            intent.putExtra("comName",comName)
            startActivity(intent)
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

            val distances= CalculationByDistance(latitude, longitude)
            val df = DecimalFormat()

            if(distances<1){
                df.maximumFractionDigits = 2
                txt_ViewProdDistance.text=df.format(distances*100).toString() +" m"
            }else{
                df.maximumFractionDigits = 2
                txt_ViewProdDistance.text=df.format(distances).toString()+" km"
            }

            txt_ViewProdDistance.setOnClickListener {
                val uri = Uri.parse("http://maps.google.com/maps?saddr=${latitude},${longitude} &daddr=${product.sell_Latitude},${product.sell_Longitude} &dirflg=w")
                val gmmIntentUri =
                    Uri.parse("google.navigation:q=${product.sell_Latitude},${product.sell_Longitude}&mode=w")
                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }

        }
    }

    fun CalculationByDistance(userLat: Double, userLong: Double): Double {
        val Radius = 6371 // radius of earth in Km
        val lat1: Double = userLat
        val lat2: Double = product.sell_Latitude
        val lon1: Double = userLong
        val lon2: Double = product.sell_Longitude
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = (Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + (Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2)))
        val c = 2 * Math.asin(Math.sqrt(a))
        val valueResult = Radius * c
        val km = valueResult / 1
        val newFormat = DecimalFormat("####")
        val kmInDec = Integer.valueOf(newFormat.format(km))
        val meter = valueResult % 1000
        val meterInDec = Integer.valueOf(newFormat.format(meter))
        Log.d(
            "Radius Value", "" + valueResult + "   KM  " + kmInDec
                    + " Meter   " + meterInDec
        )
        Log.d("distance","$lat1,$lon1 & $lat2,$lon2")
        return Radius * c
    }


    private fun setProductInfo() {
        Glide.with(this).load(product.productImageUrl).signature(ObjectKey(System.currentTimeMillis())).into(img_ViewProductInfo)
        txt_ViewProdName.text=product.productName
        txt_ViewProdPrice.text="RM ${product.price.toString()}"
        txt_ViewProdDesc.text="Product Description :\n${product.productDesciption} \n"
        txt_ViewProdDeliType.text="Delivery Option : ${product.deliveryType}"
        txt_ViewProdDeliCharge.text="Delivery Charge : RM ${product.deliveryCharges.toString()}"
        txt_ViewProdLocation.text="Selling Location : \n${product.prodAddress}\n"
        txt_ViewProdStatus.text="Product Availability : ${product.status}"
        txt_ViewProdDate.text="Date Added : ${convertLongToTime(product.dateAdded!!)}"
        getSellerInfo()
        getComName(product.communityId)
        getProdMgr()
    }

    private fun getProdMgr() {
        FirebaseDatabase.getInstance().getReference("/marketplace/product/${product.productId}/")
            .child("productManagement").addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val prodMgr = snapshot.getValue(ProductSellerInfo::class.java)
                    txt_ViewProdSold.text="Item Sold :"+prodMgr?.purchasedProduct.toString()
                    txt_ViewProdRemain.text="Item Available :"+prodMgr?.remainingStock.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun getSellerInfo() {
        val userDb = FirebaseDatabase.getInstance().getReference("/users")
        userDb.child(product.sellerId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                txt_ViewSellerName.text=user?.name
                Glide.with(this@ViewProductActivity).load(user?.profileImageUri).signature(ObjectKey(System.currentTimeMillis())).into(img_ViewSellerProfile)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
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
                comName=community?.communityName
                txt_ViewProdCommunity.text= "Community Name : $comName"
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

//    fun CalculationByDistance(StartP: LatLng, EndP: LatLng): Double {
//        val Radius = 6371 // radius of earth in Km
//        val lat1: Double = StartP.latitude
//        val lat2: Double = EndP.latitude
//        val lon1: Double = StartP.longitude
//        val lon2: Double = EndP.longitude
//        val dLat = Math.toRadians(lat2 - lat1)
//        val dLon = Math.toRadians(lon2 - lon1)
//        val a = (Math.sin(dLat / 2) * Math.sin(dLat / 2)
//                + (Math.cos(Math.toRadians(lat1))
//                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
//                * Math.sin(dLon / 2)))
//        val c = 2 * Math.asin(Math.sqrt(a))
//        val valueResult = Radius * c
//        val km = valueResult / 1
//        val newFormat = DecimalFormat("####")
//        val kmInDec: Int = Integer.valueOf(newFormat.format(km))
//        val meter = valueResult % 1000
//        val meterInDec: Int = Integer.valueOf(newFormat.format(meter))
//        Log.i(
//            "Radius Value", "" + valueResult + "   KM  " + kmInDec
//                    + " Meter   " + meterInDec
//        )
//        return Radius * c
//    }

}