package com.onedime.vrifyheadset.threads;

import com.onedime.vrify.FunctionData;
import com.onedime.vrify.client.Client;
import com.onedime.vrify.client.ClientData;
import com.onedime.vrify.client.ClientListener;
import com.onedime.vrify.Constants;

import java.net.InetAddress;

public class ClientThread extends Thread
{
    private String deviceName;
    private InetAddress address;
    private int port;
    private ClientListener clientListener = new ClientListener()
    {
        @Override
        public void onServerResponseReceived(FunctionData functionData)
        {

        }

        @Override
        public void onDataSentToServer(ClientData clientData, InetAddress inetAddress, int i)
        {

        }

        @Override
        public void onClientConnectionShutdown(InetAddress inetAddress, int i)
        {
        }

        @Override
        public ClientData getFunctionToRun()
        {
            ClientData data = new ClientData("HunnyBuns", Constants.IS_SERVER, null);
            return data;
        }

        @Override
        public void onErrorEncountered(Exception e)
        {

        }
    };

    public ClientThread(String deviceName, InetAddress address, int port, ClientListener listener)
    {
        this.deviceName = deviceName;
        this.address = address;
        this.port = port;
        this.clientListener = listener;
    }

    public ClientThread(String deviceName, InetAddress address, int port)
    {
        this.deviceName = deviceName;
        this.address = address;
        this.port = port;
    }

    @Override
    public void run()
    {
        //Start the client
        Client client = new Client(this.deviceName, this.address, this.port, true);
        client.setListener(this.clientListener);
        client.start();
    }
}
