package com.rover.android.roverapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.rover.android.roverapp.Models.User;
import com.rover.android.roverapp.R;
import com.rover.android.roverapp.Utils.EmailValidator;
import com.rover.android.roverapp.Widgets.QEditText;

public class SignUpActivity extends QBaseActivity {

    // Firebase Components
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    // UI Components
    private QEditText userNameSignUpTextview;
    private QEditText emailIdSignUpTextview;
    private QEditText passwordSignUpTextview;
    private QEditText phoneSignUpTextview;

    // Other Components
    private EmailValidator emailValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initUI();
    }

    private void initUI() {
        initToolBar();
        setToolBarTitle(getResources().getString(R.string.sign_up_activity));
        userNameSignUpTextview = (QEditText) findViewById(R.id.signup_username_id_et);
        emailIdSignUpTextview = (QEditText) findViewById(R.id.signup_email_id_et);
        passwordSignUpTextview = (QEditText) findViewById(R.id.signup_password_et);
        phoneSignUpTextview = (QEditText) findViewById(R.id.signup_phone_et);
        mAuth = setUpFirebase();
        mDatabase = setUpFirebaseDatabase();
        emailValidator = new EmailValidator();
    }

    private void onAuthSuccess(FirebaseUser user) {
        writeNewUser(user.getUid(), userNameSignUpTextview.getText().toString(),
                emailIdSignUpTextview.getText().toString(),
                phoneSignUpTextview.getText().toString(),
                passwordSignUpTextview.getText().toString());
        startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
        finish();
    }

    // [START basic_write]
    private void writeNewUser(String userId, String name, String email, String phone, String password) {
        User user = new User(name, email, phone, password);
        mDatabase.child("users").child(userId).setValue(user);
    }
    // [END basic_write]

    public void handleSignUpBtnClick(View view) {
        if (validateSignUp()) {
            mAuth.createUserWithEmailAndPassword(emailIdSignUpTextview.getText().toString(), passwordSignUpTextview.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                hideProgressDialog();
                                showKnackToast("Sorry! Our servers are busy!\nTry after sometime!", Toast.LENGTH_LONG);
                            } else {
                                hideProgressDialog();
                                showKnackToast("Sign up successful!", Toast.LENGTH_LONG);
                                onAuthSuccess(mAuth.getCurrentUser());
                            }
                        }
                    });
        }
    }

    private boolean validateSignUp() {
        if (emailIdSignUpTextview.getText().toString().equals("")){
            emailIdSignUpTextview.setError("Required.");
        }else if(!emailValidator.validate(emailIdSignUpTextview.getText().toString())){
            emailIdSignUpTextview.setError("Enter a valid Email.");
        }else if(passwordSignUpTextview.getText().toString().equals("")){
            passwordSignUpTextview.setError("Required.");
        }else if(passwordSignUpTextview.getText().toString().length() < 6 || passwordSignUpTextview.getText().toString().length() > 15){
            passwordSignUpTextview.setError("Enter only 6-15 characters.");
        }else if(phoneSignUpTextview.getText().toString().equals("")){
            phoneSignUpTextview.setError("Required.");
        }else if(phoneSignUpTextview.getText().toString().length() != 10){
            phoneSignUpTextview.setError("Enter a valid 10-digit phone number.");
        }else {
            return true;
        }
        return false;
    }

    public void handleLoginBtnClick(View view) {
        onBackPressed();
    }
}
