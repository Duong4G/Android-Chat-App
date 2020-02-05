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
import com.example.phiduongchat.Models.Chat;
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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChats;
    private String mImageUrl;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mReference;

    public MessageAdapter(Context mContext, List<Chat> mChats, String mImageUrl){
        this.mContext = mContext;
        this.mChats = mChats;
        this.mImageUrl = mImageUrl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder holder, int position) {
        final Chat chat = mChats.get(position);
        mReference = FirebaseDatabase.getInstance().getReference();
        if (chat.getMessage()!=null){
            holder.mShowMessageTextView.setText(chat.getMessage());
        }
        if (chat.getPhotoUrl()!=null){
            String photoUrl = chat.getPhotoUrl();
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
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.download_url_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else{
                Glide.with(holder.mMessageImageView.getContext())
                        .load(chat.getPhotoUrl())
                        .into(holder.mMessageImageView);
            }
            holder.mMessageImageView.setVisibility(ImageView.VISIBLE);
            holder.mShowMessageTextView.setVisibility(TextView.GONE);
        }

        if (mImageUrl.equals("default")){
            holder.mProfileImageView.setImageResource(R.mipmap.ic_launcher);
        }else{
            Glide.with(mContext).load(mImageUrl).into(holder.mProfileImageView);
        }

        if (position == mChats.size() - 1){
            if (chat.isIsseen()){
                holder.mSeenTextView.setText(mContext.getResources().getString(R.string.seen));
            }else{
                holder.mSeenTextView.setText(mContext.getResources().getString(R.string.delivered));
            }
        }else{
            holder.mSeenTextView.setVisibility(View.GONE);
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mFirebaseUser.getUid().equals(chat.getSender())){
                    new AlertDialog.Builder(mContext)
                            .setTitle(mContext.getResources().getString(R.string.delete_message))
                            .setMessage(mContext.getResources().getString(R.string.are_you_sure_you_want_to_delete_this_message))
                            .setPositiveButton(mContext.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    mReference.child("Chats").child(chat.getKey()).removeValue();
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
        public TextView mSeenTextView;
        public ImageView mMessageImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mShowMessageTextView = itemView.findViewById(R.id.show_message);
            mProfileImageView = itemView.findViewById(R.id.profile_image);
            mSeenTextView = itemView.findViewById(R.id.text_seen);
            mMessageImageView = itemView.findViewById(R.id.messageImageView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChats.get(position).getSender().equals(mFirebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }
}
