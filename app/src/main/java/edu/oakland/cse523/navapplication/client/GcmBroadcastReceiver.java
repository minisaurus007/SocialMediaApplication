package edu.oakland.cse523.navapplication.client;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import edu.oakland.cse523.navapplication.ChatListActivity;
import edu.oakland.cse523.navapplication.Common;
import edu.oakland.cse523.navapplication.DataProvider;
import edu.oakland.cse523.navapplication.R;

/**
 * Created by Jofia on 11/30/15.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "GcmBroadcastReceiver";

    private Context ctx;

    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context;

        PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();

        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

            String messageType = gcm.getMessageType(intent);
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error", false);

            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server", false);

            } else {
                String msg = intent.getStringExtra(DataProvider.COL_MSG);
                String email = intent.getStringExtra(DataProvider.COL_FROM);

                ContentValues values = new ContentValues(2);
                values.put(DataProvider.COL_MSG, msg);
                values.put(DataProvider.COL_FROM, email);
                context.getContentResolver().insert(DataProvider.CONTENT_URI_MESSAGES, values);

                if (Common.isNotify()) {
                    sendNotification("New message", true);
                }
            }
            setResultCode(Activity.RESULT_OK);

        } finally {
            mWakeLock.release();
        }
    }

    private void sendNotification(String text, boolean launchApp) {
        NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder mBuilder = new Notification.Builder(ctx)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_drawer)
                .setContentTitle(ctx.getString(R.string.app_name))
                .setContentText(text);

        if (!TextUtils.isEmpty(Common.getRingtone())) {
            mBuilder.setSound(Uri.parse(Common.getRingtone()));
        }

        if (launchApp) {
            Intent intent = new Intent(ctx, ChatListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pi);
        }

        mNotificationManager.notify(1, mBuilder.getNotification());
    }
}
