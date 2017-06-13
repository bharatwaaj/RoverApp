package com.rover.android.roverapp.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.rover.android.roverapp.R;
import com.rover.android.roverapp.Widgets.QEditText;

public class ForgotPasswordActivity extends QBaseActivity {

    // Firebase Components
    private FirebaseAuth mAuth;

    // UI Components
    private QEditText emailEditText;

    // Other Components
    private static String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        initUI();
    }

    private void initUI() {

        TAG = getPackageName();

        initToolBar();
        setToolBarTitle(getResources().getString(R.string.forgot_password_activity));

        emailEditText = (QEditText) findViewById(R.id.forgotpw_email_id_et);

        mAuth = setUpFirebase();
    }

    public void handleLoginBtnClick(View view) {
        onBackPressed();
    }

    public void handleFrgtPwResendBtnClick(View view) {
        if (getValidEmailId() != null) {
            showLoadingProgressDialog();
            mAuth.sendPasswordResetEmail(getValidEmailId())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                showKnackToast("Email Sent!", Toast.LENGTH_LONG);
                                hideProgressDialog();
                            } else {
                                showKnackToast("Please enter a proper mail id!", Toast.LENGTH_LONG);
                                hideProgressDialog();
                            }
                        }
                    });
        }
    }

    public String getValidEmailId() {
        String text = emailEditText.getText().toString();
        if (!text.equals("")) {
            return text;
        } else {
            emailEditText.setError("Required.");
            return null;
        }
    }
}
