package com.example.communitymarketplace.marketplace.seller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.example.communitymarketplace.R
import com.example.communitymarketplace.models.Product
import com.example.communitymarketplace.models.ProductSellerInfo
import com.example.communitymarketplace.models.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_create_new_product.*
import java.util.*

class CreateNewProductActivity : AppCompatActivity() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var selectedPhotoUri: Uri? = null
    var userLatitude: Double? = null
    var userLongitude: Double? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var comId:String?=null
    var prodAddress:String?=null
    val userId = FirebaseAuth.getInstance().uid
    val filename = getRandomString(8).toString()
    var storageUrl :String? =null
    var prodLongitude:Double?=null
    var prodLatitude:Double? = null
    var delivery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_product)

        setSupportActionBar(toolbarAddNewProduct)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarAddNewProduct.setNavigationOnClickListener {
            startActivity(Intent(this, ProductListSellerActivity::class.java))
            finish()
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        btn_image_add_product.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }



        txtView_status_sellingCoordinates.visibility= View.GONE
        register_prodAddress.visibility=View.GONE
        getComID()


        getCoordinate()
        rd_sellingCoordinate.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_homeCoordinate -> {
                    prodLatitude = userLatitude
                    prodLongitude = userLongitude
                    register_prodAddress.visibility=View.GONE
                    txtView_status_sellingCoordinates.visibility= View.VISIBLE
                    getAddress(prodLatitude!!,prodLongitude!!)
                }
                R.id.radio_customLocation -> {
                    prodLatitude = latitude
                    prodLongitude = longitude
                    register_prodAddress.visibility=View.GONE
                    txtView_status_sellingCoordinates.visibility= View.VISIBLE
                    getAddress(prodLatitude!!,prodLongitude!!)
                }
//                R.id.radio_customLocationAddress ->{
//                    register_prodAddress.visibility=View.VISIBLE
//                    txtView_status_sellingCoordinates.visibility= View.GONE
//                    prodAddress = edt_prodAddress.text.toString()
//                }

            }
        })
        rd_deliveryType.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_delivery -> {
                    Log.d("orderCust", "2")
                    delivery ="Delivery to customer house"
                }
                R.id.radio_delSellerPlace -> {
                    Log.d("orderCust", "2")
                    delivery ="Customer take the item at the seller place"
                }
                R.id.radio_deliveryBoth -> {
                    delivery ="Either at customer house or customer take the item at the seller place"
                }
            }
        })
        btn_createProduct.setOnClickListener {
            uploadProductImageToStorage()
        }




    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // proceed and check what the selected image was....

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            //Circle Image View - https://github.com/hdodenhof/CircleImageView
            image_add_newProduct.setImageBitmap(bitmap)

            btn_image_add_product.alpha = 0f

            val currentLayout = btn_createProduct.layoutParams as ConstraintLayout.LayoutParams
            currentLayout.topToBottom = com.example.communitymarketplace.R.id.image_add_newProduct
            btn_createProduct.layoutParams = currentLayout
        }
    }

    private fun getCoordinate() {
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
            latitude = it.latitude
            longitude = it.longitude
            Log.d("gps", " ${it.latitude},${it.longitude}")
//            getAddress(it.latitude, it.longitude)
        }
    }


    private fun getComID() {
        val id = FirebaseAuth.getInstance().uid
        val userDb = FirebaseDatabase.getInstance().getReference("/users")
        val commDb = FirebaseDatabase.getInstance().getReference("/community")

        userDb.child(id!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                comId = user?.communityId.toString()
                userLatitude = user?.gpsLatitude
                userLongitude = user?.gpsLongitude
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    private fun uploadProductImageToStorage() {
        if (selectedPhotoUri == null) return

        val ref = FirebaseStorage.getInstance().getReference("/marketplace/product/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("img","${it.toString()},$filename")
                    storageUrl=it.toString()
                    saveProductIntoDatabase(it.toString())
                }
            }
    }

    private fun saveProductIntoDatabase(imgUrl: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/marketplace/product/$filename")

        val name = edt_productName.text.toString()
        val desc = edt_productDesc.text.toString()
        val quantity = edt_productQuantity.text.toString().toInt()
        val price = edt_productPrice.text.toString().toDouble()

        var delCharge = edt_deliveryCharges.text.toString()
        var imageId = filename
        var imageUrl = imgUrl
        val status="Available"
        val date = System.currentTimeMillis()
        var deliveryCharge:Double?=0.00

        if(TextUtils.isEmpty(delCharge)){
           deliveryCharge= 0.00
        }else{
            deliveryCharge=delCharge.toString().toDouble()
        }
//        prodAddress="$prodLatitude , $prodLongitude"


        rd_deliveryType.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_delivery -> {
                    delivery ="Delivery to customer house"
                }
                R.id.radio_delSellerPlace -> {
                    delivery ="Customer take the item at the seller place"
                }
                R.id.radio_deliveryBoth -> {
                    delivery ="Either at customer house or customer take the item at the seller place"
                }
            }
        })
        rd_sellingCoordinate.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_homeCoordinate -> {
                    prodLatitude = userLatitude
                    prodLongitude = userLongitude
                    getAddress(prodLatitude!!,prodLongitude!!)
                }
                R.id.radio_customLocation -> {
                    prodLatitude = latitude
                    prodLongitude = longitude
                    getAddress(prodLatitude!!,prodLongitude!!)
                }
//                R.id.radio_customLocationAddress ->{
//                    prodAddress = edt_prodAddress.text.toString()
//                    convertAddressToCoordinate()
//                    prodLatitude=latitude
//                    prodLongitude=longitude
//                }
            }
        })

        Log.d("prodDetail","$filename")
        Log.d("prodDetail","$name")
        Log.d("prodDetail","$desc")
        Log.d("prodDetail","$quantity")
        Log.d("prodDetail","$delivery")
        Log.d("prodDetail","$deliveryCharge")
        Log.d("prodDetail","$prodLatitude")
        Log.d("prodDetail","$prodLongitude")
        Log.d("prodDetail","$prodAddress")
        Log.d("prodDetail","$price")
        Log.d("prodDetail","$status")
        Log.d("prodDetail","$imageUrl")
        Log.d("prodDetail","$imageId")
        Log.d("prodDetail","$userId")
        Log.d("prodDetail","$comId")

        val product=Product(filename,name,desc,quantity,delivery!!,deliveryCharge,prodLatitude!!,prodLongitude!!,prodAddress!!,price,status,imageUrl!!,imageId,userId!!,comId!!,date)

        Log.d("testAddProd","${product.toString()}")
        ref.setValue(product)
            .addOnSuccessListener {

            }
            .addOnFailureListener {
                Log.d("dbProduct","${it.message}")
            }

        val prodManagement = FirebaseDatabase.getInstance().getReference("/marketplace/product/$filename/productManagement")
        val productInfo = ProductSellerInfo(quantity,0,0.00,0.00)
        prodManagement.setValue(productInfo)
            .addOnSuccessListener {
                Toast.makeText(this,"Product $name has been added", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ProductListSellerActivity::class.java)
                intent.putExtra("comId",comId)
                startActivity(intent)
            }
    }

    @SuppressLint("SetTextI18n")
    private fun getAddress(latitude: Double, longitude: Double) {
        val geocoder: Geocoder = Geocoder(this, Locale.getDefault())

        val addresses: List<Address> = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        val address: String =
            addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        txtView_status_sellingCoordinates.text = "Current Address:\n$address"
        prodAddress=address
    }

    private fun convertAddressToCoordinate(){
        val geocoder: Geocoder = Geocoder(this, Locale.getDefault())
        val address: List<Address> = geocoder.getFromLocationName(prodAddress,1)
        if(address.size>0){
            latitude=address.get(0).latitude
            longitude=address.get(0).longitude
        }
    }

    fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}