package com.android.chatfirebase;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TabLayout mainTabs;
    private ViewPager mainViewPager;
    private TabsPagerAdapter mainTabsPagerAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatapref;
    private String mUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndRequestPermissions();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            mUserID = mAuth.getCurrentUser().getUid();
            mDatapref = FirebaseDatabase.getInstance().getReference().child("Users").child(mUserID);
        }



        mToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("MyChat");


        mainTabs = findViewById(R.id.main_tabs);
        mainViewPager = findViewById(R.id.main_tab_page);

        mainTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mainViewPager.setAdapter(mainTabsPagerAdapter);
        mainTabs.setupWithViewPager(mainViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            mainLogout();
        }else if (currentUser != null){
            mDatapref.child("online").setValue("true");
        }

    }

    private void mainLogout() {
        Intent startPageIntent = new Intent(MainActivity.this, StartPageActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.menu_main_logout){
            if (currentUser != null){
                mDatapref.child("online").setValue(ServerValue.TIMESTAMP);
            }
            mAuth.signOut();
            mainLogout();
        }

        if (item.getItemId() == R.id.menu_main_account_setting){
           Intent settingAccountIntent = new Intent(MainActivity.this, SettingsActivity.class);
           startActivity(settingAccountIntent);
        }

        if (item.getItemId() == R.id.menu_main_all_users){
            Intent allUserAccountIntent = new Intent(MainActivity.this, AllUsersActivity.class);
            startActivity(allUserAccountIntent);
        }

        return true;
    }

    // xin quyen
    private void checkAndRequestPermissions() {
        String[] permissions = new String[]{
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,

        };

        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentUser != null){
            mDatapref.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
