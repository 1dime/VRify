package com.onedime.vrifyheadset.griddata;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.onedime.vrifyheadset.R;

import java.util.List;

public class DeviceListAdapter extends BaseAdapter
{
    private Context context;
    private List<DeviceData> devices;
    public DeviceListAdapter(Context context, List<DeviceData> devices)
    {
        this.context = context;
        this.devices = devices;
    }

    @Override
    public int getCount()
    {
        return this.devices.size();
    }

    @Override
    public Object getItem(int i)
    {
        return this.devices.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    private class ViewHolder
    {
        private TextView deviceIPPort;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        //Create a layout inflater and a view holder
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder = null;
        //And define the view holder if the view is not set
        if(view == null)
        {
            view = inflater.inflate(R.layout.device_list_item, null);
            holder = new ViewHolder();
            holder.deviceIPPort = view.findViewById(R.id.list_item_ip_port);
            //Make the view's tag the holder
            view.setTag(holder);
        }
        else
            //Get the view holder, stored as tag in view
            holder = (ViewHolder) view.getTag();
        //Set the text for the ip+port in holder
        DeviceData deviceData = DeviceListAdapter.this.devices.get(i);
        String address = deviceData.ipAddress;
        int port = deviceData.port;
        holder.deviceIPPort.setText(address + ":" + port);

        //And return our view
        return view;
    }
}
