package com.onedime.vrifymobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.onedime.vrify.Main;

public class AddressReceiver extends BroadcastReceiver
{
    private String address = "";
    private int port = 0;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction().equals(MainActivity.ACTION_UPDATE_UI))
        {
            String fullAddress = intent.getStringExtra(MainActivity.EXTRA_UPDATE_IP_ADDRESS);
            this.onAddressReceived(fullAddress);
        }
    }

    public void onAddressReceived(String fullAddress)
    {

    }
}