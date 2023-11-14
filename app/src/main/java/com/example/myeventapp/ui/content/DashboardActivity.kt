package com.example.myeventapp.ui.content

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myeventapp.model.Event
import com.example.myeventapp.ui.authentication.SignInActivity
import com.example.myeventapp.ui.profile.ProfileActivity

import com.example.myeventapp.R
import com.example.myeventapp.databinding.ActivityMainBinding
import com.example.myeventapp.ui.adapter.EventsAdapter
import com.example.myeventapp.ui.user.MyEventActivity
import com.example.myeventapp.ui.user.MyTicketActivity
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DashboardActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val eventsRef = db.collection("events")
    private var adapter: EventsAdapter? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setSupportActionBar(binding.dashboardAppBar)

        binding.dashboardFab.setOnClickListener {
            navigateToCreateActivity()
        }

        initGreetingMessage()
        setupRecyclerView()
    }

    private fun initGreetingMessage() {
        if (auth.currentUser != null) {
            binding.greetingText.text = getString(R.string.greeting, auth.currentUser?.displayName)
        } else {
            binding.greetingText.text = getString(R.string.anonym_greeting)
        }
    }

    private fun setupRecyclerView() {
        val query: Query = eventsRef.orderBy("event_date", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<Event>()
            .setQuery(query, Event::class.java)
            .build()

        adapter = EventsAdapter(options, applicationContext)
        val linearLayoutManager = LinearLayoutManager(
            applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.eventsRecyclerView.layoutManager = linearLayoutManager
        binding.eventsRecyclerView.hasFixedSize()
        binding.eventsRecyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            adapter?.startListening()
        } else {
            finish()
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    private fun navigateToCreateActivity() {
        val intent = Intent(this, CreateNewActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.profile_menu -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            R.id.sign_out_menu -> {
                FirebaseAuth.getInstance().signOut()
                finish()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
            }
            R.id.my_ticket_menu -> {
                startActivity(Intent(this, MyTicketActivity::class.java))
            }
            R.id.my_event_menu -> {
                startActivity(Intent(this, MyEventActivity::class.java))
            }
        }

        return true
    }
}
