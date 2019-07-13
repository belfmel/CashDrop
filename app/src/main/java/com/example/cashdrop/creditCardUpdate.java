package com.example.cashdrop;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class creditCardUpdate extends AppCompatActivity {

    private DatabaseReference mDatabaseReference;

    // reference for the fireBase auth library
    private FirebaseAuth mAuth;

    String userEmail;
    String tempEmail;

    EditText creditCardField;
    EditText expDateField;
    Button submitInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_card_update);

        //get a reference to the FireBase auth object
        mAuth = FirebaseAuth.getInstance();

        creditCardField = findViewById(R.id.creditCardField);
        expDateField = findViewById(R.id.expDateField);

        submitInfo = findViewById(R.id.submitCC);

        submitInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkFormFields()){
                    // get data
                    String cc, exp;

                    cc = creditCardField.getText().toString();
                    exp = expDateField.getText().toString();
                    // store it in database
                    startActivity(new Intent(creditCardUpdate.this, FindUsers.class));
                }else{
                    Toast.makeText(creditCardUpdate.this, "Did not enter proper information", Toast.LENGTH_SHORT).show();
                }
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

    private boolean checkFormFields() {
        String cc, exp;

        cc = creditCardField.getText().toString();
        exp = expDateField.getText().toString();

        if (cc.isEmpty()) {
            creditCardField.setError("Credit Card Required");
            return false;
        }
        if (exp.isEmpty()) {
            expDateField.setError("Expiration Date Required");
            return false;
        }
        return true;
    }

}
