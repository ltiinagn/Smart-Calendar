package com.java.weekview.sample;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import android.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private TextView textViewSignUpTitle;
    private EditText editTextNewUsername, editTextNewPassword, editTextNewEmail;
    private Spinner spinnerAuth;
    private Button buttonSignUpNew;
    private String name, password, email, auth;
    private FirebaseAuth firebaseAuth;
    private ImageView imageViewProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        textViewSignUpTitle = findViewById(R.id.textViewSignUpTitle);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        editTextNewUsername = findViewById(R.id.editTextNewUsername);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextNewEmail = findViewById(R.id.editTextNewEmail);
        spinnerAuth = findViewById(R.id.editTextAuth);
        buttonSignUpNew = findViewById(R.id.buttonSignUpNew);

        firebaseAuth = FirebaseAuth.getInstance();

        buttonSignUpNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = editTextNewUsername.getText().toString();
                password = editTextNewPassword.getText().toString();
                email = editTextNewEmail.getText().toString();
                auth = spinnerAuth.getSelectedItem().toString();

                if (name.isEmpty() || password.isEmpty() || email.isEmpty() || auth.isEmpty())
                    Toast.makeText(SignUpActivity.this, "Please enter all the details", Toast.LENGTH_SHORT).show();
                else {
                    firebaseAuth.createUserWithEmailAndPassword(email,password).
                            addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        sendUserData();
                                        Toast.makeText(SignUpActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                        Log.d("Tomato_Knight", "createUserWithEmail:success");
                                        firebaseAuth.signOut();
                                        finish();
                                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                    } else
                                        Toast.makeText(SignUpActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();

                                }
                            });
                }

            }
        });

    }

    private void getStrings(){
        name = editTextNewUsername.getText().toString();
        password = editTextNewPassword.getText().toString();
        email = editTextNewEmail.getText().toString();
        auth = spinnerAuth.getSelectedItem().toString();
    }

    private void sendUserData(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference ref = firebaseDatabase.getReference(firebaseAuth.getUid());
        ref.setValue(new UserProfile(name, email, auth));
    }
}
