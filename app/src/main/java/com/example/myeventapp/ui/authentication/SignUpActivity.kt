package com.example.myeventapp.ui.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.myeventapp.databinding.ActivitySignUpBinding
import com.example.myeventapp.ui.content.DashboardActivity
import com.github.ybq.android.spinkit.style.DoubleBounce
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initFirebaseAuth()
        setUpLoadingIndicator()
        initAuthButton()
    }

    private fun setUpLoadingIndicator() {
        val doubleBounce = DoubleBounce()
        binding.signUpLoading.indeterminateDrawable = doubleBounce
    }

    private fun initFirebaseAuth() {
        auth = FirebaseAuth.getInstance()
    }

    private fun initDataInput() {
        email = binding.newEmailEditText.text.toString().trim()
        password = binding.newPasswordEditText.text.toString().trim()

        if (email.isEmpty()) {
            binding.signUpInputEmail.error = "Email is required"
            binding.newEmailEditText.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.signUpInputEmail.error = "Email is invalid"
            binding.newEmailEditText.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.signUpInputPassword.error = "Password is required"
            binding.newPasswordEditText.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.signUpInputPassword.error = "Password must be at least 6 characters"
            binding.newPasswordEditText.requestFocus()
            return
        }
    }

    private fun initAuthButton() {
        binding.btnSignUp.setOnClickListener(this)
        binding.btnHaveAccount.setOnClickListener(this)
    }

    private fun signUp() {
        initDataInput()
        binding.signUpLoading.visibility = View.VISIBLE
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.signUpLoading.visibility = View.GONE
                if (task.isSuccessful) {
                    finish()
                    Toast.makeText(
                        applicationContext,
                        "User registered successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(applicationContext, DashboardActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            applicationContext,
                            "Email already registered",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            task.exception?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnSignUp.id -> signUp()
            binding.btnHaveAccount.id -> {
                finish()
                startActivity(Intent(this, SignInActivity::class.java))
            }
        }
    }
}
