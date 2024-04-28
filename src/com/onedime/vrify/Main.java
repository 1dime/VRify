package com.onedime.vrify;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.onedime.vrify.client.Client;
import com.onedime.vrify.client.ClientData;
import com.onedime.vrify.client.ClientListener;
import com.onedime.vrify.server.ServerListener;

public class Main
{
	private static int i = 0;
	private static ClientListener listener = new ClientListener() {

		@Override
		public void onServerResponseReceived(FunctionData serverResponse)
		{
			//Received a response from the server
			System.out.println("Received response from server!");
			File file = new File("C:\\Users\\bremo\\screenshots\\screenshot_" + i + ".png");
			Object results = serverResponse.getResults();
			if(results instanceof byte[])
			{
				byte[] data = (byte[]) results;
				try
				{
					BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
					outputStream.write(data);
					outputStream.flush();
					outputStream.close();
					i += 1;
					Thread.sleep(1);
				} catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}

		@Override
		public void onDataSentToServer(ClientData data, InetAddress serverAddress, int serverPort)
		{
			//Sent data to server
			System.out.println("Sent data to server!");
		}

		@Override
		public void onClientConnectionShutdown(InetAddress serverAddress, int serverPort)
		{
			//Connection to server shutdown
			System.out.println("Shutdown connection to server!");
		}
		
		@Override
		public ClientData getFunctionToRun()
		{
			//This is a sample, have server run IS_SERVER
			//For future reference, this function determines what function should be ran
			//based on headset and controller sensor data, and controller input.
			ClientData data = new ClientData("HunnyBuns", Constants.IS_SERVER, null);
			return data;
		}
		
		@Override
		public void onErrorEncountered(Exception error)
		{
			//We encountered an error
			System.out.println("We encountered an error: " + error.getMessage());
		}
	};
	
	private static ServerListener serverListener = new ServerListener() 
	{

		@Override
		public void onServerStarted(int port)
		{
			// TODO Auto-generated method stub
			System.out.println("Server started on port: " + port);
		}

		@Override
		public FunctionData onClientDataReceived(Socket socket, ClientData data)
		{
			// TODO Auto-generated method stub
			System.out.println("Client data received: " + data.getFunctionName());
			FunctionData funData = new FunctionData(null, data);
			return funData;
		}

		@Override
		public void onErrorEncountered(Exception error)
		{
			// TODO Auto-generated method stub
			System.out.println("Encountered an error: " + error.getMessage());
		}

		@Override
		public void onServerClosed(int port)
		{
			// TODO Auto-generated method stub
			System.out.println("Server closed.");
		}
		
	};
	
	public static Object isServer(Object ...parameters)
	{
		return Constants.TRUE;
	}
	
	public static void main(String[] args) throws InterruptedException
	{
		try
		{
			Client client = new Client("HunnyBuns", InetAddress.getByName("10.23.0.123"), 1187 , true);
			client.setListener(listener);
			client.start();
		} catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
