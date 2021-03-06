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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText edtLoginEmail, edtLoginPass;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference mDataRefUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDataRefUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDataRefUser.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        mToolbar = findViewById(R.id.signin_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtLoginEmail = findViewById(R.id.edt_email_login);
        edtLoginPass = findViewById(R.id.edt_pass_login);
        btnLogin = findViewById(R.id.btn_sign_in);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtLoginEmail.getText().toString().trim();
                String password = edtLoginPass.getText().toString().trim();

                loginUserAccount(email, password);
            }
        });
    }

    private void loginUserAccount(String email, String password) {

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(LoginActivity.this, "Please write your Email", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "Please write your Password", Toast.LENGTH_LONG).show();
        } else {
            progressDialog.setTitle("Login Account");
            progressDialog.setMessage("Please wait, while we are verifying your credentials..");
            progressDialog.show();
                mAuth.signInWithEmailAndPassword(email,password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    String onlineUserID = mAuth.getCurrentUser().getUid();
                                    String mTokenUser = FirebaseInstanceId.getInstance().getToken();
                                    mDataRefUser.child(onlineUserID).child("device_token").setValue(mTokenUser)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(mainIntent);
                                                    finish();
                                                }
                                            });


                                }else{
                                    Toast.makeText(LoginActivity.this, "Wrong email and password, please write email and password", Toast.LENGTH_LONG).show();

                                }
                                progressDialog.dismiss();
                            }
                        });
        }

    }
}
