package com.rover.android.roverapp.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rover.android.roverapp.Others.QApplication;
import com.rover.android.roverapp.R;
import com.rover.android.roverapp.Receiver.ConnectivityReceiver;
import com.rover.android.roverapp.Widgets.QTextView;
import com.rover.android.roverapp.Widgets.QToast;

public class QBaseActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {


    // Firebase Components
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // UI Components
    private Toolbar toolbar;
    private TextView toolBarTitle;
    private QToast knackToast;
    private QTextView nextButton;
    private ImageView profileMenuButton;
    private ImageView activityMenuButton;
    public ProgressDialog mProgressDialog;

    // Other Components
    private SharedPreferences sharedPreferences;
    private Handler mHandler;

    // Firebase Remote Data
    private String latestVersionFromFirebase = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpFirebase();
        FirebaseApp.initializeApp(this);
        mHandler = new Handler();
    }

    public void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    public void setToolBarTitle(String title) {
        if (toolBarTitle == null) {
            toolBarTitle = (TextView) findViewById(R.id.toolBarTitle);
        }
        toolBarTitle.setText(title);
    }

    public void showKnackToast(String message, int duration) {
        if (knackToast == null) {
            knackToast = new QToast(this);
        }
        knackToast.setBackgroundColor(getResources().getDrawable(R.drawable.oval_bg_primary));
        knackToast.showMessage(message, duration);
    }

    public FirebaseAuth setUpFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("FirebaseUser", "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    // User is signed out
                    Log.d("FirebaseUser", "onAuthStateChanged:signed_out");
                }
            }
        };
        return mAuth;
    }

    public DatabaseReference setUpFirebaseDatabase() {
        return FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        hideProgressDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        QApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) throws PackageManager.NameNotFoundException {
        if (isConnected) {
            Toast.makeText(this, "Internet Connectivity Established!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No Internet Connectivity!", Toast.LENGTH_SHORT).show();
        }
    }



    public void showKnackGreenToast(String s, int lengthLong) {
        if (knackToast == null) {
            knackToast = new QToast(this);
        }
        knackToast.setBackgroundColor(getResources().getDrawable(R.drawable.oval_bg_orange));
        knackToast.showMessage(s, lengthLong);
    }

    public void showLoadingProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    public void showProgressDialogWithMessage(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public String getUid() {
        return mAuth.getCurrentUser().getUid();
    }

    public String getUserMailId() {
        return mAuth.getCurrentUser().getEmail();
    }
}
