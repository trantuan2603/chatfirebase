package com.android.chatfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText edtUpdateStatus;
    private Button btnUpdateStatus;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = findViewById(R.id.status_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Update Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtUpdateStatus = findViewById(R.id.edt_update_status);
        btnUpdateStatus = findViewById(R.id.btn_update_status);

        if (getIntent() != null){
            String status_old = getIntent().getStringExtra("OLD_STATUS");
            edtUpdateStatus.setHint(status_old);
        }

        mAuth = FirebaseAuth.getInstance();
        String userID= mAuth.getCurrentUser().getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        mDatabaseRef.keepSynced(true);
        btnUpdateStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStatus(edtUpdateStatus.getText().toString());
            }
        });
    }

    private void updateStatus(String mStatus) {
        if (TextUtils.isEmpty(mStatus)){
            Toast.makeText(StatusActivity.this, "Please write your status",Toast.LENGTH_LONG).show();
        }
        else
        {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Update status");
            progressDialog.setMessage("The process is in progress, please wait");
            progressDialog.show();
            mDatabaseRef.child("user_status").setValue(mStatus)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(StatusActivity.this, "your status updated successfully",Toast.LENGTH_LONG).show();
                                Intent settingIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                                startActivity(settingIntent);
                            }
                            else
                            {
                                Toast.makeText(StatusActivity.this, "your status updated faile",Toast.LENGTH_LONG).show();

                            }
                            progressDialog.dismiss();
                        }
                    });
        }
    }
}
