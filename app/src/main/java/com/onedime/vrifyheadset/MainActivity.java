package com.onedime.vrifyheadset;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.onedime.vrifyheadset.threads.ClientThread;
import com.onedime.vrifyheadset.threads.DeviceFinderThread;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity
{
    private GridView devicesList;
    private EditText deviceAddressPort;
    private AppCompatButton connectButton;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Get all views from main layout
        devicesList = (GridView) findViewById(R.id.devices_list);
        deviceAddressPort = (EditText) findViewById(R.id.device_ip_port);
        connectButton = (AppCompatButton) findViewById(R.id.connect);

        //Get all devices by searching for them (put this in a thread in the future)
        DeviceFinderThread deviceFinder = new DeviceFinderThread(this, devicesList);
        deviceFinder.start();

        //Set the listener for clicking connect button
        connectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Show a message when device address port is empty
                String addressPort = deviceAddressPort.getText().toString();
                if(addressPort.matches(""))
                {
                    //IP address and port must be set for manual connection
                    Toast.makeText(MainActivity.this, "IP address and port must be set", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //Get ip address and port
                    String[] splitAddressPort = addressPort.split(":");
                    String address = splitAddressPort[0];
                    int port = Integer.parseInt(splitAddressPort[1]);
                    //And create an intent that holds address and port
                    Intent deviceStream = new Intent(MainActivity.this, DeviceStreamActivity.class);
                    deviceStream.putExtra(DeviceStreamActivity.EXTRA_IP_ADDRESS, address);
                    deviceStream.putExtra(DeviceStreamActivity.EXTRA_PORT, port);
                    //Now launch the activity
                    startActivity(deviceStream);
                }
            }
        });
    }

}