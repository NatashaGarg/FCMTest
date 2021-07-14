package com.wedevol.fcmtest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Firebase Messaging Service to handle push notifications
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService implements IRequestListener {

    private static final String TAG = "FCMMessagingService";
    private TokenService tokenService;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            // In this case the XMPP Server sends a payload data
            String message = remoteMessage.getData().get("message");
            Log.d(TAG, "Message received: " + message);

            showBasicNotification(message);
            //showInboxStyleNotification(message);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }
    /**
     * When newtoken is generated. Save and register FCM with this    token
     */
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        //Call the token service to save the token in the database
        tokenService = new TokenService(this, this);
        tokenService.registerTokenInDB(s);
    }

    private void showBasicNotification(String message) {
        String channelId = "MY_CHANNEL_ID";
        String channelName = "MY_CHANNEL_NAME"; // The user-visible name of the channel
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        Log.d(TAG, "Channel created: " + channelName);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel.getId())
                .setAutoCancel(true)
                .setContentTitle("Basic Notification")
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(0, builder.build());
        Log.d(TAG, "Notification showed!");
    }

    public void showInboxStyleNotification(String message) {
        String channelId = "MY_CHANNEL_ID";
        String channelName = "MY_CHANNEL_NAME"; // The user-visible name of the channel
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        Log.d(TAG, "Channel created: " + channelName);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, channel.getId())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Inbox Style Notification ")
                .setContentText(message)
                //.setLargeIcon(aBitmap)
                .setStyle(new NotificationCompat.InboxStyle()
                        .addLine("Line 2")
                        .addLine("Line 3"))
                .build();
        // Put the auto cancel notification flag
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        notificationManager.notify(0, notification);
        Log.d(TAG, "Notification showed!");
    }


    @Override
    public void onComplete() {
        Log.d(TAG, "Token registered successfully in the DB");

    }

    @Override
    public void onError(String message) {
        Log.d(TAG, "Error trying to register the token in the DB: " + message);
    }
}
