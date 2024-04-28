package com.onedime.vrifymobile;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.onedime.vrifymobile.screenshots.ScreenshotService;
import com.onedime.vrifymobile.server.ServerService;
import com.onedime.vrifymobile.server.ServerTask;

public class MainActivity extends AppCompatActivity
{
    public static final String EXTRA_UPDATE_IP_ADDRESS = "com.onedime.vrifymobile.UpdateIpAddress";
    public static final String ACTION_UPDATE_UI = "com.onedime.vrifymobile.UpdateUI";
    //Buttons for starting and stopping the server
    private AppCompatButton startButton;
    private AppCompatButton stopButton;

    //Textview containing IP address
    public static TextView ipAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        try {
            Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", boolean.class)
                    .invoke(null, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) ->
        {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Get the start and stop buttons
        startButton = (AppCompatButton) findViewById(R.id.start_server);
        stopButton = (AppCompatButton) findViewById(R.id.stop_server);

        //And get the IP address text view
        ipAddress = (TextView) findViewById(R.id.ip_address);
        //Now make the intent for our server service
        Intent serverIntent = new Intent(this, ServerService.class);
        Intent screenshotIntent = new Intent(MainActivity.this, ScreenshotService.class);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        //And assign click actions to both
        startButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                screenshotIntent.setAction(ScreenshotService.ACTION_RECORD);
                startService(screenshotIntent);
                //Start the server service
                startService(serverIntent);

                IntentFilter addressFilter = new IntentFilter();
                addressFilter.addAction(MainActivity.ACTION_UPDATE_UI);
                AddressReceiver addressReceiver = new AddressReceiver()
                {
                    @Override
                    public void onAddressReceived(String fullAddress)
                    {
                        ipAddress.setText("Running on " + fullAddress);
                    }
                };

                //Register the address receiver
                broadcastManager.registerReceiver(addressReceiver, addressFilter);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Stop the server service
                stopService(screenshotIntent);
                stopService(serverIntent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
    }
}