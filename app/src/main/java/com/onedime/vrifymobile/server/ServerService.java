package com.onedime.vrifymobile.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.onedime.vrify.Constants;
import com.onedime.vrify.server.Server;
import com.onedime.vrifymobile.R;

public class ServerService extends Service
{
    private String NOTIFICATION_CHANNEL = "channel_server";
    private static final int NOTIFY_ID=9916;
    public static final String EXTRA_IMAGE = "image";
    private ServerTask serverTask = null;
    public static String ACTION_RECEIVE_IMAGE = "com.onedime.vrifymobile.RECEIVE_IMAGE";
    public ServerService()
    {
    }

    public static Object isServer(Object ...parameters)
    {
        return Constants.TRUE;
    }
    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //Start the server
        serverTask = new ServerTask(this);
        serverTask.execute();
        makeForeground();
        return START_STICKY;
    }

    public void makeForeground()
    {
        //Create a persistent notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL) == null)
        {
            notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL,
                    "Ignorable", NotificationManager.IMPORTANCE_DEFAULT));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL);
        builder.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL);

        builder.setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(getString(R.string.app_name));

        startForeground(NOTIFY_ID, builder.build());
    }
}