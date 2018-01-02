package com.android.chatfirebase;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private String userNameFriend, userIDFriend, userID;
    private TextView tvNameFriend, tvLastScreenFriend;
    private CircleImageView imageViewFriend;
    private DatabaseReference mDataRefUser,mDataRefMessage;
    private EditText edtMessage;
    private ImageButton imageButtonPhoto, imageButtonSendMessage;
    private FirebaseAuth mAuth;
    private RecyclerView mRecyclerViewMessage;
    private MessageAdapter messageAdapter;
    private List<AllMessage> messageList = new ArrayList<>();




    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDataRefUser = FirebaseDatabase.getInstance().getReference();
        mDataRefMessage = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        setContentView(R.layout.activity_chat);

        mToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle(null);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_view = inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_view);

        tvNameFriend = findViewById(R.id.custom_name_chat);
        tvLastScreenFriend = findViewById(R.id.custom_last_name);
        imageViewFriend = findViewById(R.id.image_custom_chat);
        edtMessage = findViewById(R.id.edt_text_chat);
        imageButtonPhoto = findViewById(R.id.image_btn_photo_chat);
        imageButtonSendMessage = findViewById(R.id.image_btn_send_chat);

        mRecyclerViewMessage = findViewById(R.id.recycle_view_message);
        mRecyclerViewMessage.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerViewMessage.setLayoutManager(linearLayoutManager);

        messageAdapter = new MessageAdapter(messageList);
        mRecyclerViewMessage.setAdapter(messageAdapter);

        if (getIntent() != null){
           viewCustomChatToolbar();

        }

        imageButtonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSendMessage();
            }
        });
        FecthMessage();

    }

    private void FecthMessage() {
        mDataRefMessage.child("Messages").child(userID).child(userIDFriend)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        AllMessage message = dataSnapshot.getValue(AllMessage.class);
                        messageList.add(message);
                        messageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void viewCustomChatToolbar() {
        userNameFriend = getIntent().getStringExtra("NAME_FRIEND");
        userIDFriend = getIntent().getStringExtra("VISIT_USER_ID");
        tvNameFriend.setText(userNameFriend);
        mDataRefUser.child("Users").child(userIDFriend).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String status_online = dataSnapshot.child("online").getValue().toString();
                final String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                Picasso.with(imageViewFriend.getContext()).load(thumb_image)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_profile)
                        .into(imageViewFriend, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                Picasso.with(imageViewFriend.getContext())
                                        .load(thumb_image)
                                        .placeholder(R.drawable.ic_profile)
                                        .into(imageViewFriend);
                            }
                        });
                if (status_online.equals("true"))
                {
                    tvLastScreenFriend.setText("online");
                }else{
                    LastSeenTime lastSeenTime = new LastSeenTime();
                    long last_seen = Long.parseLong(status_online);
                    String lastSeen = lastSeenTime.getTimeAgo(last_seen);
                    tvLastScreenFriend.setText(lastSeen);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void mSendMessage() {
        String mMessage = edtMessage.getText().toString();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (!TextUtils.isEmpty(mMessage) && firebaseUser!=null){


            String message_sender = "Messages/"+userID+"/"+userIDFriend;
            String message_receiver = "Messages/"+userIDFriend+"/"+userID;

            DatabaseReference userMessageKey = mDataRefMessage.child("Messages").child(userID).child(userIDFriend).push();

            String messagePushID = userMessageKey.getKey();

            Map messageBody = new HashMap();
            messageBody.put("message",mMessage);
            messageBody.put("seen",false);
            messageBody.put("type","text");
            messageBody.put("time", ServerValue.TIMESTAMP);
            messageBody.put("from", userID);

            Map messageDetail = new HashMap();
            messageDetail.put(message_sender+"/"+messagePushID,messageBody);
            messageDetail.put(message_receiver+"/"+messagePushID,messageBody);

            mDataRefMessage.updateChildren(messageDetail, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d("TAG", "onComplete: databaseError");
                    }
                    edtMessage.setText("");
                }
            });
        }
    }
}
