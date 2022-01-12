package com.example.communitymarketplace.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize

class User (val userId : String, val username: String,val name:String, val email: String, val phone: String, val address:String, val gpsLatitude: Double, val gpsLongitude:Double,val biography:String, val profileImageUri:String,val imageId:String, val communityId:String ):Parcelable {
    constructor(): this("","","","","","",00.0000,00.0000,"","","","")
}