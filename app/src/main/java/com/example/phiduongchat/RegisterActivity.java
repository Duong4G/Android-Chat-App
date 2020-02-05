package com.example.phiduongchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private MaterialEditText mUsernameEditText,mEmailEditText,mPasswordEditText,mRepeatPasswordEditText;
    private Button mRegisterButton;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsernameEditText = findViewById(R.id.username);
        mEmailEditText = findViewById(R.id.email);
        mPasswordEditText = findViewById(R.id.password);
        mRepeatPasswordEditText = findViewById(R.id.repeat_password);
        mRegisterButton = findViewById(R.id.button_register);

        mFirebaseAuth = FirebaseAuth.getInstance();

        mRegisterButton.setEnabled(true);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRegisterButton.setEnabled(false);
                String textUsername = mUsernameEditText.getText().toString();
                String textEmail = mEmailEditText.getText().toString();
                String textPassword = mPasswordEditText.getText().toString();
                String textRepeatPassword = mRepeatPasswordEditText.getText().toString();

                if (TextUtils.isEmpty(textUsername) || TextUtils.isEmpty(textEmail) || TextUtils.isEmpty(textPassword)){
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.all_fields_are_required), Toast.LENGTH_SHORT).show();
                    mRegisterButton.setEnabled(true);
                }else if (textPassword.length() < 6){
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.password_at_least_6_character), Toast.LENGTH_SHORT).show();
                    mRegisterButton.setEnabled(true);
                }else if (!textPassword.equals(textRepeatPassword)){
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.repeat_password_failed), Toast.LENGTH_SHORT).show();
                    mRegisterButton.setEnabled(true);
                } else{
                    register(textUsername,textEmail,textPassword);
                }
            }
        });
    }
    private void register(final String username, String email, String password){
        mFirebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                            assert firebaseUser != null;
                            String userId = firebaseUser.getUid();

                            mReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("id",userId);
                            hashMap.put("username",username);
                            hashMap.put("imageURL","default");
                            hashMap.put("search",username.toLowerCase());

                            mReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(RegisterActivity.this, getResources().getString(R.string.you_cannot_register_with_this_email_or_password), Toast.LENGTH_SHORT).show();
                            mRegisterButton.setEnabled(true);
                        }
                    }
                });
    }
}
