package com.example.myeventapp.ui.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myeventapp.databinding.EventCardItemBinding
import com.example.myeventapp.model.Event
import com.example.myeventapp.ui.content.DetailActivity
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class EventsAdapter(
    options: FirestoreRecyclerOptions<Event>,
    private val context: Context
) : FirestoreRecyclerAdapter<Event, EventsAdapter.EventHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
        val binding =
            EventCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventHolder(binding)
    }

    override fun onBindViewHolder(holder: EventHolder, position: Int, model: Event) {
        holder.bindItem(model)
        holder.itemView.setOnClickListener {
            Toast.makeText(context, model.event_name, Toast.LENGTH_SHORT).show()
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_QUERY_TITLE, model.event_name)
            context.startActivity(intent)
        }
    }

    class EventHolder(private val binding: EventCardItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItem(event: Event) {
            binding.apply {
                eventName.text = event.event_name
                eventDate.text = event.event_date
                eventPlace.text = event.event_place
                Glide.with(root.context).load(event.image_url).into(eventImage)
            }
        }
    }
}
