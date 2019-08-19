package com.example.youngran.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.youngran.R;
import com.example.youngran.main.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;

public class FCMListenerService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FCM";

    int mLastId = 0;
    ArrayList<Integer> mActiveIdList = new ArrayList<Integer>();
    NotificationManager nm;

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // 메시지를 받았을 때 동작하는 메소드
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Message: " + message);

        sendPushNotification(title,message);
    }

    private void createNotificationId() {
        int id = ++mLastId;
        mActiveIdList.add(id);
    }

    public void sendPushNotification(String title, String message) {
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(mLastId);
        createNotificationId();

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // 하위 호환성을 위해 NotificationCompat.Builder 사용

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setSound(defaultSoundUri)
                .setLights(000000255,500,2000)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentText(message);

        Intent popupIntent = new Intent(getApplicationContext(), MainActivity.class);
        popupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        popupIntent.putExtra("msg", title);
        popupIntent.putExtra("LastId", mLastId);
        startActivity(popupIntent); // 메시지 팝업창을 바로 띄운다.

        PendingIntent resultPendingIntent =PendingIntent.getActivity(this, 0, popupIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        nm.notify(mLastId, mBuilder.build());
    }
}


