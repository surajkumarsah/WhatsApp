package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login_Activity extends AppCompatActivity
{

    private FirebaseUser currentUser;
    private Button LoginButton, PhoneLoginButton;
    private EditText UserEmail, UserPassword;
    private TextView NewAccLink, ForgetPassLink;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingbar;

    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        loadingbar = new ProgressDialog(this);


        initializeFields();

        NewAccLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login_Activity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateField();
            }
        });


    }

    private void validateField()
    {
        email = UserEmail.getText().toString().trim();
        password = UserPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email))
        {
            UserEmail.setError("Please, Enter emailId");
            UserEmail.requestFocus();
            finish();
        }

        else if (TextUtils.isEmpty(password))
        {
            UserPassword.setError("Please, Enter Password");
            UserPassword.requestFocus();
            finish();
        }
        else if(password.length() < 6)
        {
            UserPassword.setError("Length of password greater then 6.");
            UserPassword.requestFocus();
            finish();
        }
        else {
            loadingbar.setTitle("Logged in....");
            loadingbar.setMessage("Please wait, while we are checking credentials.");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();

            validateDataFromDB();
        }
    }

    private void validateDataFromDB()
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            loadingbar.dismiss();
                            sendUserToMainActivity();
                            Toast.makeText(Login_Activity.this, "Logged in successfully.", Toast.LENGTH_SHORT).show();
                        }
                        else {

                            String message = task.getException().toString();
                            Toast.makeText(Login_Activity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                        }
                    }
                });
    }

    private void initializeFields()
    {
        LoginButton = (Button) findViewById(R.id.login_btn);
        PhoneLoginButton = (Button) findViewById(R.id.phone_login_btn);
        UserEmail = (EditText) findViewById(R.id.login_gmail);
        UserPassword = (EditText) findViewById(R.id.login_password);
        NewAccLink = (TextView) findViewById(R.id.need_an_account);
        ForgetPassLink = (TextView) findViewById(R.id.forgot_password);
    }

    private void sendUserToMainActivity()
    {
        Intent intent = new Intent(Login_Activity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
