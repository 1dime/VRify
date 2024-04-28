package com.onedime.vrifymobile.screenshots;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.onedime.vrifymobile.R;
import com.onedime.vrifymobile.server.ServerService;

/*
Credit for this entire screenshot package goes to commonsguy.
https://github.com/commonsguy/cw-omnibus
 */
public class ScreenshotService extends Service
{
    private String NOTIFICATION_CHANNEL = "channel_ignorable";
    private static final int NOTIFY_ID=9906;
    static final int VIRTUAL_DISPLAY_FLAGS=
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR |
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    public static String ACTION_RECORD = "com.onedime.vrifymobile.RECORD";
    public static String EXTRA_RESUT_CODE = "ResultCode";
    public static String EXTRA_RESULT_INTENT = "ResultIntent";
    private MediaProjectionManager projectionManager;
    private MediaProjection projection;
    private WindowManager windowManager;
    private HandlerThread handlerThread;
    private Handler handler;
    private int resultCode;
    private Intent resultData;
    private ImageConverter converter;
    private VirtualDisplay virtualDisplay;
    private LocalBroadcastManager broadcastManager;
    public ScreenshotService()
    {
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
        //Create the broadcast manager
        ScreenshotService.this.broadcastManager = LocalBroadcastManager.getInstance(this);
        //Create the handler thread
        handlerThread = new HandlerThread(getClass().getSimpleName(), Process.THREAD_PRIORITY_BACKGROUND);
        //Create window and projection managers
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        //Then start the handler thread
        handlerThread.start();
        //And create the handler
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(intent != null)
        {
            //Check if the action is not set
            if (intent.getAction() == null)
            {
                //Get the passed result code
                resultCode = intent.getIntExtra(EXTRA_RESUT_CODE, 1);
                //And intent
                resultData = intent.getParcelableExtra(EXTRA_RESULT_INTENT);
                makeForeground();
            } else if (ACTION_RECORD.equals(intent.getAction()))
            {
                if (resultData != null)
                {
                    //Start capturing screenshots
                    startCapturing();
                } else
                {
                    //Start the screenshot activity
                    Intent screenshotIntent = new Intent(this, ScreenshotActivity.class);
                    screenshotIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(screenshotIntent);
                }
            }
        }
        return START_NOT_STICKY;
    }

    public WindowManager getWindowManager()
    {
        return this.windowManager;
    }

    public Handler getHandler()
    {
        return this.handler;
    }

    public void handleImage(byte[] image)
    {
        //Pass the image data to the server service using broadcast receiver
        //Or save the image and have the server service read the image from a file

        //Send data to ServerService
        Intent imageIntent = new Intent(ServerService.ACTION_RECEIVE_IMAGE);
        imageIntent.putExtra(ServerService.EXTRA_IMAGE, image);
        ScreenshotService.this.broadcastManager.sendBroadcast(imageIntent);
    }

    public void startCapturing()
    {
        //Get projection from manager
        projection = projectionManager.getMediaProjection(resultCode, resultData);
        converter = new ImageConverter(this);

        MediaProjection.Callback callback = new MediaProjection.Callback()
        {
            @Override
            public void onStop()
            {
                virtualDisplay.release();
            }
        };


        virtualDisplay = projection.createVirtualDisplay("VRify",
                converter.getWidth(), converter.getHeight(),
                getResources().getDisplayMetrics().densityDpi,
                VIRTUAL_DISPLAY_FLAGS, converter.getSurface(), null,
                handler);

        projection.registerCallback(callback, handler);
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