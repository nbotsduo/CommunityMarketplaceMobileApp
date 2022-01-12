package com.example.communitymarketplace.marketplace.seller

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
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
import kotlinx.android.synthetic.main.activity_edit_product.*
import java.util.*


class EditProductActivity : AppCompatActivity() {

    private lateinit var product:Product
    private lateinit var productMgt:ProductSellerInfo
    var selectedPhotoUri: Uri? = null
    private lateinit var user:User
    var status:String?=null

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var prodAddress:String?=null
    var prodLatitude:Double?=0.00
    var prodLongitude:Double?=0.00
    private var delivery:String?=null
    private var photoStorageUri:String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product)

        product= intent.getParcelableExtra<Product>("product")!!

        productMgt=intent.getParcelableExtra<ProductSellerInfo>("productMgt")!!

        setSupportActionBar(toolbarEditProduct)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarEditProduct.setNavigationOnClickListener {
            val intent = Intent(this, ViewProductSellerActivity::class.java)
            intent.putExtra("product",product)
            intent.putExtra("prodMgr",productMgt)
            startActivity(intent)
            finish()
        }

        toolbarEditProduct.title = "Edit ${product.productName}"
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

//        txtView_status_sellingCoordinates.visibility= View.GONE
        register_prodAddress.visibility= View.GONE

        getUserData()
        getProductData()
        rd_editSellingCoordinate.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                when(checkedId){
                    R.id.radio_editHomeCoordinate ->{
                        txtView_status_sellingCoordinates.text = user.address
                        prodAddress=user.address
                        prodLatitude=user.gpsLatitude
                        prodLongitude=user.gpsLongitude
                    }
                    R.id.radio_edit_sameCoordinate->{
                        txtView_status_sellingCoordinates.text = product.prodAddress
                        prodAddress=product.prodAddress
                        prodLatitude=product.sell_Latitude
                        prodLongitude=product.sell_Longitude
                    }
                    R.id.radio_editCustomLocation->{
                        getCoordinate()
                    }
                }
            })
//        editData()
        edt_Status?.setText(this.product.status)
        val items = listOf("Available","Restock","Not Available")
        val adapter = ArrayAdapter(this, R.layout.list_statusproduct, items)
        (register_productStatus.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        (register_productStatus.editText as AutoCompleteTextView).onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                status = adapter.getItem(position)
                Log.d("editProd","$status")
            }

        rd_editDeliveryType.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_edit_delivery -> {
                    delivery ="Delivery to customer house"
                    Log.d("radio","$delivery")
                }
                R.id.radio_edit_delSellerPlace -> {
                    delivery ="Customer take the item at the seller place"
                    Log.d("radio","$delivery")
                }
                R.id.radio_edit_deliveryBoth -> {
                    delivery ="Either at customer house or customer take the item at the seller place"
                    Log.d("radio","$delivery")
                }
            }
        })

        btn_image_edit_product.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        btn_editProduct.setOnClickListener {
            updateProductImage()
            updateProductInfo()
        }

    }

    private fun updateProductInfo() {
        val prodId = product.productId
        val prodName = edt_editProductName.text.toString()
        val prodDesc = edt_editProductDesc.text.toString()
        val prodQuantity = edt_editProductQuantity.text.toString().toInt()
        val prodPrice= edt_editProductPrice.text.toString().toDouble()
        val deliCharges = edt_editDeliveryCharges.text.toString().toDouble()
        val imageId=product.imageid
        if(photoStorageUri==null){photoStorageUri=product.productImageUrl}
        if(status == null){status=product.status}
        if(prodAddress== null){
            prodAddress=product.prodAddress
            prodLatitude=product.sell_Latitude
            prodLongitude=product.sell_Longitude
        }
        if(delivery==null){delivery=product.deliveryType}

        Log.d("prodDetail","$prodId")
        Log.d("prodDetail","$prodName")
        Log.d("prodDetail","$prodDesc")
        Log.d("prodDetail","$prodQuantity")
        Log.d("prodDetail","$delivery")
        Log.d("prodDetail","$deliCharges")
        Log.d("prodDetail","$prodLatitude")
        Log.d("prodDetail","$prodLongitude")
        Log.d("prodDetail","$prodAddress")
        Log.d("prodDetail","$prodPrice")
        Log.d("prodDetail","$status")
        Log.d("prodDetail","$photoStorageUri")
        Log.d("prodDetail","$imageId")
        Log.d("prodDetail","${product.sellerId}")
        Log.d("prodDetail","${product.communityId}")

        val productEdit = Product(prodId,prodName,prodDesc,prodQuantity,delivery!!,deliCharges,prodLatitude!!,prodLongitude!!,prodAddress!!,prodPrice,status!!,photoStorageUri!!,imageId,product.sellerId,product.communityId,product.dateAdded)
        val productEditString = mapOf<String,String>(
            "productName" to prodName,
            "productDesciption" to prodDesc,
            "deliveryType" to delivery!!,
            "prodAddress" to prodAddress!!,
            "status" to status!!
        )
        val productEditDouble= mapOf<String,Double>(
            "deliveryCharges" to deliCharges,
            "sell_Latitude" to prodLatitude!!,
            "sell_Longitude" to prodLongitude!!,
            "price" to prodPrice
        )
        val productEditInt= mapOf<String,Int>(
            "productQuantity" to prodQuantity
        )
        val prodDb = FirebaseDatabase.getInstance().getReference("/marketplace/product/$prodId")

        prodDb.updateChildren(productEditString)
        prodDb.updateChildren(productEditDouble)
        prodDb.updateChildren(productEditInt)

        val prodSold = edt_productSold.text.toString().toInt()
        val prodRemain = edt_productRemaining.text.toString().toInt()
        val prodSale =edt_totSale.text.toString().toDouble()
        val totDeliCharge =  edt_DeliCharge.text.toString().toDouble()

        val prodMgr = ProductSellerInfo(prodRemain,prodSold,prodSale,totDeliCharge)

        val prodMgrInt= mapOf<String,Int>(
            "remainingStock" to prodRemain,
            "purchasedProduct" to prodSold
        )

        val prodMgrDouble = mapOf<String, Double>(
            "totSales" to prodSale,
            "totDeliveryCharges" to totDeliCharge
        )

        val mgr = FirebaseDatabase.getInstance().getReference("/marketplace/product/$prodId/productManagement")

        mgr.updateChildren(prodMgrInt)
        mgr.updateChildren(prodMgrDouble)

        Toast.makeText(this,"$prodName has been saved",Toast.LENGTH_SHORT).show()
        val intent = Intent(this,ViewProductSellerActivity::class.java)
        intent.putExtra("product",productEdit)
        intent.putExtra("prodMgr",prodMgr)
        startActivity(intent)
//        Log.d("productEdit","${productEdit.toString()}")

    }


    private fun updateProductImage() {
        if (selectedPhotoUri == null) return
        val filename = product.imageid

        val ref = FirebaseStorage.getInstance().getReference("/marketplace/product/$filename")
        val delRef = FirebaseStorage.getInstance().getReference("marketplace/product").child("$filename")

        delRef.delete()
            .addOnSuccessListener {
                ref.putFile(selectedPhotoUri!!)
                    .addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener {
                            photoStorageUri=it.toString()
                        }
                    }
                    .addOnFailureListener {
                        Log.d("image", "Failed to upload image to storage: ${it.message}")
                    }
            }
            .addOnFailureListener {
                Log.d("imageDel","${it.message}")
            }
    }

    private fun editData() {
        rd_editSellingCoordinate.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                when(checkedId){
                    R.id.radio_editHomeCoordinate ->{
                        txtView_status_sellingCoordinates.text = user.address
                        prodAddress=user.address
                        prodLatitude=user.gpsLatitude
                        prodLongitude=user.gpsLongitude
                    }
                    R.id.radio_edit_sameCoordinate->{
                        txtView_status_sellingCoordinates.text = product.prodAddress
                        prodAddress=product.prodAddress
                        prodLatitude=product.sell_Latitude
                        prodLongitude=product.sell_Longitude
                    }
                    R.id.radio_editCustomLocation->{
                        getCoordinate()
                    }
                }
            })

        /*if(radio_editHomeCoordinate.isChecked){
            txtView_status_sellingCoordinates.text = user.address
        }*/
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
            prodLatitude = it.latitude
            prodLongitude = it.longitude
            Log.d("gps", " ${it.latitude},${it.longitude}")
            getAddress()
        }
    }

    private fun getAddress() {
        val geocoder: Geocoder = Geocoder(this, Locale.getDefault())

        val addresses: List<Address> = geocoder.getFromLocation(
            prodLatitude!!,
            prodLongitude!!,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        val address: String =
            addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        txtView_status_sellingCoordinates.text = "Current Address:\n$address"
        prodAddress=address
    }

    private fun getUserData() {
        var sellerId = FirebaseAuth.getInstance().uid
        val ref= FirebaseDatabase.getInstance().getReference("users")
        ref.child(sellerId!!).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                user= snapshot.getValue(User::class.java)!!
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)

            image_edit_newProduct.setImageBitmap(bitmap)
            btn_image_edit_product.alpha = 0f

            val currentLayout = v_edtProduct1.layoutParams as ConstraintLayout.LayoutParams
            currentLayout.topToBottom = com.example.communitymarketplace.R.id.image_edit_newProduct
            v_edtProduct1.layoutParams = currentLayout
        }
    }

    private fun getProductData() {
//        btn_image_edit_product.alpha = 0f
//        Glide.with(this).load(product.productImageUrl).signature(ObjectKey(System.currentTimeMillis())).into(image_edit_newProduct)
        Log.d("productObject","${product.toString()}")
        edt_editProductName.setText(product.productName)
        edt_editProductDesc.setText(product.productDesciption)
        edt_editProductQuantity.setText(product.productQuantity.toString())
        edt_editProductPrice.setText(product.price.toString())
        edt_editDeliveryCharges.setText(product.deliveryCharges.toString())
        edt_productSold.setText(productMgt.purchasedProduct.toString())
        edt_productRemaining.setText(productMgt.remainingStock.toString())
        edt_totSale.setText(productMgt.totSales.toString())
        edt_DeliCharge.setText(productMgt.totDeliveryCharges.toString())

        val delMethod= this.product.deliveryType
//        when(delMethod){
//            "Delivery to customer house" -> radio_edit_delivery.isChecked=true
//            "Customer take the item at the seller place" -> radio_edit_delSellerPlace.isChecked
//            "Either at customer house or customer take the item at the seller place" -> radio_edit_deliveryBoth.isChecked
//            else -> false
//        }

        Log.d("edtProd",delMethod)
        if(delMethod == "Delivery to customer house"){
            radio_edit_delivery.isChecked=true
            radio_edit_delSellerPlace.isChecked=false
            radio_edit_deliveryBoth.isChecked=false
        }
        if(delMethod == "Customer take the item at the seller place"){
            radio_edit_delivery.isChecked=false
            radio_edit_delSellerPlace.isChecked=true
            radio_edit_deliveryBoth.isChecked=false
        }
        if(delMethod=="Either at customer house or customer take the item at the seller place"){
            radio_edit_delivery.isChecked=false
            radio_edit_delSellerPlace.isChecked=false
            radio_edit_deliveryBoth.isChecked=true
        }



//        radio_edit_sameCoordinate.isChecked = true
        txtView_status_sellingCoordinates.text = this.product.prodAddress



    }
}