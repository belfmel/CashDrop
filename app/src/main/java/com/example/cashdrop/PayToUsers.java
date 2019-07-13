package com.example.cashdrop;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PayToUsers extends AppCompatActivity {
    TextView user;
    Button payToBtn;

    // For Database
    private DatabaseReference mDatabaseReference;
    //private DatabaseReference otherDatabaseReference;

    //user email
    private String userEmail;
    private String tempEmail;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_to_users);

        mAuth = FirebaseAuth.getInstance();

        user = (TextView) findViewById(R.id.userToPay);
        String userName = getIntent().getStringExtra("Username");
        user.setText(userName);

        payToBtn = (Button) findViewById(R.id.payUserBtn);

        payToBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userEmail = currentUser.getEmail();
            // FireBase Database
            Log.d("USER_EMAIL", userEmail);
            tempEmail = userEmail.replace('.', '-');
            mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users/" + tempEmail);
        }


    }


}
