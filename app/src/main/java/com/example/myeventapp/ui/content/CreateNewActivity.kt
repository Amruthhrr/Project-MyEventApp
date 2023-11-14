package com.example.myeventapp.ui.content

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myeventapp.R
import com.example.myeventapp.databinding.ActivityCreateNewBinding
import com.github.ybq.android.spinkit.style.DoubleBounce
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CreateNewActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var uploadTask: UploadTask

    private lateinit var eventName: String
    private lateinit var eventOrganizer: String
    private lateinit var eventDate: String
    private lateinit var eventPlace: String
    private lateinit var eventDescription: String
    private lateinit var eventPrice: String
    private lateinit var events: MutableMap<String, Any>
    private lateinit var filePath: Uri
    private var isEventPaid: Boolean = false

    companion object {
        const val PICK_IMAGE_REQUEST = 100
    }

    private lateinit var binding: ActivityCreateNewBinding

    // Declare an ActivityResultLauncher at the top of your class
    private var imagePickerLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = Firebase.storage
        storageRef = storage.reference
        auth = FirebaseAuth.getInstance()

        val doubleBounce = DoubleBounce()
        binding.loadingProgress.indeterminateDrawable = doubleBounce

        initAppBar()

        // Initialize the ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            handleImageSelectionResult(result.resultCode, result.data)
        }

        binding.btnOpenCalendar.setOnClickListener {
            openDatePicker()
        }

        binding.priceRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            isEventPaid = checkedId == R.id.rb_paid
        }

        binding.btnUploadImage.setOnClickListener {
            pickImage()
        }
    }

    private fun initAppBar() {
        setSupportActionBar(binding.createPageAppBar)
        val appBar = supportActionBar
        appBar?.setDisplayHomeAsUpEnabled(true)
        appBar?.setDisplayShowTitleEnabled(false)
    }

    private fun getDataEvent() {
        eventName = binding.eventNameEditText.text.toString()
        eventOrganizer = binding.eventOrganizerEditText.text.toString()
        eventDate = binding.dateEditText.text.toString()
        eventPlace = binding.eventPlaceEditText.text.toString()
        eventDescription = binding.eventDescEditText.text.toString()
        eventPrice = binding.eventPriceEditText.text.toString()
        events = mutableMapOf(
            "event_name" to eventName,
            "event_organizer" to eventOrganizer,
            "event_user" to auth.currentUser?.uid.toString(),
            "event_date" to eventDate,
            "event_place" to eventPlace,
            "event_description" to eventDescription,
            "is_event_paid" to isEventPaid,
            "event_price" to eventPrice
        )
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        // Use the ActivityResultLauncher to launch the intent
        imagePickerLauncher?.launch(Intent.createChooser(intent, "Select Image from here..."))
    }

    private fun handleImageSelectionResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data!!
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                binding.previewImage.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadData() {
        binding.loadingProgress.visibility = View.VISIBLE
        binding.formContentScrollView.visibility = View.GONE

        val ref = storageRef.child("images/" + UUID.randomUUID())
        uploadTask = ref.putFile(filePath)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                getDataEvent()
                val downloadUri = task.result
                events["image_url"] = downloadUri.toString()
                writeToDb()
            } else {
                Toast.makeText(
                    this,
                    "URL Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun writeToDb() {
        val contextView: View = findViewById(android.R.id.content)
        db.collection("events")
            .add(events)
            .addOnSuccessListener {
                finish()
                Snackbar.make(
                    contextView,
                    "Event has been published: $it",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                binding.loadingProgress.visibility = View.GONE
                binding.formContentScrollView.visibility = View.VISIBLE
                Snackbar.make(
                    contextView,
                    "Event publishing fail: $it",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
    }

    private fun openDatePicker() {
        val newCalendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance()
                newDate.set(year, month, dayOfMonth)
                val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                binding.dateEditText.text = dateFormatter.format(newDate.time).toEditable()
            },
            newCalendar.get(Calendar.YEAR),
            newCalendar.get(Calendar.MONTH),
            newCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create_page_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            if (binding.eventNameEditText.text.toString().trim() == ""
                && binding.eventOrganizerEditText.text.toString().trim() == ""
                && binding.dateEditText.text.toString().trim() == ""
                && binding.eventPlaceEditText.text.toString().trim() == ""
                && binding.eventDescEditText.text.toString().trim() == ""
                && binding.eventPriceEditText.text.toString().trim() == ""
            ) {
                binding.inputEventName.error = "This field can't be empty"
                binding.inputOrganizer.error = "This field can't be empty"
                binding.inputDate.error = "This field can't be empty"
                binding.inputPlace.error = "This field can't be empty"
                binding.inputDescription.error = "This field can't be empty"
                binding.inputPrice.error = "This field can't be empty"
            } else {
                uploadData()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

