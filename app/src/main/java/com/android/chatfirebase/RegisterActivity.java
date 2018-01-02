package com.android.chatfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText edtRegisterUserName, edtRegisterEmail, edtRegisterPass;
    private Button btnRegisterAccount;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference storeUserDefaultDataReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sing Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtRegisterUserName = findViewById(R.id.edt_name_register);
        edtRegisterEmail = findViewById(R.id.edt_email_register);
        edtRegisterPass = findViewById(R.id.edt_pass_register);
        btnRegisterAccount = findViewById(R.id.btn_register_account);
        progressDialog = new ProgressDialog(this);

        btnRegisterAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtRegisterUserName.getText().toString().trim();
                String email = edtRegisterEmail.getText().toString().trim();
                String password = edtRegisterPass.getText().toString();

                RegisterAccount(name, email, password);
            }
        });
    }

    private void RegisterAccount(final String name, String email, String password) {

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(RegisterActivity.this, "Please! write your name", Toast.LENGTH_LONG).show();
        }
        else
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(RegisterActivity.this, "Please! write your email", Toast.LENGTH_LONG).show();
        }
        else
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "Please! write your password", Toast.LENGTH_LONG).show();
        }
        else {
            progressDialog.setTitle("Creating new Account");
            progressDialog.setMessage("please wait, while we are creating account for you");
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String mTokenUser = FirebaseInstanceId.getInstance().getToken();
                                String firebaseUserId = mAuth.getCurrentUser().getUid();
                                storeUserDefaultDataReference = FirebaseDatabase.getInstance()
                                                                                .getReference()
                                                                                .child("Users")
                                        .child(firebaseUserId);
                                storeUserDefaultDataReference.child("user_name").setValue(name);
                                storeUserDefaultDataReference.child("device_token").setValue(mTokenUser);
                                storeUserDefaultDataReference.child("user_status").setValue("Hi! I'm using Chat");
                                storeUserDefaultDataReference.child("user_image").setValue("ic_profile");
                                storeUserDefaultDataReference.child("user_thumb_image").setValue("default_iamge");
                                storeUserDefaultDataReference.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
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
                                        Toast.makeText(RegisterActivity.this, "data add cancelled.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }

                            progressDialog.dismiss();

                        }
                    });
        }
    }
}
