package com.onedime.vrify.server;

import java.net.Socket;

import com.onedime.vrify.FunctionData;
import com.onedime.vrify.client.ClientData;

public interface ServerListener
{
	public void onServerStarted(int port);
	public FunctionData onClientDataReceived(Socket socket, ClientData data);
	public void onErrorEncountered(Exception error);
	public void onServerClosed(int port);
}
