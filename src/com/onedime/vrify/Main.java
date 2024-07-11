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
import com.onedime.vrify.server.Server;
import com.onedime.vrify.server.ServerListener;

public class Main
{
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
	
	public static Object sendToWebsite(Object ...parameters)
	{
		return 404;
	}
	
	public static void main(String[] args) throws InterruptedException
	{
		try
		{
			FunctionWrapper wrapper = new FunctionWrapper();
			wrapper.addFunction("sendToWebsite", Main::sendToWebsitecmd);
			Server server = new Server(wrapper, 443, 443, serverListener);
			server.start();
			
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
