package com.onedime.vrifymobile.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.os.BuildCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.onedime.vrify.Constants;
import com.onedime.vrify.FunctionData;
import com.onedime.vrify.FunctionWrapper;
import com.onedime.vrify.client.ClientData;
import com.onedime.vrify.server.Server;
import com.onedime.vrify.server.ServerListener;
import com.onedime.vrifymobile.ImageReceiver;
import com.onedime.vrifymobile.MainActivity;
import com.onedime.vrifymobile.screenshots.ScreenshotService;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class ServerTask extends AsyncTask<String, Void, Void>
{
    private ServerService serverService;
    private LocalBroadcastManager broadcastManager;
    private ServerListener listener = new ServerListener()
    {
        @Override
        public void onServerStarted(int i)
        {
            Looper.prepare();
            try
            {
                String address = getIPAddress() + ":" + i;
                Intent addressIntent = new Intent(MainActivity.ACTION_UPDATE_UI);
                addressIntent.putExtra(MainActivity.EXTRA_UPDATE_IP_ADDRESS, address);
                ServerTask.this.broadcastManager.sendBroadcast(addressIntent);

            } catch (SocketException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public FunctionData onClientDataReceived(Socket socket, ClientData clientData)
        {
            /*
            Handle operations here
             */

            //Get a screenshot and wait for a response
            Intent screenshotIntent = new Intent(ServerTask.this.serverService, ScreenshotService.class);
            screenshotIntent.setAction(ScreenshotService.ACTION_RECORD);
            ServerTask.this.serverService.startService(screenshotIntent);

            //Get response from screenshot service
            IntentFilter imageReceiverFilter = new IntentFilter();
            imageReceiverFilter.addAction(ServerService.ACTION_RECEIVE_IMAGE);
            final FunctionData[] functionData = {new FunctionData(null, null)};
            ImageReceiver imageReceiver = new ImageReceiver()
            {
                @Override
                public void onImageReceived(FunctionData imageData)
                {
                    functionData[0] = imageData;
                }
            };

            ServerTask.this.broadcastManager.registerReceiver(imageReceiver, imageReceiverFilter);
            while(functionData[0].getResults() == null)
            {
                //Do nothing, just wait to send back
            }
            return functionData[0];
        }

        @Override
        public void onErrorEncountered(Exception e)
        {
            e.printStackTrace();
            Toast.makeText(serverService, "Encountered an error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServerClosed(int i)
        {
            Toast.makeText(serverService, "Server on port " + i + " closed successfully.", Toast.LENGTH_SHORT).show();
        }
    };

    private Server server = null;
    public static Object isServer(Object ...parameters)
    {
        return Constants.IS_SERVER;
    }
    public ServerTask(ServerService service)
    {
        this.serverService = service;
        this.broadcastManager = LocalBroadcastManager.getInstance(this.serverService);
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
    protected Void doInBackground(String... strings)
    {
        //Create our server
        FunctionWrapper wrapper = new FunctionWrapper();
        wrapper.addFunction(Constants.IS_SERVER, ServerTask::isServer);
        server = new Server(wrapper, Server.MIN_PORT, 4000, listener);
        //And start it
        server.start();
        return null;
    }

    public Server getServer()
    {
        return this.server;
    }
}
