package com.example.projectpulse.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.example.projectpulse.R
import com.example.projectpulse.databinding.ActivitySignInBinding
import com.example.projectpulse.firebase.FirestoreClass
import com.example.projectpulse.models.User
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {
    private var binding: ActivitySignInBinding? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        setupToolbar()
        binding?.toolbarSignin?.setNavigationOnClickListener {
            onBackPressed()
        }

        binding?.btnSignin?.setOnClickListener {
            signInRegisterUser()
        }
    }

    fun signInSuccess(user: User) {
        hideProgressDialog()
        startActivity(Intent(this , MainActivity::class.java))
        finish()
    }

    private fun signInRegisterUser() {
        val email = binding?.etEmailAddressSignIn?.text.toString().trim{it <= ' '}
        val password = binding?.etPasswordSignIn?.text.toString().trim{it <= ' '}

        if(validateForm(email , password)) {
            showProgressDialog()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                       FirestoreClass().loadUserData(this)

                    } else {
                        hideProgressDialog()
                        // If sign in fails, display a message to the user.
                        Log.w("Signin", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()

                    }
                }
        }
    }

    private fun validateForm(email: String , password: String): Boolean {
        return when {

            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter a email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {
                true
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding?.toolbarSignin)
        if(supportActionBar != null)
        {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
    }
}