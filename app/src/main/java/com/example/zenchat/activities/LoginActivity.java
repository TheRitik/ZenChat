package com.example.zenchat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.zenchat.R;
import com.example.zenchat.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 11;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            goToNextActivity();
        }

        database = FirebaseDatabase.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1:867573929437:android:065ba71ed5541c16975234")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.loginbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private void signIn() {
        try {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } catch (Exception e) {
            Log.e(TAG, "Error starting sign in intent", e);
            Toast.makeText(this, "Error starting sign in. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToNextActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Log.e(TAG, "Google Sign In failed: account is null");
                    Toast.makeText(this, "Google Sign In failed", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                Log.e(TAG, "Google sign in failed", e);
                handleSignInError(e.getStatusCode());
            }
        }
    }

    private void handleSignInError(int statusCode) {
        String message;
        switch (statusCode) {
            case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                message = "Sign in cancelled";
                break;
            case GoogleSignInStatusCodes.NETWORK_ERROR:
                message = "Network error occurred";
                break;
            case 10: // DEVELOPER_ERROR
                message = "Developer error: Check your Google Sign In configuration";
                break;
            default:
                message = "Sign in failed: " + statusCode;
        }
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Log.e(TAG, "Error: ID Token is null");
            Toast.makeText(this, "Error: Unable to sign in", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            String photo = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";

            User firebaseUser = new User(user.getUid(), name, photo, "unknown");

            database.getReference()
                    .child("profiles")
                    .child(user.getUid())
                    .setValue(firebaseUser)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                goToNextActivity();
                            } else {
                                Log.e(TAG, "Failed to save user profile", task.getException());
                                Toast.makeText(LoginActivity.this, "Failed to save user profile", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
        }
    }
}