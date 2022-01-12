package com.example.communitymarketplace.models

import android.os.Parcelable
import au.com.console.kassava.kotlinToString
import kotlinx.android.parcel.Parcelize

@Parcelize
class ProductSellerInfo(val remainingStock : Int,val purchasedProduct:Int,val totSales:Double,val totDeliveryCharges:Double) :Parcelable {
    constructor():this(0,0,0.00,0.00)
    companion object{
        private val properties= arrayOf(ProductSellerInfo::remainingStock,ProductSellerInfo::purchasedProduct,ProductSellerInfo::totSales,ProductSellerInfo::totDeliveryCharges)
    }
    override fun toString() = kotlinToString(properties = properties)
}