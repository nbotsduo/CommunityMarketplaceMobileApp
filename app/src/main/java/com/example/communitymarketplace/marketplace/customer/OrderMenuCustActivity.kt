package com.example.communitymarketplace.marketplace

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.communitymarketplace.marketplace.customer.OrderConfirmCustActivity
import com.example.communitymarketplace.models.Product
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_order_menu_cust.*


class OrderMenuCustActivity : AppCompatActivity() {

    private lateinit var product: Product
    var comName: String? = null
    var price: Double? = 0.00
    private lateinit var order: OrderCustomer
    var delivery: String? = null

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.communitymarketplace.R.layout.activity_order_menu_cust)

        product = intent.getParcelableExtra<Product>("product")!!
        comName = intent.getStringExtra("comName")
        register_OrderCustPay_productQuantity.setText("1")
//        register_OrderCustPay_productQuantity.setFilters(arrayOf<InputFilter>(InputFilterMinMax("1", "12")))
        setSupportActionBar(toolbarOrderCustPay)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarOrderCustPay.setNavigationOnClickListener {
            finish()
        }

        setProductInfo()
        rd_Delivery()
//        calculatePrice()
//        rd_OrderCustPay_deliveryType.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { _, checkedId ->
//            when (checkedId) {
//                R.id.radio_OrderCustPay_delivery1 -> {
//                    Log.d("orderCust", "1")
//                    delivery = "Delivery to customer house"
//                }
//                R.id.radio_OrderCustPay_delivery2 -> {
//                    Log.d("orderCust", "2")
//                    delivery = "Customer take the item at the seller place"
//                }
//            }
//        })
        val deliType = product.deliveryType
        when (deliType) {
            "Delivery to customer house" -> {
                delivery=deliType
            }
            "Customer take the item at the seller place" -> {
                delivery=deliType
            }
            "Either at customer house or customer take the item at the seller place" -> {

            }
        }

        rd_OrderCustPay_deliveryType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                com.example.communitymarketplace.R.id.radio_OrderCustPay_delivery1 -> {
                    Log.d("orderCust", "1")
                    delivery = "Delivery to customer house"
                }
                com.example.communitymarketplace.R.id.radio_OrderCustPay_delivery2 -> {
                    Log.d("orderCust", "2")
                    delivery = "Customer take the item at the seller place"
                }
            }
        }

        btn_OrderCustPay_order.setOnClickListener {
            val item: Boolean = checkNoItem()
            val nullCheck: Boolean = nullCheck()
            Log.d("noItem", "$item")
            if (!item) {
                val builder = AlertDialog.Builder(this)
                //set title for alert dialog
                builder.setTitle("Alert")
                //set message for alert dialog
                builder.setMessage("The number of item exceed the available quantity of item")
                builder.setIcon(android.R.drawable.ic_dialog_alert)

                //performing positive action
                builder.setPositiveButton("OK") { dialogInterface, which -> }
                val alertDialog: AlertDialog = builder.create()
                // Set other dialog properties
                alertDialog.setCancelable(false)
                alertDialog.show()
            } else if (!nullCheck) {
                val builder = AlertDialog.Builder(this)
                //set title for alert dialog
                builder.setTitle("Alert")
                //set message for alert dialog
                builder.setMessage("Please enter quantity or select delivery method")
                builder.setIcon(android.R.drawable.ic_dialog_alert)

                //performing positive action
                builder.setPositiveButton("OK") { dialogInterface, which -> }
                val alertDialog: AlertDialog = builder.create()
                // Set other dialog properties
                alertDialog.setCancelable(false)
                alertDialog.show()
            } else {
                order = getOrder()
                val intent = Intent(this, OrderConfirmCustActivity::class.java)
                intent.putExtra("product", product)
                intent.putExtra("orderCust", order)
                startActivity(intent)
            }
        }
    }

    private fun getOrder(): OrderCustomer {
        var orderCust: OrderCustomer? = null

        val quantity = register_OrderCustPay_productQuantity.text.toString().toInt()
        var desc = edt_OrderCustPay_orderRequest.text.toString()

        if(edt_OrderCustPay_orderRequest.text.toString().isEmpty()){
            desc="No Request"
        }

        Log.d("orderCust", "$quantity")
        Log.d("orderCust", "$desc")
        Log.d("orderCust", "$delivery")
        orderCust = OrderCustomer(quantity, desc, delivery!!)
        Log.d("orderCust", orderCust.toString())
        return orderCust
    }


    private fun nullCheck(): Boolean {
        var check: Boolean = true
        val noItem = register_OrderCustPay_productQuantity.text.toString().toInt()
        if (noItem == null) {
            check = false
        }
        if (delivery==null){check=false}
        return check
    }

    private fun rd_Delivery() {
        val deliType = product.deliveryType
        when (deliType) {
            "Delivery to customer house" -> {
                radio_OrderCustPay_delivery2.isEnabled = false
                radio_OrderCustPay_delivery1.isChecked = true
            }
            "Customer take the item at the seller place" -> {
                radio_OrderCustPay_delivery2.isChecked = true
                radio_OrderCustPay_delivery1.isEnabled = false
            }
            "Either at customer house or customer take the item at the seller place" -> {

            }
        }
    }

    private fun checkNoItem(): Boolean {
        var checkNoItem: Boolean = true
        val noItem = register_OrderCustPay_productQuantity.text.toString().toInt()
        if (noItem > product.productQuantity) {
            checkNoItem = false
        }
        return checkNoItem
    }

    private fun setProductInfo() {
        txt_OrderCustPay_ProductName.text = product.productName.toString()
        txt_OrderCustPay_ProductPrice.text = "RM ${product.price.toString()}"
        Glide.with(this).load(product.productImageUrl)
            .signature(ObjectKey(System.currentTimeMillis())).into(img_OrderCustPay)
        txtView_OrderCustPay_maxItem.text = product.productQuantity.toString() + " items available"
    }
}

@Parcelize
class OrderCustomer(
    val orderQuantity: Int,
    val orderDescription: String,
    val order_DeliveryType: String,
) : Parcelable {
    constructor() : this(0, "", "")
}