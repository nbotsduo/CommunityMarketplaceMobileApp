package com.example.communitymarketplace.marketplace

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.communitymarketplace.R
import com.example.communitymarketplace.menu.MarketplaceActivity
import com.example.communitymarketplace.models.Community
import com.example.communitymarketplace.models.Product
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_search_product.*
import kotlinx.android.synthetic.main.dialog_searchfilter.*
import java.text.DecimalFormat

class SearchProductActivity : AppCompatActivity() {

    private val adapter = GroupAdapter<GroupieViewHolder>()
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var latitude:Double? = 0.00
    private var longitude:Double?=0.00
    private var distancesInput:Double?=0.00
    private var result:String?=null
    private var comType:String?=null
    private var comId:String?=null
    private var comName:String?=null
//    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
//    private lateinit var dialogBuilderDeleteProduct: MaterialAlertDialogBuilder
//    private lateinit var customAlertDialogView : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_product)

        comId =intent.getStringExtra("comId")
        result=intent.getStringExtra("search")

        setSupportActionBar(toolbarViewSearchProduct)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarViewSearchProduct.setNavigationOnClickListener {
            val intent= Intent(this, MarketplaceActivity::class.java)
            intent.putExtra("comId",comId)
            startActivity(intent)
            finish()
        }

//        materialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
//        dialogBuilderDeleteProduct = MaterialAlertDialogBuilder(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        toolbarViewSearchProduct.title="Search result for '$result'"
        edt_searchProductResult.setText(result)

        getItem(null)
        getCoordinate()
        Log.d("comId","$comId")
        getComName()
        recycler_searchProd.layoutManager= GridLayoutManager(applicationContext,2)
        recycler_searchProd.adapter=adapter

        txt_ViewSearchFilter.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        txt_ViewSearchFilter.setOnClickListener {
            popUpfilter()
        }

        btn_searchResult.setOnClickListener {
            val result = edt_searchProductResult.text.toString()
            val intent = Intent(this,SearchProductActivity::class.java)
            intent.putExtra("search",result)
            intent.putExtra("comId",comId)
            startActivity(intent)
        }

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ViewProductActivity::class.java)
            val row = item as SearchCard
            intent.putExtra("product",row.productObject)
            startActivity(intent)
        }
    }

    private fun getComName() {
//        var comName:String?=null

        val commDb = FirebaseDatabase.getInstance().getReference("/community")
        commDb.child(comId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val community=snapshot.getValue(Community::class.java)
                comName=community?.communityName
                Log.d("comId","$comName")
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
        Log.d("comId","$comId")
        Log.d("comId","$comName")
//        return comName
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
            latitude=it.latitude
            longitude=it.longitude
            Log.d("gps"," ${it.latitude},${it.longitude}")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun popUpfilter() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_searchfilter,null)
        val distanceDialog = dialogLayout.findViewById<EditText>(R.id.edt_filterDistances)
        distanceDialog.setText("0")
        val items = listOf("Any","$comName")
        val dialogCom = dialogLayout.findViewById<Spinner>(R.id.spinner_filterComType)


        val adapterSpin = ArrayAdapter(this, R.layout.list_statusproduct, items)
        dialogCom.adapter = adapterSpin

//        (register_filterComType.editText as AutoCompleteTextView).onItemClickListener =
//            AdapterView.OnItemClickListener { _, _, position, _ ->
//                comType = adapter.getItem(position)
//                Log.d("editProd","$comType")
//            }

        with(builder){
            setTitle("Search Filter")

            setPositiveButton("Search"){dialog,which ->
                if(distanceDialog.text.toString()==null){
                distancesInput=0.00
                }else{
                    distancesInput=distanceDialog.text.toString().toDouble()
                }
                comType=dialogCom.selectedItem.toString()
                adapter.clear()
                adapter.notifyDataSetChanged()
                getCoordinate()
                Log.d("testSearch","$longitude , $distancesInput")
                getItem(comType!!)
                txt_ViewSearchCommunity.text = "Community: $comType"
            }
            setNeutralButton("Cancel"){dialog,which->

            }
            setView(dialogLayout)
            show()
        }
    }

    private fun getItem(comType: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("/marketplace/product")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val product = snapshot.getValue(Product::class.java)
                val search =result.toString()
                val name = product?.productName.toString()
                if((longitude!! ==0.00)&&(latitude!! ==0.00)){
                    txt_ViewSearchCommunity.text = "Community : Any"
                    txt_ViewSearchDistances.text = "Distances : Any"
                    if(name.contains(search, ignoreCase = true)){
                        adapter.add(SearchCard(product!!))

                    }
                }else if(this@SearchProductActivity.comType == null){
                    var prodLat = product?.sell_Latitude
                    var prodLong = product?.sell_Longitude
                    txt_ViewSearchDistances.text = "Distances : $distancesInput km"
                    txt_ViewSearchCommunity.text = "Community : Any"
                    var distance = CalculationByDistance(latitude!!,longitude!!,prodLat!!,prodLong!!)
                    Log.d("testSearch","$distance , $distancesInput, $search")
                    Log.d("testSearch","${(distancesInput!! >=distance)&&(name.contains(search, ignoreCase = true))}")
                    if((distancesInput!! >=distance)&&(name.contains(search, ignoreCase = true))){
                        adapter.add(SearchCard(product!!))
                        Log.d("testSearch","done")
                    }
                    adapter.notifyDataSetChanged()
                }else if((distancesInput ==0.00)&&(this@SearchProductActivity.comType != null)){
                    txt_ViewSearchCommunity.text = "Community : $comType"
                    txt_ViewSearchDistances.text = "Distances : Any"
                    if((product?.communityId.equals(comId))&&(name.contains(search, ignoreCase = true))){
                        adapter.add(SearchCard(product!!))
                    }
                    adapter.notifyDataSetChanged()
                }
                else{
                    Log.d("testSearch","${this@SearchProductActivity.comType}")
                    var prodLat = product?.sell_Latitude
                    var prodLong = product?.sell_Longitude
                    txt_ViewSearchDistances.text = "Distances : $distancesInput km"
//                    txt_ViewSearchCommunity.text = "Community : $comName"
                    var distance = CalculationByDistance(latitude!!,longitude!!,prodLat!!,prodLong!!)
                    Log.d("testSearch","$distance , $distancesInput, $search")
                    Log.d("testSearch","${(distancesInput!! >=distance)&&(name.contains(search, ignoreCase = true))}")
                    if((product?.communityId.equals(comId))&&(distancesInput!! >=distance)&&(name.contains(search, ignoreCase = true))){
                        adapter.add(SearchCard(product!!))
                        Log.d("testSearch","done")
                    }
                    adapter.notifyDataSetChanged()
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

    fun CalculationByDistance(userLat: Double, userLong: Double,prodLat:Double,prodLong:Double): Double {
        val Radius = 6371 // radius of earth in Km
        val lat1: Double = userLat
        val lat2: Double = prodLat
        val lon1: Double = userLong
        val lon2: Double = prodLong
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

}