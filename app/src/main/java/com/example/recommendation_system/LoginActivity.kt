package com.example.recommendation_system


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.recommendation_system.R

import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailLayout: TextInputLayout? = findViewById(R.id.emailLayout)
        val passwordLayout: TextInputLayout? = findViewById(R.id.passwordLayout)
        val editEmail: TextInputEditText = findViewById(R.id.editEmail)
        val editPassword: TextInputEditText = findViewById(R.id.editPassword)
        val btnLogin: MaterialButton = findViewById(R.id.btnLogin)
        val textSignup: TextView = findViewById(R.id.textSignup)

        btnLogin.setOnClickListener {
            // clear previous errors if TextInputLayouts exist
            emailLayout?.error = null
            passwordLayout?.error = null

            val email = editEmail.text?.toString()?.trim().orEmpty()
            val password = editPassword.text?.toString()?.trim().orEmpty()

            var hasError = false
            if (email.isEmpty()) {
                if (emailLayout != null) emailLayout.error = "Required" else editEmail.error = "Required"
                hasError = true
            }
            if (password.isEmpty()) {
                if (passwordLayout != null) passwordLayout.error = "Required" else editPassword.error = "Required"
                hasError = true
            }
            if (hasError) return@setOnClickListener

            // Disable button and show slight UI feedback
            btnLogin.isEnabled = false
            val oldText = btnLogin.text
            btnLogin.text = "Signing in…"

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                        prefs.edit().putBoolean("user_logged_in", true).apply()

                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        val msg = task.exception?.localizedMessage ?: "Login failed"
                        if (emailLayout != null) emailLayout.error = msg else editEmail.error = msg
                        btnLogin.isEnabled = true
                        btnLogin.text = oldText
                    }
                }
        }

        textSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        auth = FirebaseAuth.getInstance()
        val current = auth.currentUser
        if (current != null) {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            prefs.edit().putBoolean("user_logged_in", true).apply()
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
}
