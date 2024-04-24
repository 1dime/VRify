package com.onedime.vrify.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import com.onedime.vrify.FunctionData;
import com.onedime.vrify.FunctionWrapper;
import com.onedime.vrify.client.ClientData;

public class Server
{
	public static int MIN_PORT = 1000;
	public static int MAX_PORT = 1010;
	private boolean runForever = true;
	private FunctionWrapper functions;
	private int port = 0;
	private ServerListener listener;
	
	public Server(FunctionWrapper functions, int minPort, int maxPort, ServerListener listener)
	{
		//Set function wrapper, port, and listener
		this.functions = functions;
		this.port = generatePort(minPort, maxPort);
		this.listener = listener;
	}
	
	/*
	 * Function: generatePort
	 * Generates a valid random port number
	 * @minPort: minimum random port number
	 * @maxPort: maximum random port number
	 */
	public int generatePort(int minPort, int maxPort)
	{
		//First, generate a random port number
		int port = (int) (Math.random() * (maxPort - minPort)) + minPort;
		try
		{
			//Then, check if the port number is valid
			DatagramSocket socket = new DatagramSocket(port);
			socket.close();
			//Port number is valid, return it
			return port;
		} catch (Exception e)
		{
			//Port number is not valid, try again
			return generatePort(minPort, maxPort);
		}
	}
	
	/*
	 * Function: getPort
	 * Gets the generated port number
	 * @returns port number
	 */
	public int getPort()
	{
		return this.port;
	}
	
	/*
	 * Function: getListener
	 * Returns instance of server listener
	 * @returns server listener
	 */
	public ServerListener getListener()
	{
		return this.listener;
	}
	
	/*
	 * Function: setRunForever
	 * Changes runForever
	 * @runForever: boolean, for determining if server runs forever
	 */
	public void setRunForever(boolean runForever)
	{
		this.runForever = runForever;
	}
	
	/*
	 * Function: setListener
	 * Changes server listener
	 * @listener: listener to be used
	 */
	public void setListener(ServerListener listener)
	{
		this.listener = listener;
	}
	
	/*
	 * Function: start
	 * Starts thread that makes a UDP server for this program
	 */
	public void start()
	{
		//Create a thread for serving using UDP
		Thread serverThread = new Thread(new Runnable() {

			@Override
			public void run()
			{
				try
				{
					//Create a UDP server on this server's port with a reusable address
					DatagramSocket serverSocket = new DatagramSocket(Server.this.getPort());
					serverSocket.setReuseAddress(true);
					Server.this.listener.onServerStarted(Server.this.getPort());
					//And serve potentially forever
					do
					{
						//Do so by first receiving client data sent by client if client has connected
						byte[] serializedClientData = new byte[1024];
						DatagramPacket serializedPacket = new DatagramPacket(serializedClientData, serializedClientData.length);
						serverSocket.receive(serializedPacket);
						ClientData clientData = ClientData.deserialize(serializedClientData);
						//And run the function that is requested with its parameters
						FunctionData data = Server.this.functions.run(clientData.getFunctionName(), clientData.getParameters());
						//After, send the results (function data) to the client
						byte[] serializedFunctionData = data.serialize();
						serverSocket.send(new DatagramPacket(serializedFunctionData, serializedFunctionData.length, serializedPacket.getAddress(), serializedPacket.getPort()));
					} while(Server.this.runForever);
					
					//Finally, close the server
					serverSocket.close();
					Server.this.listener.onServerClosed(Server.this.getPort());
				} catch (IOException | InterruptedException e)
				{
					//Encountered an error, handle it
					Server.this.listener.onErrorEncountered(e);
				}
			}
			
		});
		//And run that thread
		serverThread.start();
	}
}
