package com.mobile.instagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    private var googleSignInClient: GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        email_login_button.setOnClickListener{
            signinAndSignup()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        //구글 로그인
        google_sign_in_button.setOnClickListener {
            //first step
            googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient!!.signOut() //계정 선택하게 하기 !!!
            val signInIntent = googleSignInClient!!.signInIntent
            startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
        }
        
    }

    override fun onDestroy() {
        super.onDestroy()
        auth.signOut()
        googleSignInClient!!.signOut()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_LOGIN_CODE) {
            val signInTask = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = signInTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth?.signInWithCredential(credential)
                .addOnCompleteListener(this) {
                    task->
                    if (task.isSuccessful) {
                        // Google로 로그인 성공
                        Log.d(this::class.java.simpleName, "User Email :: ${task.result?.user?.email}")
                        moveMainPage(task.result?.user)}
                    else {
                        // Google로 로그인 실패
                        Log.d(this::class.java.simpleName, "Firebase Login Failure.")
                    }

        }
    }


    private fun signinAndSignup(){
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener{
                task ->
                    if(task.isSuccessful){
                        //creating a user acoount
                        moveMainPage(task.result?.user)
                    }else if(task.exception?.message.isNullOrBlank()){
                        //show the error message
                        Toast.makeText(this, task.exception?.message,Toast.LENGTH_LONG).show()
                    }else{
                        //Login if you have count
                        signinEmail()
                    }
            }
    }
    fun signinEmail(){
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())
            ?.addOnCompleteListener{
                task->
                if(task.isSuccessful){
                    //login
                    moveMainPage(task.result?.user)
                }else{
                    //show the error message
                    Toast.makeText(this, task.exception?.message,Toast.LENGTH_LONG).show()

                }

            }
    }
    fun moveMainPage(user:FirebaseUser?){
        if(user != null){
            startActivity(Intent(this,MainActivity::class.java))
        }
    }
}