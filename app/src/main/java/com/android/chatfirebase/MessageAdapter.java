package com.android.chatfirebase;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by TRANTUAN on 28-Dec-17.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private FirebaseAuth mAuth;
    private DatabaseReference mDataRef;

    private List<AllMessage> allMessages;

    public MessageAdapter(List<AllMessage> allMessages) {
        this.allMessages = allMessages;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView tvMessage, tvTime, tvMessageReceiver, tvTimeReceiver;
        public CircleImageView thumbImage, thumbImageReceiver;

        public MessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_text_message);
            tvTime = itemView.findViewById(R.id.tv_time_message);
            thumbImage = itemView.findViewById(R.id.thumb_image_message);
            tvMessageReceiver = itemView.findViewById(R.id.tv_text_message_receiver);
            tvTimeReceiver = itemView.findViewById(R.id.tv_time_message_receiver);
            thumbImageReceiver = itemView.findViewById(R.id.thumb_image_message_receiver);
        }
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {
        AllMessage message = allMessages.get(position);


        mAuth = FirebaseAuth.getInstance();
        String mSendId = mAuth.getCurrentUser().getUid();
        String fromSendID = message.getFrom();

        if (!TextUtils.isEmpty(fromSendID)) {
            mDataRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromSendID);

            if (mSendId.equals(fromSendID)) {
                holder.tvMessage.setVisibility(View.VISIBLE);
                holder.thumbImage.setVisibility(View.VISIBLE);
                holder.tvMessage.setText(message.getMessage());
                mDataRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       final String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();
                        Picasso.with(holder.thumbImage.getContext()).load(thumb_image)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.ic_profile)
                                .into(holder.thumbImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(holder.thumbImage.getContext())
                                                .load(thumb_image)
                                                .placeholder(R.drawable.ic_profile)
                                                .into(holder.thumbImage);
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            } else {
                holder.tvMessageReceiver.setVisibility(View.VISIBLE);
                holder.thumbImageReceiver.setVisibility(View.VISIBLE);
                holder.tvMessageReceiver.setText(message.getMessage());
                mDataRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();
                        Picasso.with(holder.thumbImageReceiver.getContext()).load(thumb_image)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.ic_profile)
                                .into(holder.thumbImageReceiver, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(holder.thumbImageReceiver.getContext())
                                                .load(thumb_image)
                                                .placeholder(R.drawable.ic_profile)
                                                .into(holder.thumbImageReceiver);
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

    }

    @Override
    public int getItemCount() {
        return allMessages.size();
    }


}
