package com.example.phiduongchat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.phiduongchat.MessageActivity;
import com.example.phiduongchat.Models.Chat;
import com.example.phiduongchat.Models.User;
import com.example.phiduongchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    private boolean isChat;
    private DatabaseReference mReference;

    private String mLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, boolean isChat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mReference = FirebaseDatabase.getInstance().getReference();
        final User user = mUsers.get(position);
        holder.mUsernameTextView.setText(user.getUsername());
        if (user.getImageURL().equals("default")) {
            holder.mProfileImageView.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.mProfileImageView);
        }

        if (isChat){
            lastMessage(user.getId(),holder.mLastMessageTextView);
        }else{
            holder.mLastMessageTextView.setVisibility(View.GONE);
        }

        if (isChat) {
            if (user.getStatus().equals("online")) {
                holder.mOnImageView.setVisibility(View.VISIBLE);
                holder.mOffImageView.setVisibility(View.GONE);
            } else {
                holder.mOnImageView.setVisibility(View.GONE);
                holder.mOffImageView.setVisibility(View.VISIBLE);
            }
        } else {
            holder.mOnImageView.setVisibility(View.GONE);
            holder.mOffImageView.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mUsernameTextView;
        public ImageView mProfileImageView;
        private ImageView mOnImageView;
        private ImageView mOffImageView;
        private TextView mLastMessageTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mUsernameTextView = itemView.findViewById(R.id.username);
            mProfileImageView = itemView.findViewById(R.id.profile_image);
            mOnImageView = itemView.findViewById(R.id.image_on);
            mOffImageView = itemView.findViewById(R.id.image_off);
            mLastMessageTextView = itemView.findViewById(R.id.last_message);
        }
    }

    private void lastMessage(final String userId, final TextView last_message) {
        mLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        if (firebaseUser!=null){
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);
                        assert chat != null;
                        assert firebaseUser != null;
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId) ||
                                chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())) {
                            mLastMessage = chat.getMessage();
                        }
                    }

                    if (mLastMessage!=null){
                        switch (mLastMessage) {
                            case "default":
                                last_message.setText(mContext.getResources().getString(R.string.no_message));
                                break;
                            default:
                                last_message.setText(mLastMessage);
                                break;

                        }
                    }else{
                        last_message.setText("hình ảnh/*");
                    }

                    mLastMessage = "default";

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
