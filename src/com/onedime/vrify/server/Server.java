package com.onedime.vrify.server;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import javax.xml.crypto.Data;

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
	private Thread serverThread;
	private boolean running = false;
	private ServerSocket serverSocket;
	
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
		//For checking if a port is set, used in loop below
		boolean foundPort = false;
		//Loop until a port is found
		while(!foundPort)
		{
			//First, generate a random port number
			int port = (int) (Math.random() * (maxPort - minPort)) + minPort;
			try
			{
				//Then, check if the port number is valid
				DatagramSocket socket = new DatagramSocket(port);
				socket.close();
				//Valid port was found
				foundPort = true;
				//Port number is valid, return it
				return port;
			} catch (Exception e)
			{
				//Port number is not valid, try again
				foundPort = false;
			}
		}
		
		//Return 0, this wont be reached
		return 0;
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
	 * Function: read
	 * Reads data from socket
	 * @socket: socket to send data to
	 * @returns data from socket
	 */
	public byte[] read(Socket socket) throws IOException
	{
		//Read data using DataInputStream
		DataInputStream inputStream = new DataInputStream(socket.getInputStream());
		int length = inputStream.readInt();
		byte[] data = new byte[length];
		inputStream.readFully(data, 0, length);
		
		//And return it
		return data;
	}
	
	/*
	 * Function: send
	 * Sends data using socket
	 * @socket: socket to send data using
	 * @data: data to be sent
	 */
	public void send(Socket socket, byte[] data) throws IOException
	{
		//Send the data using DataOutputStream
		DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
		outputStream.writeInt(data.length);
		outputStream.write(data);
	}
	
	/*
	 * Function: start
	 * Starts thread that makes a UDP server for this program
	 */
	public void start()
	{
		//Create a thread for serving using UDP
		this.serverThread = new Thread(new Runnable() {

			@Override
			public void run()
			{
				//Create a server on the given port and notify user
				try
				{
					Server.this.serverSocket = new ServerSocket(Server.this.getPort());
					Server.this.serverSocket.setReuseAddress(true);
					Server.this.listener.onServerStarted(Server.this.getPort());
					do
					{
						try {
							//Listen for a connection from the client
							Socket socket = Server.this.serverSocket.accept();
							do
							{
								//Then get the data sent by client
								byte[] serializedClientData = Server.this.read(socket);
								ClientData clientData = ClientData.deserialize(serializedClientData);
								//And notify user that client has connected
								FunctionData dataReceived = Server.this.listener.onClientDataReceived(socket, clientData);
								
								//Now, run the requested function
								FunctionData requestedFunction = Server.this.functions.run(clientData.getFunctionName(), clientData.getParameters());
								
								//And send both received data and requested function results
								Server.this.send(socket, dataReceived.serialize());
								Server.this.send(socket, requestedFunction.serialize());	
							} while(Server.this.runForever && (!(socket.isClosed())));
						}
						catch(Exception e)
						{
							Server.this.listener.onErrorEncountered(e);
						}
					} while(((Server.this.runForever) && (Server.this.running))
							|| (Server.this.serverSocket != null));
					
				} catch(IOException ioe)
				{
					Server.this.listener.onErrorEncountered(ioe);
				}
			}
			
		});
		//And run that thread
		serverThread.start();
	}
	
	/*
	 * Function: stop
	 * Stops this server
	 */
	public void stop()
	{
		//Check if the server thread was set
		if(this.serverThread != null)
		{
			//Check if the server socket is set
			if(this.serverSocket != null)
			{
				try
				{
					//Stop the server thread
					this.serverThread.interrupt();
					//Close the server socket
					this.serverSocket.close();
					//And set isRunning to false
					this.running = false;
					//And notify that server was closed
					Server.this.listener.onServerClosed(Server.this.getPort());
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/*
	 * Function: isRunning
	 * Gets if server is running
	 * @returns if server is running
	 */
	public boolean isRunning()
	{
		return this.running;
	}
}
