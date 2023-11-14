package com.example.myeventapp.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myeventapp.databinding.ActivityMyTicketBinding
import com.example.myeventapp.databinding.TicketCardItemBinding
import com.example.myeventapp.model.Ticket
import com.bumptech.glide.Glide
import com.example.myeventapp.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyTicketActivity : AppCompatActivity() {
    private lateinit var database: FirebaseFirestore
    private lateinit var adapter: FirestoreRecyclerAdapter<Ticket, TicketHolder>
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMyTicketBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initFirebase()
        initAppBar()
        getTicketData()
    }

    private fun initAppBar() {
        setSupportActionBar(binding.myTicketToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun initFirebase() {
        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    private fun getTicketData() {
        val query = database.collection("tickets")
            .whereEqualTo("ticket_buyer", auth.currentUser?.displayName)

        val response = FirestoreRecyclerOptions.Builder<Ticket>()
            .setQuery(query, Ticket::class.java)
            .build()

        adapter = object : FirestoreRecyclerAdapter<Ticket, TicketHolder>(response) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketHolder {
                val view = TicketCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false).root
                return TicketHolder(view)
            }

            override fun onBindViewHolder(holder: TicketHolder, position: Int, model: Ticket) {
                holder.binding.itemTicketEventName.text = model.ticket_name
                holder.binding.itemTicketEventBuyer.text = model.ticket_buyer
                holder.binding.itemTicketEventPrice.text =
                    getString(R.string.ticket_price_text, model.ticket_price)
                Glide.with(applicationContext)
                    .load(model.ticket_qr_code).into(holder.binding.itemQrCodeImage)
            }
        }

        adapter.notifyDataSetChanged()
        binding.myTicketRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.myTicketRecyclerView.adapter = adapter
    }

    inner class TicketHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: TicketCardItemBinding = TicketCardItemBinding.bind(view)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
