package com.example.communitymarketplace.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import au.com.console.kassava.kotlinToString
@Parcelize
class Product(
    val productId: String,
    val productName: String,
    val productDesciption: String,
    val productQuantity: Int,
    val deliveryType: String,
    val deliveryCharges: Double,
    val sell_Latitude : Double,
    val sell_Longitude:Double,
    val prodAddress:String,
    val price: Double,
    val status: String,
    val productImageUrl: String,
    val imageid:String,
    val sellerId: String,
    val communityId: String,
    val dateAdded:Long?
) :Parcelable{
    constructor() : this("", "", "", 0, "", 0.00, 0.0000, 0.00, "",0.00, "", "", "","","",null)
    companion object{
        private val properties = arrayOf(Product::productId,Product::productName,Product::productDesciption,Product::productQuantity,Product::deliveryType,Product::deliveryCharges,Product::sell_Latitude,Product::sell_Longitude,
            Product::prodAddress,Product::price,Product::status,Product::productImageUrl,Product::imageid,Product::sellerId,Product::communityId,Product::dateAdded)
    }
    override fun toString() = kotlinToString(properties = properties)
}