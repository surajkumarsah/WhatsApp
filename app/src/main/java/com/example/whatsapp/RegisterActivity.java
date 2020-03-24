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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText RegEmail, RegPassword;
    private TextView AlreadyHaveAnAcc;
    private Button RegBtn;
    private FirebaseAuth mAuth;
    String email, password;
    private ProgressDialog loadingbar;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        initializeField();


        RegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAcc();
            }
        });

        AlreadyHaveAnAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });




}

    private void validateAcc()
    {

        email = RegEmail.getText().toString().trim();
        password = RegPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email))
        {
            RegEmail.setError("Please, Enter EmailId");
            RegEmail.requestFocus();
            finish();
        }
        else if (TextUtils.isEmpty(password))
        {
            RegPassword.setError("Please, Enter Password");
            RegPassword.requestFocus();
            finish();
        }
        else
        {
            loadingbar.setTitle("Creating New Account");
            loadingbar.setMessage("Please wait, while we are creating new Account for you.");
            loadingbar.setCanceledOnTouchOutside(true);
            loadingbar.show();

            AddAccToDB();
        }
    }

    private void AddAccToDB()
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            String currentUserId = mAuth.getCurrentUser().getUid();
                            rootRef.child("Users").child(currentUserId).setValue("");

                            Toast.makeText(RegisterActivity.this, "Account Created Successfully.", Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                            sendUserToMainActivity();
                        }
                        else {
                            String message = task.getException().toString();
                            Toast.makeText(RegisterActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                        }
                    }
                });
    }

    private void sendUserToMainActivity()
    {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initializeField()
    {
        RegEmail = (EditText) findViewById(R.id.reg_gmail);
        RegPassword = (EditText) findViewById(R.id.reg_password);
        AlreadyHaveAnAcc = (TextView) findViewById(R.id.already_have_account);
        RegBtn = (Button) findViewById(R.id.reg_btn);

        loadingbar = new ProgressDialog(this);
    }

    private void sendUserToLoginActivity()
    {
        Intent intent = new Intent(RegisterActivity.this, Login_Activity.class);
        startActivity(intent);
    }

}
