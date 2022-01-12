package com.example.communitymarketplace.loginRegister

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.communitymarketplace.R
import com.example.communitymarketplace.models.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_setup_account.*
import java.util.*

class SetupAccountActivity : AppCompatActivity() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var selectedPhotoUri: Uri? = null
    var latitude: Double? = null
    var longitude: Double? = null

    companion object {
        val TAG = "SetupAccountActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        //      Personalize Action Bar
        supportActionBar?.title = "Setup Your Profile"

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_account)

        val userId = intent.getStringExtra("user_id")
        val emailId = intent.getStringExtra("email_id")

        btn_image_add_news.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        rd_userHomeLocation.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_homeYes -> {
                    getCoordinate()
                }
            }
        })

        btn_Next.setOnClickListener {
            uploadUserImagetoFirebaseStorage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            image_add_news.setImageBitmap(bitmap)
            btn_image_add_news.alpha = 0f
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
            txtView_userCoordinate.text = "Current Coordinate: ${it.latitude},${it.longitude}"
            latitude = it.latitude
            longitude = it.longitude
            Log.d("gps", " ${it.latitude},${it.longitude}")
            getAddress(it.latitude, it.longitude)
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
        txtView_userAddress.isEnabled = true
        txtView_userAddress.text = "Current Address:\n$address"
    }

    private fun uploadUserImagetoFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/user-image/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    registerUserIntoDatabase(it.toString(), filename)
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to upload image to storage: ${it.message}")
            }
    }

    private fun registerUserIntoDatabase(profileImageUri: String, filename: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val username = edt_ComName.text.toString()
        val name = edt_Name.text.toString()
        val email = intent.getStringExtra("email_id") ?: ""
        val phone = edt_Phone.text.toString()
        val address = edt_ComAddress.text.toString()
        val biography = edt_Biography.text.toString()
//        val imageUri =profileImageUri
        val communityId = ""

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(
            uid,
            username,
            name,
            email,
            phone,
            address,
            latitude!!,
            longitude!!,
            biography,
            profileImageUri,
            filename,
            communityId
        )

        ref.setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this, CommunityIntroductionActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to set value to database: ${it.message}")
            }
    }
}