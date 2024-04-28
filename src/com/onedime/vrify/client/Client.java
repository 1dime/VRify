package com.onedime.vrify.client;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.onedime.vrify.Constants;
import com.onedime.vrify.FunctionData;

public class Client
{
	private boolean shutdown = false;
	private boolean runForever = true;
	private String deviceName;
	private InetAddress serverAddress;
	private int serverPort;
	private Socket clientSocket;
	private ClientListener listener = new ClientListener()
	{

		@Override
		public void onServerResponseReceived(FunctionData serverResponse)
		{
			// Received a response from the server
			System.out.println("Received response from server!");
		}

		@Override
		public void onDataSentToServer(ClientData data, InetAddress serverAddress, int serverPort)
		{
			// Sent data to server
			System.out.println("Sent data to server!");
		}

		@Override
		public void onClientConnectionShutdown(InetAddress serverAddress, int serverPort)
		{
			// Connection to server shutdown
			System.out.println("Shutdown connection to server!");
		}

		@Override
		public ClientData getFunctionToRun()
		{
			// This is a sample, have server run IS_SERVER
			// For future reference, this function determines what function should be ran
			// based on headset and controller sensor data, and controller input.
			ClientData data = new ClientData(deviceName, Constants.IS_SERVER, null);
			return data;
		}

		@Override
		public void onErrorEncountered(Exception error)
		{
			// We encountered an error
			System.out.println("We encountered an error!");
		}
	};

	public Client(String deviceName, InetAddress address, int port, boolean runForever)
	{
		this.deviceName = deviceName;
		this.serverAddress = address;
		this.serverPort = port;
		this.runForever = runForever;
	}

	public boolean isDeviceServer(String ipAddress, int port)
	{
		try
		{
			// Now convert that to an InetAddress
			InetAddress address = InetAddress.getByName(ipAddress);
			// Then make a datagram socket for checking if device is running VRify
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setReuseAddress(true);
			clientSocket.setSoTimeout(100);

			// Now, create a new instance of client data that runs Constants.IS_SERVER on
			// server for checking
			// and serialize it before sending
			byte[] serializedClientData = new ClientData(Client.this.deviceName, Constants.IS_SERVER, null).serialize();
			clientSocket.send(new DatagramPacket(serializedClientData, serializedClientData.length, address, port));

			// Now get the response from the device as an instance of FunctionData
			byte[] serializedResponse = new byte[1024];
			clientSocket.receive(new DatagramPacket(serializedResponse, serializedResponse.length));
			FunctionData data = FunctionData.deserialize(serializedResponse);

			// And check if the device is a server by checking if results in data is TRUE
			if (data.getResults().toString().equals(Constants.TRUE))
			{
				// Device is a server, return true
				clientSocket.close();
				return true;
			}
			// Finally, close the client socket
			clientSocket.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		// And return false
		return false;
	}

	public List<String> findDevices(String subnet, int minPort, int maxPort) throws InterruptedException
	{
		FindDevicesThread findThread = new FindDevicesThread(subnet, minPort, maxPort);
		findThread.start();
		findThread.join();
		return findThread.getDevices();
	}

	/*
	 * Function: setListener Sets ClientListener to another instance
	 * 
	 * @listener: new listener instance
	 */
	public void setListener(ClientListener listener)
	{
		// Set the listener
		this.listener = listener;
	}

	/*
	 * Function: getListener Returns current instance of ClientListener
	 * 
	 * @returns client listener
	 */
	public ClientListener getListener()
	{
		// Get the current client listener instance
		return this.listener;
	}

	/*
	 * Function: interactWithServer Gets function to be ran, sends it to server,
	 * gets response. Also, handles errors and server response.
	 * 
	 * @socket: TCP socket for server communication
	 */
	public void interactWithServer(Socket socket) throws IOException
	{
		// Send the function to be ran and its parameters
		ClientData functionToRun = Client.this.listener.getFunctionToRun();
		byte[] serializedFunctionToRun = functionToRun.serialize();
		Client.this.send(socket, serializedFunctionToRun);

		// And receive the server response and server notify results
		byte[] serializedServerResponse = Client.this.read(socket);
		byte[] serializedServerNotifyResults = Client.this.read(socket);
		FunctionData serverResponse = FunctionData.deserialize(serializedServerResponse);
		FunctionData serverNotifyResults = FunctionData.deserialize(serializedServerNotifyResults);

		// And handle both
		Client.this.listener.onServerResponseReceived(serverResponse);
		Client.this.listener.onServerResponseReceived(serverNotifyResults);
	}

	/*
	 * Function: read Reads data from socket
	 * 
	 * @socket: socket to send data to
	 * 
	 * @returns data from socket
	 */
	public byte[] read(Socket socket) throws IOException
	{
		// Read data using DataInputStream
		DataInputStream inputStream = new DataInputStream(socket.getInputStream());
		int length = inputStream.readInt();
		byte[] data = new byte[length];
		inputStream.readFully(data, 0, length);

		// And return it
		return data;
	}

	/*
	 * Function: send Sends data using socket
	 * 
	 * @socket: socket to send data using
	 * 
	 * @data: data to be sent
	 */
	public void send(Socket socket, byte[] data) throws IOException
	{
		// Send the data using DataOutputStream
		DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
		outputStream.writeInt(data.length);
		outputStream.write(data);
	}

	/*
	 * Function: start Communicates with server, runs functions on server, gets data
	 * from server, etc
	 */
	public void start()
	{
		try
		{
			// Start a connection to the server
			Client.this.clientSocket = new Socket(Client.this.serverAddress, Client.this.serverPort);
			Client.this.clientSocket.setReuseAddress(true);
			do
			{
				// And interact with the server
				Client.this.interactWithServer(Client.this.clientSocket);
			} while ((Client.this.runForever) && (!Client.this.shutdown));

			// Finally, close the client socket
			Client.this.clientSocket.close();
			// And notify that the connection was shut down
			Client.this.listener.onClientConnectionShutdown(serverAddress, serverPort);
		} catch (IOException e)
		{
			Client.this.listener.onErrorEncountered(e);
		}
	}

	/*
	 * Function: getClientSocket Gets client socket
	 * 
	 * @returns client socket
	 */
	public Socket getClientSocket()
	{
		return this.clientSocket;
	}

	class FindDevicesThread extends Thread
	{
		private List<String> devices = new ArrayList<>();
		private int minPort = 0;
		private int maxPort = 0;
		private String subnet = "";

		public FindDevicesThread(String subnet, int minPort, int maxPort)
		{
			this.subnet = subnet;
			this.minPort = minPort;
			this.maxPort = maxPort;
		}

		@Override
		public void run()
		{
			// Print that we are looking for a server
			System.out.println("Searching for VRify server, this can take a few minutes...");
			// We have the subnet, we need to make the final part of the address
			// So loop from 0 to 255
			for (int addressPart = 0; addressPart < 255; addressPart++)
			{
				// We also need to find whatever port is being used
				// So loop from minPort to maxPort
				for (int port = minPort; port < maxPort; port++)
				{
					// Form an ip address string using subnet and addressPart
					String ipAddress = subnet + "." + addressPart;
					// And check if the ipAddress and port are a VRify server
					if (Client.this.isDeviceServer(ipAddress, port))
					{
						// Add the ipAddress and port to devices list
						FindDevicesThread.this.devices.add(ipAddress + ":" + port);
					}
				}
			}
		}

		public List<String> getDevices()
		{
			return this.devices;
		}
	}
}
