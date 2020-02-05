package com.example.phiduongchat.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.phiduongchat.Adapters.ChatGroupAdapter;
import com.example.phiduongchat.Models.ChatGroup;
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

import static android.app.Activity.RESULT_OK;


public class ChatGroupFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private ImageButton mSendButton;
    private ImageButton mSendImageButton;
    private EditText mSendEditText;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mReference;
    private ChatGroupAdapter mChatGroupAdapter;
    private List<ChatGroup> mChats;
    private static final String TAG = "ChatGroupFragment";
    private boolean mIsLoad = false;
    private String mImageUrl;
    private String mName;
    private String mKey;
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "gs://phi-duong-app.appspot.com/loading.gif";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_group,container,false);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mSendButton = view.findViewById(R.id.button_send);
        mSendImageButton = view.findViewById(R.id.button_send_image);
        mSendEditText = view.findViewById(R.id.text_send);
        mSendImageButton.setEnabled(true);
        mSendButton.setEnabled(true);

        mReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());

        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mImageUrl = user.getImageURL();
                mName = user.getUsername();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendButton.setEnabled(false);
                String msg = mSendEditText.getText().toString();
                if (!msg.equals("")){
                    sendMessage(mName,msg,mImageUrl);
                }else{
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.you_cannot_send_empty_message), Toast.LENGTH_SHORT).show();
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

        readMessage();
        return view;
    }
    private void sendMessage(String name, String message, String imageUrl) {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name",name);
        hashMap.put("message",message);
        hashMap.put("sender",mFirebaseUser.getUid());
        //Avatar
        hashMap.put("imageUrl",imageUrl);
        //Send
        hashMap.put("photoUrl",null);

        reference.child("Chatgroup").push().setValue(hashMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                String key = databaseReference.getKey();
                HashMap<String,Object> hashMap1 = new HashMap<>();
                hashMap1.put("key",key);
                assert key != null;
                reference.child("Chatgroup").child(key).updateChildren(hashMap1);
            }
        });
    }

    private void readMessage(){
        mChats = new ArrayList<>();

        mReference = FirebaseDatabase.getInstance().getReference("Chatgroup");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChats.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatGroup chatGroup = snapshot.getValue(ChatGroup.class);
                    mChats.add(chatGroup);
                }

                mChatGroupAdapter = new ChatGroupAdapter(getContext(),mChats);
                mRecyclerView.setAdapter(mChatGroupAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG,"onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == REQUEST_IMAGE){
            if (resultCode == RESULT_OK){
                if (data!=null){
                    mIsLoad = true;
                    final Uri uri = data.getData();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("name",mName);
                    hashMap.put("message",null);
                    hashMap.put("imageUrl",mImageUrl);
                    hashMap.put("photoUrl",LOADING_IMAGE_URL);


                    reference.child("Chatgroup").push().setValue(hashMap, new DatabaseReference.CompletionListener() {
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
        storageReference.putFile(uri).addOnCompleteListener(getActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                if (task.isSuccessful()){
                    task.getResult().getMetadata().getReference().getDownloadUrl()
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()){
                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("name",mName);
                                        hashMap.put("message",null);
                                        hashMap.put("sender",mFirebaseUser.getUid());
                                        hashMap.put("imageUrl",mImageUrl);
                                        hashMap.put("photoUrl",task.getResult().toString());

                                        reference.child("Chatgroup").child(key)
                                                .setValue(hashMap, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                        String key = databaseReference.getKey();
                                                        HashMap<String,Object> hashMap1 = new HashMap<>();
                                                        hashMap1.put("key",key);
                                                        assert key != null;
                                                        reference.child("Chatgroup").child(key).updateChildren(hashMap1);
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
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        if (mIsLoad){
            reference.child("Chatgroup").child(mKey).removeValue();
        }
    }
}
