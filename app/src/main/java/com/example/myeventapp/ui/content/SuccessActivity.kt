package com.example.myeventapp.ui.content

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.myeventapp.R


class SuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        Handler().postDelayed({
            val home = Intent(this, DashboardActivity::class.java)
            startActivity(home)
            finish()
        }, 2000)
    }
}
