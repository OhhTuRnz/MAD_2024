package com.example.mad_2024_app.Activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.mad_2024_app.App
import com.example.mad_2024_app.R
import com.example.mad_2024_app.database.User
import com.example.mad_2024_app.repositories.UserRepository
import com.example.mad_2024_app.view_models.UserViewModel
import com.example.mad_2024_app.view_models.ViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText

    private lateinit var buttonLogin: Button
    private lateinit var buttonGoogleSignIn: Button

    private lateinit var buttonCancel: ImageButton

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var userViewModel: UserViewModel
    private lateinit var userRepo: UserRepository

    private val TAG = "LoginActivity"

    // ActivityResultLauncher for Google Sign-In
    private val googleSignInResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                // Handle Google Sign-In failure
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        applyTheme(sharedPreferences)

        setContentView(R.layout.activity_login)

        userRepo = DbUtils.getUserRepository(application as App)
        val userFactory = ViewModelFactory(userRepo)
        userViewModel = ViewModelProvider(this, userFactory)[UserViewModel::class.java]

        // Initialize Firebase Auth
        auth = Firebase.auth

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonCancel = findViewById(R.id.buttonCancel)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.firebase.ui.auth.R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn)
        buttonGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInResultLauncher.launch(signInIntent)
        }

        buttonCancel.setOnClickListener {
            // Create an intent to start MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finish LoginActivity
        }

        setupSignupClickable()
    }

    private fun setupSignupClickable(){
        val textViewRegister: TextView = findViewById(R.id.textViewRegister)
        val spannableString = SpannableString("Don't have an account? Sign up here")

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Create and show the dialog
                val dialogView =
                    LayoutInflater.from(this@LoginActivity).inflate(R.layout.layout_sign_up, null)
                val dialog = AlertDialog.Builder(this@LoginActivity)
                    .setView(dialogView)
                    .create()

                // Initialize dialog views
                val emailField = dialogView.findViewById<EditText>(R.id.editTextSignUpEmail)
                val passwordField = dialogView.findViewById<EditText>(R.id.editTextSignUpPassword)
                val signUpButton = dialogView.findViewById<Button>(R.id.buttonSignUp)

                signUpButton.setOnClickListener {
                    val email = emailField.text.toString()
                    val password = passwordField.text.toString()
                    val sharedPreferences =
                        getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

                    sharedPreferences.getString("userId", null)?.let { userId ->
                        // Retrieve user data asynchronously using Flow
                        lifecycleScope.launch {
                            userViewModel.getUserByUUIDPreCollect(userId).collect { user ->
                                // Collect the user data from the Flow
                                user?.let {
                                    // Implement sign-up logic with Firebase Auth
                                    FirebaseAuth.getInstance()
                                        .createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(this@LoginActivity) { task ->
                                            if (task.isSuccessful) {
                                                // Sign-up success
                                                val newUser = FirebaseAuth.getInstance().currentUser
                                                val newUuid = newUser?.uid
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Registration successful",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                if (newUuid != null) {
                                                    // Perform upsertUser with the retrieved user data
                                                    userViewModel.upsertUser(
                                                        user.copy(
                                                            uuid = newUuid,
                                                            email = email
                                                        )
                                                    )

                                                    // Update shared preferences with the new UUID
                                                    val editor = sharedPreferences.edit()
                                                    editor.putString("userId", newUuid)
                                                    editor.apply()
                                                }
                                                dialog.dismiss()
                                            } else {
                                                // If sign-up fails, display a message to the user.
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Authentication failed: ${task.exception?.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }
                            }
                        }
                    }
                }
                dialog.show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true // Set to false if you don't want underline
            }
        }

        // Set the span to the word "here"
        val startIndex = spannableString.indexOf("here")
        val endIndex = startIndex + "here".length
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Apply the SpannableString to the TextView
        textViewRegister.text = spannableString
        textViewRegister.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun applyTheme(sharedPreferences: SharedPreferences){
        val isDarkModeEnabled = sharedPreferences.getBoolean("darkModeEnabled", false)

        // Apply the appropriate theme
        if (isDarkModeEnabled) {
            setTheme(R.style.AppTheme_Dark)
        } else {
            setTheme(R.style.AppTheme_Light)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User successfully logged in")
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    val sharedPreferences =
                        getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val currentUserUuid = currentUser?.uid

                    lifecycleScope.launch {
                        val userFromDb = currentUserUuid?.let {
                            userViewModel.getUserByUUIDPreCollect(it).firstOrNull()
                        }

                        if (userFromDb == null && currentUserUuid != null) {
                            // User does not exist in the database, create a new user
                            val newUserDetails = User(
                                uuid = currentUserUuid,
                                email = email
                            ) // Adjust according to your User data class
                            userViewModel.upsertUser(newUserDetails)
                            Log.d(TAG, "New user created with UUID: $currentUserUuid")
                            sharedPreferences.edit().putString("userId", currentUserUuid).apply()
                        } else if (userFromDb != null) {
                            Log.d(TAG, "Existing user found with UUID: ${userFromDb.uuid}")
                            // Existing user logic, if needed
                        }

                        startActivity(intent)
                        finish()
                    }
                } else {
                    Log.d(TAG, "Invalid user login")
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Google sign-in successful, check if new or existing user
                    val user = auth.currentUser
                    if (user != null) {
                        // Check if the user exists in your database
                        lifecycleScope.launch {
                            val userFromDb = userViewModel.getUserByUUIDPreCollect(user.uid).firstOrNull()
                            if (userFromDb == null) {
                                // New user, create an entry in your database
                                createUserInDatabase(user)
                            } else {
                                // Existing user
                                Log.d(TAG, "Existing Firebase user found")
                                // You can redirect to the main activity here if needed
                            }
                        }
                    }
                } else {
                    // Google sign-in failed
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createUserInDatabase(firebaseUser: FirebaseUser) {
        val newUserDetails = User(
            uuid = firebaseUser.uid,
            email = firebaseUser.email ?: ""
        ) // Adjust according to your User data class

        // Upsert (create or update) the new user in the database
        userViewModel.upsertUser(newUserDetails).also {
            Log.d(TAG, "New Firebase user created with UUID: ${firebaseUser.uid}")
            // Update shared preferences with the new user's UUID
            val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("userId", firebaseUser.uid).apply()
            // Redirect to the main activity or perform other post-creation actions
        }
    }
}