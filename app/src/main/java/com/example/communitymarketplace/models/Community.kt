package com.example.communitymarketplace.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Community (val communityId:String,val communityName: String,val address:String,val postcode:String,val state:String,val gpsCoordinate:String):
    Parcelable {
    constructor():this("","","","","","")
}