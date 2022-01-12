package com.example.communitymarketplace.models

import android.os.Parcelable
import au.com.console.kassava.kotlinToString
import kotlinx.android.parcel.Parcelize

@Parcelize
class Order(
    val orderId:String,
    val orderQuantity:Int,
    val orderDescription:String,
    val orderDateTime: Long?,
    val order_DeliveryType:String,
    var orderStatus:String,
    val order_totPrice:Double,
    val orderReceived_TimeDate: Long?,
    val sellerId:String,
    val customerId:String,
    val productId:String) :Parcelable{
constructor():this("",0,"",null,"","",0.00,null,"","","")
    companion object{
        private val properties = arrayOf(Order::orderId,Order::orderQuantity,Order::orderDescription,Order::orderDateTime,Order::order_DeliveryType,Order::orderStatus,Order::order_totPrice,Order::orderReceived_TimeDate
        ,Order::sellerId,Order::customerId,Order::productId)

    }
    override fun toString() = kotlinToString(properties = Order.properties)
}