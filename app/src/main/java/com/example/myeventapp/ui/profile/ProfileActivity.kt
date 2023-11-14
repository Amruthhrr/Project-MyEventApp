package com.example.myeventapp.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myeventapp.R
import com.example.myeventapp.databinding.ActivityProfileBinding
import com.github.ybq.android.spinkit.style.ThreeBounce
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException
import java.util.*

class ProfileActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var uriProfile: Uri
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityProfileBinding

    companion object {
        const val CHOOSE_PROFILE = 300
        var mPhotoUrl = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureAppBar()
        configureLoading()
        initFirebaseAuth()
        initProfileClickedItem()
        loadUserInformation()
    }

    override fun onStart() {
        super.onStart()
        loadUserInformation()
    }

    private fun initFirebaseAuth() {
        auth = FirebaseAuth.getInstance()
    }

    private fun configureLoading() {
        val threeBounce = ThreeBounce()
        binding.profileUploadProgress.indeterminateDrawable = threeBounce
    }

    private fun configureAppBar() {
        setSupportActionBar(binding.profilePageAppBar)
        val appBar = supportActionBar
        appBar?.setDisplayShowTitleEnabled(false)
        appBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun loadUserInformation() {
        val user = auth.currentUser
        if (user != null) {
            if (user.photoUrl != null) {
                Glide.with(this).load(user.photoUrl.toString()).into(binding.userProfileImage)
            }
            if (user.displayName != null) {
                binding.displayNameEditText.text = user.displayName.toString().toEditable()
            }
        }
    }

    private fun initProfileClickedItem() {
        binding.userProfileImage.setOnClickListener(this)
        binding.btnSaveProfile.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_save_profile) {
            saveUserInformation()
        } else if (v?.id == R.id.user_profile_image) {
            showImageChooser()
        }
    }

    private fun showImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select Profile Image"
            ),
            CHOOSE_PROFILE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_PROFILE
            && resultCode == Activity.RESULT_OK
            && data != null
            && data.data != null
        ) {
            uriProfile = data.data!!
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uriProfile)
                binding.userProfileImage.setImageBitmap(bitmap)
                uploadProfileImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadProfileImage() {
        val profileRef =
            FirebaseStorage.getInstance().getReference("profile_pics/" + UUID.randomUUID() + ".jpg")

        binding.profileUploadProgress.visibility = View.VISIBLE
        profileRef.putFile(uriProfile).addOnSuccessListener {
            binding.profileUploadProgress.visibility = View.GONE
            profileRef.downloadUrl.addOnSuccessListener {
                mPhotoUrl += it.toString()
            }
            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserInformation() {
        val displayName = binding.displayNameEditText.text.toString()
        if (displayName.isEmpty()) {
            binding.inputDisplayName.error = "Name is required"
            binding.displayNameEditText.requestFocus()
            return
        }

        val user = auth.currentUser
        if (user != null) {
            val profile = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .setPhotoUri(Uri.parse(mPhotoUrl))
                .build()
            user.updateProfile(profile).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
}
