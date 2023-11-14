package com.example.myeventapp.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myeventapp.databinding.ActivityMyEventBinding
import com.example.myeventapp.databinding.EventCardItemBinding
import com.example.myeventapp.model.Event
import com.example.myeventapp.R
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyEventActivity : AppCompatActivity() {
    private lateinit var database: FirebaseFirestore
    private lateinit var adapter: FirestoreRecyclerAdapter<Event, MyEventHolder>
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMyEventBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAppBar()
        initFirebase()
        getMyEventData()
    }

    private fun initAppBar() {
        setSupportActionBar(binding.myEventToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun initFirebase() {
        database = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    private fun getMyEventData() {
        val query = database.collection("events")
            .whereEqualTo("event_user", auth.currentUser?.uid)

        val response = FirestoreRecyclerOptions.Builder<Event>()
            .setQuery(query, Event::class.java)
            .build()

        adapter = object : FirestoreRecyclerAdapter<Event, MyEventHolder>(response) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyEventHolder {
                val view = EventCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false).root
                return MyEventHolder(view)
            }

            override fun onBindViewHolder(holder: MyEventHolder, position: Int, model: Event) {
                holder.binding.eventName.text = model.event_name
                holder.binding.eventPlace.text = model.event_place
                holder.binding.eventDate.text = model.event_date
                Glide.with(holder.itemView.context)
                    .load(model.image_url).into(holder.binding.eventImage)
            }


        }

        adapter.notifyDataSetChanged()
        binding.myEventRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.myEventRecyclerView.adapter = adapter
    }

    inner class MyEventHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding: EventCardItemBinding = EventCardItemBinding.bind(view)
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
