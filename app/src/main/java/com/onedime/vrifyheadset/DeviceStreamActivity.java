package com.onedime.vrifyheadset;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.onedime.vrify.Constants;
import com.onedime.vrify.FunctionData;
import com.onedime.vrify.client.ClientData;
import com.onedime.vrify.client.ClientListener;
import com.onedime.vrifyheadset.threads.ClientThread;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DeviceStreamActivity extends AppCompatActivity
{
    private StreamReceiver streamReceiver;
    private IntentFilter viewImageFilter;
    private LocalBroadcastManager broadcastManager;
    public static String EXTRA_IP_ADDRESS = "IpAddress";
    public static String EXTRA_PORT = "Port";
    private ImageView streamView;
    private String address;
    private int port;
    private ClientListener listener = new ClientListener()
    {
        @Override
        public void onServerResponseReceived(FunctionData functionData)
        {
            Object results = functionData.getResults();
            if(results instanceof byte[])
            {
                //Send the image data to a receiver so it can be viewed
                byte[] imageData = (byte[]) results;
                Intent viewImage = new Intent(StreamReceiver.ACTION_VIEW_IMAGE);
                viewImage.putExtra(StreamReceiver.EXTRA_IMAGE, imageData);
                DeviceStreamActivity.this.broadcastManager.sendBroadcast(viewImage);
            }
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_stream);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Set the local broadcast manager
        this.broadcastManager = LocalBroadcastManager.getInstance(this);
        //Get the IP address and port
        Bundle passedData = getIntent().getExtras();
        this.address = passedData.getString(EXTRA_IP_ADDRESS);
        this.port = passedData.getInt(EXTRA_PORT);

        //Get the stream view
        streamView = (ImageView) findViewById(R.id.stream_view);

        try
        {
            //Create a client thread and start it
            ClientThread thread = new ClientThread("HunnyBuns", InetAddress.getByName(this.address), this.port, this.listener);
            thread.start();
        } catch (UnknownHostException e)
        {
            this.listener.onErrorEncountered(e);
        }

        //Handle viewing images here
        viewImageFilter = new IntentFilter();
        viewImageFilter.addAction(StreamReceiver.ACTION_VIEW_IMAGE);

        this.streamReceiver = new StreamReceiver()
        {
            @Override
            public void handleImage(byte[] image)
            {
                ImageView imageStreamView = DeviceStreamActivity.this.streamView;
                if(imageStreamView != null)
                {
                    Bitmap streamImage = BitmapFactory.decodeByteArray(image, 0, image.length);
                    imageStreamView.setImageBitmap(streamImage);
                }
            }
        };

        this.broadcastManager.registerReceiver(streamReceiver, viewImageFilter);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(streamReceiver != null)
        {
            this.broadcastManager.unregisterReceiver(streamReceiver);
        }
    }
}