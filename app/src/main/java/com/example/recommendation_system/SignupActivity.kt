package com.example.recommendation_system

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.recommendation_system.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlin.apply
import kotlin.jvm.java
import kotlin.let
import kotlin.text.isEmpty
import kotlin.text.isNotEmpty
import kotlin.text.orEmpty
import kotlin.text.trim
import androidx.core.content.edit

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize FirebaseAuth (requires Firebase dependencies and google-services.json)
        auth = FirebaseAuth.getInstance()

        val nameLayout: TextInputLayout = findViewById(R.id.nameLayout)
        val emailLayout: TextInputLayout = findViewById(R.id.emailLayout)
        val passwordLayout: TextInputLayout = findViewById(R.id.passwordLayout)
        val confirmPasswordLayout: TextInputLayout = findViewById(R.id.confirmPasswordLayout)

        val editName: TextInputEditText = findViewById(R.id.editName)
        val editEmail: TextInputEditText = findViewById(R.id.editEmail)
        val editPassword: TextInputEditText = findViewById(R.id.editPassword)
        val editConfirmPassword: TextInputEditText = findViewById(R.id.editConfirmPassword)

        val checkTerms: CheckBox = findViewById(R.id.checkTerms)
        val btnSignup: MaterialButton = findViewById(R.id.btnSignup)
        val textLogin: TextView = findViewById(R.id.textLogin)
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        val progressBar: CircularProgressIndicator = findViewById(R.id.progressBar)

        btnBack.setOnClickListener { finish() }
        textLogin.setOnClickListener { finish() }

        btnSignup.setOnClickListener {
            // Reset previous errors
            nameLayout.error = null
            emailLayout.error = null
            passwordLayout.error = null
            confirmPasswordLayout.error = null

            val name = editName.text?.toString()?.trim().orEmpty()
            val email = editEmail.text?.toString()?.trim().orEmpty()
            val password = editPassword.text?.toString()?.trim().orEmpty()
            val confirmPassword = editConfirmPassword.text?.toString()?.trim().orEmpty()

            var hasError = false
            if (name.isEmpty()) {
                nameLayout.error = "Required"
                hasError = true
            }
            if (email.isEmpty()) {
                emailLayout.error = "Required"
                hasError = true
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.error = "Invalid email"
                hasError = true
            }
            if (password.length < 6) {
                passwordLayout.error = "At least 6 characters"
                hasError = true
            }
            if (confirmPassword != password) {
                confirmPasswordLayout.error = "Passwords do not match"
                hasError = true
            }
            if (!checkTerms.isChecked) {
                // Surface an inline hint using the name layout to avoid adding more UI
                confirmPasswordLayout.error = confirmPasswordLayout.error?.let { "$it • Accept terms" } ?: "Accept terms"
                hasError = true
            }

            if (hasError) return@setOnClickListener

            // Show progress and disable button to prevent repeats
            progressBar.visibility = View.VISIBLE
            btnSignup.isEnabled = false

            // Create user with Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null && name.isNotEmpty()) {
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                            user.updateProfile(profileUpdates).addOnCompleteListener {
                                // proceed regardless of profile update result
                                onSignupSuccess()
                            }
                        } else {
                            onSignupSuccess()
                        }
                    } else {
                        val errorMsg = task.exception?.localizedMessage ?: "Signup failed"
                        emailLayout.error = errorMsg
                        progressBar.visibility = View.GONE
                        btnSignup.isEnabled = true
                    }
                }
        }
    }

    private fun onSignupSuccess() {
        // Mark user as logged in for app flow
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit { putBoolean("user_logged_in", true) }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
