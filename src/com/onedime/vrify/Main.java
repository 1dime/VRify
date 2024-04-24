package com.onedime.vrify;

import java.net.InetAddress;
import java.util.List;

import com.onedime.vrify.client.Client;
import com.onedime.vrify.client.ClientData;
import com.onedime.vrify.server.Server;
import com.onedime.vrify.server.ServerListener;

public class Main
{
	public static Object isDeviceServer(Object... parameters)
	{
		return Constants.TRUE;
	}
	
	public static void main(String[] args) throws Exception
	{
		// TODO Auto-generated method stub
		
		FunctionWrapper wrapper = new FunctionWrapper();
		wrapper.addFunction(Constants.IS_SERVER, Main::isDeviceServer);
		
		Server server = new Server(wrapper, Server.MIN_PORT, Server.MAX_PORT, new ServerListener() {

			@Override
			public void onServerStarted(int port)
			{
				// TODO Auto-generated method stub
				System.out.println("Server started on port: " + port);
			}

			@Override
			public FunctionData onClientDataReceived(ClientData data)
			{
				// TODO Auto-generated method stub
				System.out.println("Client data received!");
				return null;
			}

			@Override
			public void onErrorEncountered(Exception error)
			{
				// TODO Auto-generated method stub
				System.out.println("We encountered an error!");
			}

			@Override
			public void onServerClosed(int port)
			{
				// TODO Auto-generated method stub
				System.out.println("Server on " + port + " closed!");
			}
			
		});
		
		server.start();
		
		Client client = new Client("Whopper", InetAddress.getByName("10.23.4.110"), server.getPort(), false);
		client.start();
		
		/*
		 * 
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress address = InetAddress.getByName("10.23.4.110");
		byte[] serializedData = new ClientData("Wheeze", "isDeviceServer", null).serialize();
		clientSocket.send(new DatagramPacket(serializedData, serializedData.length, address, server.getPort()));
		
		byte[] response = new byte[1024];
		clientSocket.receive(new DatagramPacket(response, response.length, address, server.getPort()));
		FunctionData deserializedData = FunctionData.deserialize(response);
		
		clientSocket.close();
		
		System.out.println("Response from server: " + deserializedData.getResults());
		 */
	}
	
}
