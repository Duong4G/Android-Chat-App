package com.example.phiduongchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.phiduongchat.Adapters.MessageAdapter;
import com.example.phiduongchat.Models.Chat;
import com.example.phiduongchat.Models.User;
import com.example.phiduongchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    private CircleImageView mProfileImageView;
    private TextView mUsernameTextView;
    private ImageButton mSendButton;
    private ImageButton mSendImageButton;
    private EditText mSendEditText;
    private MessageAdapter mMessageAdapter;
    private List<Chat> mChats;
    private RecyclerView mRecyclerView;
    private String mUserId;
    private String mKey;
    private boolean mIsLoad = false;
    private static final String TAG = "MessageActivity";
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "gs://phi-duong-app.appspot.com/loading.gif";

    private FirebaseUser mFirebaseUser;
    private DatabaseReference mReference;
    private Intent intent;

    private ValueEventListener mSeenListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mProfileImageView = findViewById(R.id.profile_image);
        mUsernameTextView = findViewById(R.id.username);
        mSendButton = findViewById(R.id.button_send);
        mSendImageButton = findViewById(R.id.button_send_image);
        mSendEditText = findViewById(R.id.text_send);
        mSendImageButton.setEnabled(true);
        mSendButton.setEnabled(true);

        intent = getIntent();
        mUserId = intent.getStringExtra("userid");
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendButton.setEnabled(false);
                String msg = mSendEditText.getText().toString();
                if (!msg.equals("")){
                    sendMessage(mFirebaseUser.getUid(),mUserId,msg);
                }else{
                    Toast.makeText(MessageActivity.this, getResources().getString(R.string.you_cannot_send_empty_message), Toast.LENGTH_SHORT).show();
                }
                mSendEditText.setText("");
                mSendButton.setEnabled(true);
            }
        });

        mSendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendImageButton.setEnabled(false);
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent,REQUEST_IMAGE);
            }
        });

        mReference = FirebaseDatabase.getInstance().getReference("Users").child(mUserId);

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mUsernameTextView.setText(user.getUsername());

                if (user.getImageURL().equals("default")){
                    mProfileImageView.setImageResource(R.mipmap.ic_launcher);
                }else{
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(mProfileImageView);

                }

                readMessage(mFirebaseUser.getUid(),mUserId,user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(mUserId);
    }
    private void seenMessage(final String userId){
        mReference = FirebaseDatabase.getInstance().getReference("Chats");
        mSeenListener = mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

                    if (chat.getReceiver().equals(mFirebaseUser.getUid()) && chat.getSender().equals(userId)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message){
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("photoUrl",null);
        hashMap.put("isseen",false);

        reference.child("Chats").push().setValue(hashMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                String key = databaseReference.getKey();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("key",databaseReference.getKey());
                assert key != null;
                reference.child("Chats").child(key).updateChildren(hashMap);
            }
        });

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(mFirebaseUser.getUid())
                .child(mUserId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(mUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(mUserId)
                .child(mFirebaseUser.getUid());

        chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef2.child("id").setValue(mFirebaseUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void readMessage(final String myId, final String userId, final String imageUrl){
        mChats = new ArrayList<>();

        mReference = FirebaseDatabase.getInstance().getReference("Chats");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myId) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(myId)){
                        mChats.add(chat);
                    }
                }
                mMessageAdapter = new MessageAdapter(MessageActivity.this,mChats,imageUrl);
                mRecyclerView.setAdapter(mMessageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void status(String status){
        mReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        mReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mReference.removeEventListener(mSeenListener);
        status("offline");
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        if (mIsLoad){
            reference.child("Chats").child(mKey).removeValue();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG,"onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == REQUEST_IMAGE){
            if (resultCode == RESULT_OK){
                if (data!=null){
                    mIsLoad = true;
                    final Uri uri = data.getData();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("sender",mFirebaseUser.getUid());
                    hashMap.put("receiver",mUserId);
                    hashMap.put("message",null);
                    hashMap.put("photoUrl",LOADING_IMAGE_URL);
                    hashMap.put("isseen",false);

                    reference.child("Chats").push().setValue(hashMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null){
                                mKey = databaseReference.getKey();
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference("images").child(mFirebaseUser.getUid())
                                        .child(mKey).child(uri.getLastPathSegment());
                                putImageInStorage(storageReference,uri,mKey);
                            }else{
                                Log.w(TAG,"Unable to write message to database",databaseError.toException());
                            }
                        }
                    });
                }
            }else{
                mSendImageButton.setEnabled(true);
            }
        }
    }
    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key){
        storageReference.putFile(uri).addOnCompleteListener(MessageActivity.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                if (task.isSuccessful()){
                    task.getResult().getMetadata().getReference().getDownloadUrl()
                            .addOnCompleteListener(MessageActivity.this, new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()){
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("sender",mFirebaseUser.getUid());
                                        hashMap.put("receiver",mUserId);
                                        hashMap.put("message",null);
                                        hashMap.put("photoUrl",task.getResult().toString());
                                        hashMap.put("isseen",false);

                                        reference.child("Chats").child(key)
                                                .setValue(hashMap, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                        String key = databaseReference.getKey();
                                                        HashMap<String, Object> hashMap = new HashMap<>();
                                                        hashMap.put("key",databaseReference.getKey());
                                                        assert key != null;
                                                        reference.child("Chats").child(key).updateChildren(hashMap);
                                                    }
                                                });
                                        mSendImageButton.setEnabled(true);
                                        mIsLoad = false;
                                    }
                                }
                            });
                }else{
                    Log.w(TAG,"Image upload task was not successful.",task.getException());
                }

                final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                        .child(mFirebaseUser.getUid())
                        .child(mUserId);

                chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){
                            chatRef.child("id").setValue(mUserId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist")
                        .child(mUserId)
                        .child(mFirebaseUser.getUid());

                chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){
                            chatRef2.child("id").setValue(mFirebaseUser.getUid());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}
