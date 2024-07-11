package com.onedime.vrifyheadset.threads;

import android.content.Context;
import android.widget.GridView;

import com.onedime.vrify.client.Client;
import com.onedime.vrify.server.Server;
import com.onedime.vrifyheadset.griddata.DeviceData;
import com.onedime.vrifyheadset.griddata.DeviceListAdapter;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class DeviceFinderThread extends Thread
{
    private Context context;
    private GridView devicesList;

    public DeviceFinderThread(Context context, GridView devicesList)
    {
        this.context = context;
        this.devicesList = devicesList;
    }

    public List<DeviceData> getAllDevices(int minPort, int maxPort) throws InterruptedException, SocketException
    {
        //Get the subnet for the device running this app
        String address = getIPAddress();
        String[] splitAddress = address.split("\\.");
        String subnet = "";
        for(int index = 0; index < (splitAddress.length - 2); index++)
        {
            if(index < (splitAddress.length - 3))
            {
                subnet += splitAddress[index] + ".";
            }
            else
            {
                subnet += splitAddress[index];
            }
        }

        //Search for all devices on network
        List<String> allDevices = Client.findDevices(subnet, Server.MIN_PORT, Server.MAX_PORT);
        List<DeviceData> devices = new ArrayList<>();
        for(int index = 0; index < allDevices.size(); index++)
        {
            //Get device ip address and port
            String device = allDevices.get(index);
            String[] portSplitDevice = device.split(":");
            String ipAddress = portSplitDevice[0];
            int port = Integer.parseInt(portSplitDevice[1]);
            DeviceData data = new DeviceData();
            data.ipAddress = ipAddress;
            data.port = port;
            devices.add(data);
        }

        //Finally return the list of devices
        return devices;
    }

    //Returns device's IP address
    private String getIPAddress() throws SocketException
    {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while(networkInterfaces.hasMoreElements())
        {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if(networkInterface.isUp() && !networkInterface.isLoopback())
            {
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while(addresses.hasMoreElements())
                {
                    InetAddress address = addresses.nextElement();
                    if(address instanceof Inet4Address)
                    {
                        return address.getHostAddress();
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void run()
    {
        try
        {
            //Get all devices on network
            List<DeviceData> devices = this.getAllDevices(Server.MIN_PORT, Server.MAX_PORT);
            //And create a list adapter for the devices list
            DeviceListAdapter listAdapter = new DeviceListAdapter(this.context, devices);
            devicesList.setAdapter(listAdapter);
        } catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        } catch (SocketException e)
        {
            throw new RuntimeException(e);
        }
    }
}
