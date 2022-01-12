package com.example.communitymarketplace.settings

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
import android.util.Log
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.communitymarketplace.R
import com.example.communitymarketplace.menu.ProfileActivity
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
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_edit_profile.edt_Biography
import kotlinx.android.synthetic.main.activity_edit_profile.edt_Phone
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_setup_account.*
import java.util.*


class EditProfileActivity : AppCompatActivity() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var selectedPhotoUri: Uri? = null
    var userId = FirebaseAuth.getInstance().uid
    var latitude:Double? =null
    var longitude:Double?=null
    var imageId:String?=null
    var comId:String?=null
    var imageUri:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        setSupportActionBar(toolbarViewEditProfile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbarViewEditProfile.setNavigationOnClickListener {
            val intent = Intent(this, AppSettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        displayOriginalData()
        txtView_currentAddress.isEnabled=false

        btn_image_editProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        rd_userLocation.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_locationYes -> {
                    getCoordinate()
                }
            }
        })

        btn_Save.setOnClickListener {
            updateProfileImage()
            updateProfile()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)

            img_change_profile.setImageBitmap(bitmap)
            btn_image_editProfile.alpha = 0f
        }
    }

    private fun displayOriginalData() {
        val userId = FirebaseAuth.getInstance().uid
        val userDb = FirebaseDatabase.getInstance().getReference("/users")

        userDb.child(userId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                imageId=user?.imageId
                btn_image_editProfile.alpha = 0f
                Glide.with(this@EditProfileActivity).load(user?.profileImageUri).signature(ObjectKey(System.currentTimeMillis()))
                    .into(img_change_profile);
                edt_userName.setText(user?.username)
                edt_fullName.setText(user?.name)
                edt_Phone.setText(user?.phone)
                edt_userAddress.setText(user?.address)
                edt_Biography.setText(user?.biography)
                comId=user?.communityId
                if ((user?.gpsLongitude == 00.0000) && (user?.gpsLatitude == 00.0000)) {
                    txtView_currentCoordinate.text = "Not registered"
                } else {
                    txtView_currentCoordinate.text =
                        "Saved Coordinate: " + user?.gpsLongitude + "," + user?.gpsLatitude
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

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
            txtView_currentCoordinate.text ="Current Coordinate: ${it.latitude},${it.longitude}"
            latitude=it.latitude
            longitude=it.longitude
            Log.d("gps"," ${it.latitude},${it.longitude}")
            getAddress(it.latitude,it.longitude)
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
        txtView_currentAddress.isEnabled=true
        txtView_currentAddress.text = "Current Address:\n$address"
    }

    private fun updateProfileImage(){
        if (selectedPhotoUri == null) return

        val ref = FirebaseStorage.getInstance().getReference("/user-image/$imageId")
        val delRef = FirebaseStorage.getInstance().getReference("user-image").child("$imageId")

        delRef.delete()
            .addOnSuccessListener {
                ref.putFile(selectedPhotoUri!!)
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener {
                        Log.d("image", "Failed to upload image to storage: ${it.message}")
                    }
            }
            .addOnFailureListener {
                Log.d("imageDel","${it.message}")
            }

    }

    private fun updateProfile() {
        val username = edt_userName.text.toString()
        val name = edt_fullName.text.toString()
        val phone = edt_Phone.text.toString()
        val address = edt_userAddress.text.toString()
        val bio = edt_Biography.text.toString()
        val latitude= latitude
        val longitude=longitude
        Log.d("coordinate","$latitude,$longitude")

        val user = mapOf<String,String>(
            "username" to username,
            "name" to name,
            "phone" to phone,
            "address" to address,
            "biography" to bio
        )
        val userCoordinate = mapOf<String,Double>(
            "gpsLatitude" to latitude!!,
            "gpsLongitude" to longitude!!
        )
        val userComm = mapOf<String, String>(
            "userId" to userId!!,
            "name" to name,
            "username" to username
        )

        val userDb = FirebaseDatabase.getInstance().getReference("users")
        var commDb = FirebaseDatabase.getInstance().getReference("/community/$comId/members")

        userDb.child(userId!!).updateChildren(user)
            .addOnSuccessListener {
                userDb.child(userId!!).updateChildren(userCoordinate)
                    .addOnSuccessListener {
                        commDb.child(userId!!).updateChildren(userComm)
                            .addOnSuccessListener {
                                Toast.makeText(this,"Profile Updated",Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this,ProfileActivity::class.java))
                            }
                    }
                    .addOnFailureListener {
                        Log.d("databaseCoordinate","$latitude,$longitude ${it.message}")
                    }
            }
            .addOnFailureListener {
                Log.d("database","${it.message}")
            }
    }
}

