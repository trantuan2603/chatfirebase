package com.android.chatfirebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by TRANTUAN on 26-Dec-17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private FirebaseAuth mAuth;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String mTitle = remoteMessage.getNotification().getTitle();
        String mText = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();

        mAuth = FirebaseAuth.getInstance();
        String mUserID = mAuth.getCurrentUser().getUid();
        String mUserSend = remoteMessage.getData().get("from_sender_id").toString();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_profile)
                .setContentTitle(mTitle)
                .setContentText(mText);

        if (!mUserID.equals(mUserSend)){


            Intent resultIntent = new Intent(click_action);
            resultIntent.putExtra("VISIT_USER_ID",mUserSend);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT

            );

            builder.setContentIntent(resultPendingIntent);
        }



        int mNotificationId= (int) System.currentTimeMillis();
        NotificationManager managerCompat= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        managerCompat.notify(mNotificationId,builder.build());
    }
}
