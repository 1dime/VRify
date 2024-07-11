package com.onedime.vrifyheadset;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StreamReceiver extends BroadcastReceiver
{
    public static String ACTION_VIEW_IMAGE = "com.onedime.vrifyheadset.VIEW_IMAGE";
    public static String EXTRA_IMAGE = "Image";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction().equals(ACTION_VIEW_IMAGE))
        {
            //Get the image and display it
            byte[] image = intent.getByteArrayExtra(StreamReceiver.EXTRA_IMAGE);
            handleImage(image);
        }
    }

    public void handleImage(byte[] image)
    {

    }
}
