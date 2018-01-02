package com.android.chatfirebase;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class AllUsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView recyclerViewAllUser;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);


        mToolbar = findViewById(R.id.all_user_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerViewAllUser = findViewById(R.id.recycle_all_user);
        recyclerViewAllUser.setHasFixedSize(true);
        recyclerViewAllUser.setLayoutManager(new LinearLayoutManager(this));


        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseReference.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();



        FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<AllUsers, AllUsersViewHolder>(
                        AllUsers.class,
                        R.layout.item_all_users,
                        AllUsersViewHolder.class,
                        mDatabaseReference
                ) {
                    @Override
                    protected void populateViewHolder(AllUsersViewHolder viewHolder, AllUsers model, final int position) {



                            viewHolder.setUserName(model.getUser_name());
                            viewHolder.setUserStatus(model.getUser_status());
                            viewHolder.setThumbImage(model.getUser_thumb_image());

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String mUserID = getRef(position).getKey();
                                    Intent mProfileIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                                    mProfileIntent.putExtra("VISIT_USER_ID", mUserID);
                                    startActivity(mProfileIntent);
                                }
                            });
                        }

                };


        recyclerViewAllUser.setAdapter(firebaseRecyclerAdapter);
    }

    public static class AllUsersViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public AllUsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
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
    }
}
