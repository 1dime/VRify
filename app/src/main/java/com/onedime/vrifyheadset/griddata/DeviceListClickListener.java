package com.onedime.vrifyheadset.griddata;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.onedime.vrifyheadset.griddata.DeviceData;

import java.util.List;

public class DeviceListClickListener implements AdapterView.OnItemClickListener
{
    private Context context;
    private List<DeviceData> devices;
    public DeviceListClickListener(Context context, List<DeviceData> devices)
    {
        this.context = context;
        this.devices = devices;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        //Get current device
        DeviceData device = this.devices.get(i);
        //And show the device address
        Toast.makeText(context, "Device: " + device.ipAddress, Toast.LENGTH_SHORT).show();
    }
}
