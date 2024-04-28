package com.onedime.vrifymobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.onedime.vrify.FunctionData;
import com.onedime.vrifymobile.server.ServerService;

public class ImageReceiver extends BroadcastReceiver
{
    private FunctionData functionData = new FunctionData(null, null);

    @Override
    public void onReceive(Context context, Intent intent)
    {
        //Get the screenshot if the action is appropriate
        if(intent.getAction() == ServerService.ACTION_RECEIVE_IMAGE)
        {
            functionData.setResults(intent.getByteArrayExtra(ServerService.EXTRA_IMAGE));
            this.onImageReceived(functionData);
        }
    }

    public void onImageReceived(FunctionData imageData)
    {

    }
}