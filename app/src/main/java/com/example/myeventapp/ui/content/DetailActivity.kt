package com.example.myeventapp.ui.content

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.myeventapp.R
import com.example.myeventapp.databinding.ActivityDetailBinding
import com.github.ybq.android.spinkit.style.ThreeBounce
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DetailActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var bundle: Bundle? = null
    private lateinit var eventName: String
    private lateinit var eventPrice: String

    private lateinit var binding: ActivityDetailBinding

    companion object {
        const val EXTRA_QUERY_TITLE = "event_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bundle = intent.extras
        initAuth()
        initAppBar()
        initLoadingIndicator()
        initFireStore()
        getEventData()

        binding.btnBuy.setOnClickListener(this)
    }

    private fun initAuth() {
        auth = FirebaseAuth.getInstance()
    }

    private fun initFireStore() {
        db = FirebaseFirestore.getInstance()
    }

    private fun initLoadingIndicator() {
        val threeBounce = ThreeBounce()
        binding.detailLoading.indeterminateDrawable = threeBounce
    }

    private fun initAppBar() {
        setSupportActionBar(binding.detailToolbar)
        val appBar = supportActionBar
        appBar?.setDisplayHomeAsUpEnabled(true)
        appBar?.title = bundle?.getString(EXTRA_QUERY_TITLE)
        appBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp)
        binding.detailCollapseToolbar.setExpandedTitleColor(
            ContextCompat.getColor(
                this,
                android.R.color.transparent
            )
        )
    }

    private fun getEventData() {
        binding.contentContainer.visibility = View.GONE
        binding.detailLoading.visibility = View.VISIBLE

        val dbRef = db.collection("events")
        val query = dbRef.whereEqualTo("event_name", bundle?.getString(EXTRA_QUERY_TITLE))
        query.get().addOnSuccessListener { documents ->
            binding.contentContainer.visibility = View.VISIBLE
            binding.detailLoading.visibility = View.GONE
            for (document in documents) {
                eventName = document["event_name"].toString()
                eventPrice = document["event_price"].toString()
                Glide.with(this).load(document["image_url"]).into(binding.detailEventPoster)
                binding.detailEventDescription.text = document["event_description"].toString()
                binding.detailEventOrganizer.text = document["event_organizer"].toString()
                binding.detailEventDate.text = document["event_date"].toString()
                binding.detailEventPlace.text = document["event_place"].toString()
                binding.detailEventPrice.text = document["event_price"].toString()
                binding.eventNameText.text = document["event_name"].toString()
                document["event_price"].let { price ->
                    if (price != null) {
                        if (price != "0") {
                            binding.detailFreePaid.text = getString(R.string.price_paid, price)
                        } else {
                            binding.detailFreePaid.text = getString(R.string.price_free)
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_buy) {
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putExtra(CheckoutActivity.EXTRA_EVENT_NAME, eventName)
            intent.putExtra(CheckoutActivity.EXTRA_EVENT_PRICE, eventPrice)
            intent.putExtra(CheckoutActivity.EXTRA_BUYER_NAME, auth.currentUser?.displayName)
            startActivity(intent)
        }
    }
}
