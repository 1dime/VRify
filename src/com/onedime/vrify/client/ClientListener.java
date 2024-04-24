package com.onedime.vrify.client;

import java.net.InetAddress;

import com.onedime.vrify.FunctionData;

public interface ClientListener
{
	public void onServerResponseReceived(FunctionData serverResponse);
	public void onDataSentToServer(ClientData data, InetAddress serverAddress, int serverPort);
	public void onClientConnectionShutdown(InetAddress serverAddress, int serverPort);
	ClientData getFunctionToRun();
	void onErrorEncountered(Exception error);
}
