package com.example.phiduongchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ChangeUserNameActivity extends AppCompatActivity {
    private EditText mSendUsernameEditText;
    private Button mChangeButton;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_name);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.change_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSendUsernameEditText = findViewById(R.id.send_username);
        mChangeButton = findViewById(R.id.button_change);
        mChangeButton.setEnabled(true);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChangeButton.setEnabled(false);
                String username = mSendUsernameEditText.getText().toString();
                if (username.equals("")){
                    Toast.makeText(ChangeUserNameActivity.this, getResources().getString(R.string.all_fields_are_required), Toast.LENGTH_SHORT).show();
                    mChangeButton.setEnabled(true);
                }else{
                    mReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("username",username);
                    hashMap.put("search",username.toLowerCase());

                    mReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ChangeUserNameActivity.this, getResources().getString(R.string.successful), Toast.LENGTH_SHORT).show();
                                mChangeButton.setEnabled(true);
                            }
                        }
                    });
                }

            }
        });
    }
}
