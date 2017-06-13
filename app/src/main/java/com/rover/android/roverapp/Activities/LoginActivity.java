package com.rover.android.roverapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.rover.android.roverapp.Models.User;
import com.rover.android.roverapp.R;
import com.rover.android.roverapp.Widgets.QEditText;
import com.rover.android.roverapp.Widgets.QTextView;

public class LoginActivity extends QBaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    // Firebase Components
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Google Components
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions gso;
    private static final int RC_SIGN_IN = 9001;


    // UI Components
    private QEditText emailIdEditText;
    private QEditText passwordEditText;
    private QTextView fbLoginButton;

    // Other Components
    private static String TAG;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    public void onStart() {
        super.onStart();

        // Check auth on Activity start
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initUI();
    }

    private void initUI() {

        //TAG Setup
        TAG = getPackageName();

        // Toolbar Setup
        initToolBar();
        setToolBarTitle(getResources().getString(R.string.login_activity));

        // UI Setup
        emailIdEditText = (QEditText) findViewById(R.id.login_email_id_et);
        passwordEditText = (QEditText) findViewById(R.id.login_password_et);

        // Firebase Setup
        mAuth = setUpFirebase();

        // Permission Check
        checkPermissions();

        // Google Setup
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mDatabase = setUpFirebaseDatabase();
    }

    private boolean validateLogin() {
        if (emailIdEditText.getText().toString().equals("")) {
            emailIdEditText.setError("Required.");
            return false;
        } else if (passwordEditText.getText().toString().equals("")) {
            passwordEditText.setError("Required.");
            return false;
        } else {
            return true;
        }
    }

    // Button OnClicks
    public void handleLoginBtnClick(View view) {
        if (validateLogin()) {
            showLoadingProgressDialog();
            mAuth.signInWithEmailAndPassword(emailIdEditText.getText().toString(), passwordEditText.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                hideProgressDialog();
                                showKnackToast("Login Unsuccessful", Toast.LENGTH_LONG);
                            } else {
                                hideProgressDialog();
                                showKnackToast("Login Successful", Toast.LENGTH_LONG);
                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            }
                        }
                    });
        }
    }

    public void handleForgotPassBtnClick(View view) {
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
    }

    public void handleSignUpBtnClick(View view) {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
    }

    // Google Authentication
    public void handleGoogleLoginBtnClick(View view) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void onAuthSuccess(FirebaseUser user) {
        writeNewUser(user.getUid(), user.getDisplayName(), user.getEmail());
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    private void writeNewUser(String userId, String username, String email) {
        User user = new User(username, email, "Null", "Null");
        mDatabase.child("users").child(userId).setValue(user);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showLoadingProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            hideProgressDialog();
                            showKnackToast("Authentication failed.",
                                    Toast.LENGTH_SHORT);
                        } else {
                            showKnackGreenToast("Authentication success.",
                                    Toast.LENGTH_SHORT);
                            onAuthSuccess(mAuth.getCurrentUser());
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {

            }
        }
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                + ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else

        {
            return true;
        }

    }

}
