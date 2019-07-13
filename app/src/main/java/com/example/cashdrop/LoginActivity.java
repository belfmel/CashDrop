package com.example.cashdrop;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    // TAG for logCat
    private final String TAG = "FireBase_SIGNIN";

    // UI elements
    private EditText mEmailView;
    private EditText mPasswordView;

//    private DatabaseReference mDatabaseReference;

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref;

    private String userEmail;
    private String tempEmail;

    /*
     * FireBase Auth variables
     */
    // reference for the fireBase auth library
    private FirebaseAuth mAuth;
    // Listener that detects changes in the user's current authentication state
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_login);

        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        // Set up click handlers and view item references
        findViewById(R.id.btnCreate).setOnClickListener(this);
        findViewById(R.id.btnSignIn).setOnClickListener(this);

        //get a reference to the FireBase auth object
        mAuth = FirebaseAuth.getInstance();

        //then attach an auth listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // This will get a reference to the current user
                FirebaseUser user = mAuth.getCurrentUser();
                // if user is not currently signed in, it will be null
                if (user != null) {
                    // if signing in was successful then go to navigation page
                    Intent navIntent = new Intent(LoginActivity.this, FindUsers.class);
                    startActivity(navIntent);

                } else {
                    // user is signed out
                    Log.d("FIREBASE", "Currently signed out");
                }
            }
        };

    }

    /**
     * When the activity starts and stops, the app needs to connect and disconnect the
     * AuthListener
     */
    @Override
    public void onStart() {
        super.onStart();
        //make sure the Auth state listener is active
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userEmail = currentUser.getEmail();
            // FireBase Database
            Log.d("USER_EMAIL", userEmail);
            tempEmail = userEmail.replace('.', '-');
            ref = database.getReference("Users/" + tempEmail);

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
    }

    /**
     * On click button listener
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignIn:
                signUserIn();
                break;

            case R.id.btnCreate:
                createUserAccount();
                break;
        }
    }

    /**
     * Makes sure user submits an email & a password
     *
     * @return true if they submitted both
     */
    private boolean checkFormFields() {
        String email, password;

        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();

        if (email.isEmpty()) {
            mEmailView.setError("Email Required");
            return false;
        }
        if (password.isEmpty()) {
            mPasswordView.setError("Password Required");
            return false;
        }
        return true;
    }

    /**
     * Update the status of the user
     * if they are logged in, etc.
     */
    private void updateStatus() {
        TextView tvStat = (TextView) findViewById(R.id.tvSignInStatus); //to show status of log in
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            //signed in user.getEmail();
            tvStat.setText("Signed in: " + user.getEmail());
        } else {
            tvStat.setText("Signed Out");
        }
    }

    private void updateStatus(String stat) {
        TextView tvStat = (TextView) findViewById(R.id.tvSignInStatus);
        tvStat.setText(stat);
    }

    /**
     * Create user account
     */
    private void createUserAccount() {
        if (!checkFormFields()) {
            return;
        }

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();


        // Create User account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //check if task was successful
                        if (task.isSuccessful()) {
                            // This will get a reference to the current user
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "User created", Toast.LENGTH_SHORT).show();
                            DatabaseReference defaultPost = ref.child("Balance");
                            defaultPost.setValue("0"); //when create account balance is $0
                            clean();
                        } else {
                            Toast.makeText(LoginActivity.this, "Account creation failed", Toast.LENGTH_SHORT).show();
                            clean();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            updateStatus("This email address is already in use.");
                        } else {
                            updateStatus(e.getLocalizedMessage());
                        }
                    }
                });
    }

    /**
     * Sign user in
     */
    private void signUserIn() {
        // checks if the forms are filled out
        if (!checkFormFields()) {
            return;
        }

        // Gets the values in the text boxes
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Signed in", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Sign in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            updateStatus("Invalid password.");
                        } else if (e instanceof FirebaseAuthInvalidUserException) {
                            updateStatus("No account with this email.");
                        } else {
                            updateStatus(e.getLocalizedMessage());
                        }
                    }
                });
    }


    /**
     * Clean form out
     */
    private void clean() {
        mEmailView.setText("");
        mPasswordView.setText("");
    }


}
