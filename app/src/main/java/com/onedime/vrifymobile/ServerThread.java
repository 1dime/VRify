package com.onedime.vrifymobile;

import android.content.Context;
import android.content.Intent;

import com.onedime.vrifymobile.screenshots.ScreenshotService;
import com.onedime.vrifymobile.server.ServerService;

public class ServerThread extends Thread
{
    private Context context;
    private Intent screenshotIntent;
    private Intent serverIntent;
    public ServerThread(Context context)
    {
        this.context = context;
        this.screenshotIntent = new Intent(this.context, ScreenshotService.class);
        this.serverIntent = new Intent(this.context, ServerService.class);
    }

    @Override
    public void run()
    {
        screenshotIntent.setAction(ScreenshotService.ACTION_RECORD);
        this.context.startService(this.screenshotIntent);
        this.context.startService(this.serverIntent);
    }

    @Override
    public void interrupt()
    {
        super.interrupt();
        this.context.stopService(this.screenshotIntent);
        this.context.stopService(this.serverIntent);
    }
}
