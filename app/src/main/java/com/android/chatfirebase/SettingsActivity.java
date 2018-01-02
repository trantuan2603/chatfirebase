package com.android.chatfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_SETTING = 1987;
    private CircleImageView cimvProfileSetting;
    private TextView tvUserNameSetting, tvStatusSetting;
    private Button btnChangeStatusSetting, btnChangePictureSetting;

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    //luu tren thiet bi
    private StorageReference mStorageRefImage, mStorageRefThumb;

    private Bitmap thumbBitmap = null;

    private Toolbar mToolbar;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mToolbar = findViewById(R.id.Setting_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        cimvProfileSetting = findViewById(R.id.profile_setting);
        tvStatusSetting = findViewById(R.id.tv_status_setting);
        tvUserNameSetting = findViewById(R.id.tv_user_name_setting);

        btnChangePictureSetting = findViewById(R.id.btn_change_picture_setting);
        btnChangeStatusSetting = findViewById(R.id.btn_change_status_setting);


        String firebaseUserId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUserId);
        databaseReference.keepSynced(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                final String imageProfile = dataSnapshot.child("user_image").getValue().toString();
                String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                tvUserNameSetting.setText(name);
                tvStatusSetting.setText(status);
                Picasso.with(cimvProfileSetting.getContext())
                        .load(imageProfile)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_profile)
                        .into(cimvProfileSetting, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(cimvProfileSetting.getContext())
                                        .load(imageProfile)
                                        .placeholder(R.drawable.ic_profile)
                                        .error(R.drawable.ic_profile)
                                        .into(cimvProfileSetting);
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SettingsActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        btnChangePictureSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, REQUEST_SETTING);
            }
        });

        btnChangeStatusSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("OLD_STATUS", tvStatusSetting.getText());
                startActivity(statusIntent);
            }
        });

        mProgressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SETTING && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgressDialog.setTitle("Update Image");
                mProgressDialog.setMessage("Image is processing, please wait a moment");
                mProgressDialog.show();

                Uri resultUri = result.getUri();
                //dung thu vien compressor de nen anh nho lai
                File thumb_fileUri = new File(resultUri.getPath());

                try {
                    thumbBitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_fileUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                final byte[] thumb_byte = byteArrayOutputStream.toByteArray();

                String firebaseUserId = mAuth.getCurrentUser().getUid();
                //cap nhat anh vao store dung compressor
                mStorageRefThumb = FirebaseStorage.getInstance().getReference().child("thumb_image").child(firebaseUserId + ".jpg");

                //cap nhat anh vao store
                mStorageRefImage = FirebaseStorage.getInstance().getReference().child("profile_image").child(firebaseUserId + ".jpg");



                mStorageRefImage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                          UploadTask uploadTask = mStorageRefThumb.putBytes(thumb_byte);
                          uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                              @Override
                              public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> taskThumb) {
                                  if (taskThumb.isSuccessful()) {
                                      //cap nhat ten file anh vao database
                                      String downloadUrl = task.getResult().getDownloadUrl().toString();
                                      String thumbUrl = taskThumb.getResult().getDownloadUrl().toString();

                                      Map update_user_data = new HashMap();
                                      update_user_data.put("user_image",downloadUrl);
                                      update_user_data.put("user_thumb_image",thumbUrl);

                                      databaseReference.updateChildren(update_user_data).addOnCompleteListener(
                                              new OnCompleteListener() {
                                                  @Override
                                                  public void onComplete(@NonNull Task task) {
                                                      if (task.isSuccessful()){
                                                          Toast.makeText(SettingsActivity.this, "updated picture successfully", Toast.LENGTH_LONG).show();
                                                      }
                                                      else
                                                      {
                                                          Toast.makeText(SettingsActivity.this, "updated picture faile", Toast.LENGTH_LONG).show();

                                                      }

                                                  }
                                              }
                                      );

                                  }
                              }
                          });

                        } else {
                            Toast.makeText(SettingsActivity.this, "update picture faile", Toast.LENGTH_LONG).show();
                        }
                        mProgressDialog.dismiss();
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
