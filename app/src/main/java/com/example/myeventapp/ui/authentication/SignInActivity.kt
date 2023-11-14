package com.example.myeventapp.ui.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.myeventapp.databinding.ActivityAuthBinding
import com.example.myeventapp.ui.content.DashboardActivity
import com.github.ybq.android.spinkit.style.DoubleBounce
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initFirebaseAuth()
        setUpLoadingIndicator()
        initAuthButton()
    }

    private fun setUpLoadingIndicator() {
        val doubleBounce = DoubleBounce()
        binding.signInLoading.indeterminateDrawable = doubleBounce
    }

    private fun initFirebaseAuth() {
        auth = FirebaseAuth.getInstance()
    }

    private fun initDataInput() {
        email = binding.emailEditText.text.toString().trim()
        password = binding.passwordEditText.text.toString().trim()

        if (email.isEmpty()) {
            binding.signInInputEmail.error = "Email is required"
            binding.emailEditText.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.signInInputEmail.error = "Email is invalid"
            binding.emailEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.signInInputPassword.error = "Password is required"
            binding.passwordEditText.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.signInInputPassword.error = "Password must be at least 6 characters"
            binding.passwordEditText.requestFocus()
            return
        }
    }

    private fun initAuthButton() {
        binding.btnSignIn.setOnClickListener(this)
        binding.btnNewAccount.setOnClickListener(this)
    }

    private fun signIn() {
        initDataInput()
        binding.signInLoading.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.signInLoading.visibility = View.GONE
                if (task.isSuccessful) {
                    finish()
                    val intent = Intent(applicationContext, DashboardActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        applicationContext,
                        task.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // Use the ViewBinding references
            binding.btnSignIn.id -> signIn()
            binding.btnNewAccount.id -> {
                finish()
                startActivity(Intent(this, SignUpActivity::class.java))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            finish()
            startActivity(Intent(this, DashboardActivity::class.java))
        }
    }
}
