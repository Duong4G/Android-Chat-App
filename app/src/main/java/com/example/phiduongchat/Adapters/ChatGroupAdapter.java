package com.example.phiduongchat.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.phiduongchat.Models.ChatGroup;
import com.example.phiduongchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ChatGroupAdapter extends RecyclerView.Adapter<ChatGroupAdapter.ViewHolder> {
    private Context mContext;
    private List<ChatGroup> mChats;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mReference;

    public ChatGroupAdapter(Context mContext, List<ChatGroup> mChats){
        this.mContext = mContext;
        this.mChats = mChats;

    }

    @NonNull
    @Override
    public ChatGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item,parent,false);
        return new ChatGroupAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatGroupAdapter.ViewHolder holder, int position) {
        final ChatGroup chatGroup = mChats.get(position);
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mReference = FirebaseDatabase.getInstance().getReference();

        if (chatGroup.getMessage()!=null){
            holder.mShowMessageTextView.setText(chatGroup.getMessage());
        }
        if (chatGroup.getPhotoUrl()!=null){
            String photoUrl = chatGroup.getPhotoUrl();
            if (photoUrl.startsWith("gs://")){
                StorageReference storageReference = FirebaseStorage.getInstance()
                        .getReferenceFromUrl(photoUrl);
                storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            String downloadUrl = task.getResult().toString();
                            Glide.with(holder.mMessageImageView.getContext())
                                    .load(downloadUrl).into(holder.mMessageImageView);
                        }else{
                            Toast.makeText(mContext,mContext.getResources().getString(R.string.download_url_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else{
                Glide.with(holder.mMessageImageView.getContext())
                        .load(chatGroup.getPhotoUrl())
                        .into(holder.mMessageImageView);
            }
            holder.mMessageImageView.setVisibility(ImageView.VISIBLE);
            holder.mShowMessageTextView.setVisibility(TextView.GONE);
        }

        if (chatGroup.getImageUrl().equals("default")){
            holder.mProfileImageView.setImageResource(R.mipmap.ic_launcher);
        }else{
            Glide.with(mContext).load(chatGroup.getImageUrl()).into(holder.mProfileImageView);
        }
        holder.mShowMessengerTextView.setText(chatGroup.getName());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mFirebaseUser.getUid().equals(chatGroup.getSender())){
                    new AlertDialog.Builder(mContext)
                            .setTitle(mContext.getResources().getString(R.string.delete_message))
                            .setMessage(mContext.getResources().getString(R.string.are_you_sure_you_want_to_delete_this_message))
                            .setPositiveButton(mContext.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mReference.child("Chatgroup").child(chatGroup.getKey()).removeValue();
                                }
                            })
                            .setNegativeButton(mContext.getResources().getString(R.string.no), null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mShowMessageTextView;
        public ImageView mProfileImageView;
        public ImageView mMessageImageView;
        public TextView mShowMessengerTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mShowMessageTextView = itemView.findViewById(R.id.show_message);
            mShowMessengerTextView = itemView.findViewById(R.id.show_messenger);
            mProfileImageView = itemView.findViewById(R.id.profile_image);
            mMessageImageView = itemView.findViewById(R.id.messageImageView);
        }
    }
}
