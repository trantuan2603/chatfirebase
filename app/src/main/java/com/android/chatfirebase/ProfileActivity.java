package com.android.chatfirebase;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView tvUserName, tvUserStatus;
    private CircleImageView circleImageViewProfile;
    private Button btnSendRequest, btnDeclineRequest;

    private DatabaseReference mDataRefUser, mDataRefFriend, mDataRefFriended, mDataRefNotify;
    private FirebaseAuth mAuth;
    private String mSenderUserID = "", mReceiderUserId = "", CURRENT_STATE = "not_friends";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvUserName = findViewById(R.id.tv_user_name_profile);
        tvUserStatus = findViewById(R.id.tv_status_profile);
        circleImageViewProfile = findViewById(R.id.profile_image);
        btnSendRequest = findViewById(R.id.btn_send_friend_request);
        btnDeclineRequest = findViewById(R.id.btn_decline_friend_request);
        btnDeclineRequest.setVisibility(View.GONE);

        mDataRefUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDataRefUser.keepSynced(true);
        mDataRefFriend = FirebaseDatabase.getInstance().getReference().child("Friend_Request");
        mDataRefFriend.keepSynced(true);
        mDataRefFriended = FirebaseDatabase.getInstance().getReference().child("Friends");
        mDataRefFriended.keepSynced(true);
        mDataRefNotify = FirebaseDatabase.getInstance().getReference().child("Notification");

        mAuth = FirebaseAuth.getInstance();
        mSenderUserID = mAuth.getCurrentUser().getUid();

        if (getIntent() != null) {
            mReceiderUserId = getIntent().getStringExtra("VISIT_USER_ID");
            checkUserReceicer();
        }
        if (!mSenderUserID.contains(mReceiderUserId)) {
            btnSendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnSendRequest.setEnabled(false);
                    if (CURRENT_STATE.equals("not_friends")) {
                        sendFriendRequestToAsFriend();
                    }
                    if (CURRENT_STATE.equals("request_sent")) {
                        cancelFriendReuest();
                    }
                    if (CURRENT_STATE.equals("request_received")) {
                        AcceptFriendReuest();
                    }
                    if (CURRENT_STATE.equals("friends")) {
                        unFriendPerson();
                    }
                }
            });

            btnDeclineRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    declineFriendRequest();
                }
            });


        } else {
            btnSendRequest.setVisibility(View.INVISIBLE);
            btnDeclineRequest.setVisibility(View.INVISIBLE);
        }


    }

    private void checkUserReceicer() {
        mDataRefUser.child(mReceiderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                final String imageProfile = dataSnapshot.child("user_image").getValue().toString();


                tvUserName.setText(name);
                tvUserStatus.setText(status);

                Picasso.with(circleImageViewProfile.getContext())
                        .load(imageProfile)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_profile)
                        .into(circleImageViewProfile, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(circleImageViewProfile.getContext())
                                        .load(imageProfile)
                                        .placeholder(R.drawable.ic_profile)
                                        .into(circleImageViewProfile);
                            }
                        });

                mDataRefFriend.child(mSenderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(mReceiderUserId)) {
                            String req_type = dataSnapshot.child(mReceiderUserId).child("request_type").toString();
                            if (req_type.contains("sent")) {
                                btnSendRequest.setText("Cancel Friend Request");
                                CURRENT_STATE = "request_sent";
                                btnDeclineRequest.setVisibility(View.GONE);
                            }
                            if (req_type.contains("receiver")) {
                                btnSendRequest.setText("Accept Friend Request");
                                CURRENT_STATE = "request_received";
                                btnDeclineRequest.setVisibility(View.VISIBLE);
                            }
                        }


                        mDataRefFriended.child(mSenderUserID)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(mReceiderUserId)) {
                                            btnSendRequest.setEnabled(true);
                                            btnSendRequest.setText("Unfriend this person");
                                            CURRENT_STATE = "friends";
                                            btnDeclineRequest.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void declineFriendRequest() {
        mDataRefFriend.child(mSenderUserID).child(mReceiderUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mDataRefFriend.child(mReceiderUserId).child(mSenderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btnSendRequest.setEnabled(true);
                                                btnSendRequest.setText("Send Friend Request");
                                                CURRENT_STATE = "not_friends";
                                                btnDeclineRequest.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void unFriendPerson() {
        mDataRefFriended.child(mSenderUserID).child(mReceiderUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mDataRefFriended.child(mReceiderUserId).child(mSenderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btnSendRequest.setEnabled(true);
                                                btnSendRequest.setText("Send Friend Request");
                                                CURRENT_STATE = "not_friends";
                                                btnDeclineRequest.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendReuest() {
        Calendar mCalenar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        final String currentDate = dateFormat.format(mCalenar.getTime());

        mDataRefFriended.child(mSenderUserID).child(mReceiderUserId).child("date").setValue(currentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDataRefFriended.child(mReceiderUserId).child(mSenderUserID).child("date").setValue(currentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mDataRefFriend.child(mSenderUserID).child(mReceiderUserId).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            mDataRefFriend.child(mReceiderUserId).child(mSenderUserID).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                btnSendRequest.setEnabled(true);
                                                                                btnSendRequest.setText("Unfriend this person");
                                                                                CURRENT_STATE = "friends";
                                                                                btnDeclineRequest.setVisibility(View.GONE);
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                });

    }

    private void cancelFriendReuest() {
        mDataRefFriend.child(mSenderUserID).child(mReceiderUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mDataRefFriend.child(mReceiderUserId).child(mSenderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btnSendRequest.setEnabled(true);
                                                btnSendRequest.setText("Send Friend Request");
                                                CURRENT_STATE = "not_friends";
                                                btnDeclineRequest.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendFriendRequestToAsFriend() {
        mDataRefFriend.child(mSenderUserID).child(mReceiderUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mDataRefFriend.child(mReceiderUserId).child(mSenderUserID).child("request_type").setValue("receiver")
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        HashMap<String,String> mNotify = new HashMap();
                                                        mNotify.put("from",mSenderUserID);
                                                        mNotify.put("type","request");
                                                        mDataRefNotify.child(mReceiderUserId).push().setValue(mNotify)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            btnSendRequest.setEnabled(true);
                                                                            btnSendRequest.setText("Cancel Friend Request");
                                                                            CURRENT_STATE = "request_sent";
                                                                            btnDeclineRequest.setVisibility(View.GONE);
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                );
    }

}
