package com.android.chatfirebase;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private View viewFriends;
    private RecyclerView recyclerViewFriend;
    private String mUserID;

    private DatabaseReference mDataRefFirend,mDataRefUser;
    private FirebaseAuth mAuth;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewFriends = inflater.inflate(R.layout.fragment_friends, container, false);

        recyclerViewFriend = viewFriends.findViewById(R.id.recycle_friends);
        recyclerViewFriend.setHasFixedSize(true);
        recyclerViewFriend.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        mUserID = mAuth.getCurrentUser().getUid();

        mDataRefFirend = FirebaseDatabase.getInstance().getReference().child("Friends").child(mUserID);
        mDataRefFirend.keepSynced(true);
        mDataRefUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDataRefUser.keepSynced(true);



        return viewFriends;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<AllFirends,AllFriendViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<AllFirends, AllFriendViewHolder>(
               AllFirends.class,
                R.layout.item_all_users,
                AllFriendViewHolder.class,
                mDataRefFirend
        ) {
            @Override
            protected void populateViewHolder(final AllFriendViewHolder viewHolder, final AllFirends model, int position) {
                final String mUserIdFriend = getRef(position).getKey();
                if(!TextUtils.isEmpty(mUserIdFriend)){
                    mDataRefUser.child(mUserIdFriend).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){

                                final String name = dataSnapshot.child("user_name").getValue().toString();
                                String status = dataSnapshot.child("user_status").getValue().toString();
                                String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                                if (dataSnapshot.hasChild("online")){
                                    String status_online = (String) dataSnapshot.child("online").getValue().toString();
                                    viewHolder.setImvStatus(status_online);
                                }


                                viewHolder.setUserName(name);
                                viewHolder.setUserStatus(status);
                                viewHolder.setThumbImage(thumb_image);
                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CharSequence[] options = new CharSequence[]{
                                            name + "'s profile",
                                                "Send Message"
                                        };

                                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
                                        mBuilder.setTitle("Select options");

                                        mBuilder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int position) {
                                                if (position == 0){
                                                    Intent profileIntent = new Intent(getActivity(),ProfileActivity.class);
                                                    profileIntent.putExtra("VISIT_USER_ID",mUserIdFriend);
                                                    startActivity(profileIntent);
                                                }

                                                if (position == 1){
                                                    if (dataSnapshot.child("online").exists()){
                                                        Intent profileIntent = new Intent(getActivity(),ChatActivity.class);
                                                        profileIntent.putExtra("VISIT_USER_ID",mUserIdFriend);
                                                        profileIntent.putExtra("NAME_FRIEND",name);
                                                        startActivity(profileIntent);
                                                    }else{
                                                        mDataRefUser.child(mUserIdFriend).child("online")
                                                                .setValue(ServerValue.TIMESTAMP)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Intent profileIntent = new Intent(getActivity(),ChatActivity.class);
                                                                        profileIntent.putExtra("VISIT_USER_ID",mUserIdFriend);
                                                                        profileIntent.putExtra("NAME_FRIEND",name);
                                                                        startActivity(profileIntent);
                                                                    }
                                                                });
                                                    }

                                                }
                                            }
                                        });

                                        mBuilder.show();
                                    }
                                });

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        };

        recyclerViewFriend.setAdapter(firebaseRecyclerAdapter);
    }

    public static class AllFriendViewHolder extends RecyclerView.ViewHolder{
        private View mView;

        public AllFriendViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void setUserName(String name) {
            TextView userName = mView.findViewById(R.id.tv_all_user_name);
            userName.setText(name);
        }

        public void setUserStatus(String status) {
            TextView userStatus = mView.findViewById(R.id.tv_all_user_status);
            userStatus.setText(status);
        }

        public void setThumbImage(final String urlImage) {
            final CircleImageView mCircleImageView = mView.findViewById(R.id.profile_all_user);

            Picasso.with(mCircleImageView.getContext()).load(urlImage)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.ic_profile)
                    .into(mCircleImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(mCircleImageView.getContext())
                                    .load(urlImage)
                                    .placeholder(R.drawable.ic_profile)
                                    .into(mCircleImageView);
                        }
                    });
        }

        public void setImvStatus(String status_online) {
            ImageView imageView = mView.findViewById(R.id.imgvi_profile_status);
            if (status_online.equals("true")){
                imageView.setVisibility(View.VISIBLE);
            }else
                imageView.setVisibility(View.INVISIBLE);
        }
    }
}
