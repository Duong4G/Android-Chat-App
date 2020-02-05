package com.example.phiduongchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    EditText mSendMailEditText;
    Button mResetButton;

    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.reset));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSendMailEditText = findViewById(R.id.send_email);
        mResetButton = findViewById(R.id.button_reset);
        mResetButton.setEnabled(true);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResetButton.setEnabled(false);
                String email = mSendMailEditText.getText().toString();
                if (email.equals("")){
                    Toast.makeText(ResetPasswordActivity.this, getResources().getString(R.string.all_fields_are_required), Toast.LENGTH_SHORT).show();
                    mResetButton.setEnabled(true);
                }else{
                    mFirebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ResetPasswordActivity.this, getResources().getString(R.string.please_check_your_email), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPasswordActivity.this,LoginActivity.class));
                            }else{
                                String error = task.getException().getMessage();
                                Toast.makeText(ResetPasswordActivity.this, error, Toast.LENGTH_SHORT).show();
                                mResetButton.setEnabled(true);
                            }
                        }
                    });
                }
            }
        });
    }
}
