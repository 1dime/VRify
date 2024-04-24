package com.onedime.vrify.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.onedime.vrify.FunctionData;

public class ClientData implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String deviceName;
	private String functionName;
	private Object[] parameters;
	
	public ClientData(String deviceName, String functionName, Object[] parameters)
	{
		this.deviceName = deviceName;
		this.functionName = functionName;
		this.parameters = parameters;
	}
	
	/*
	 * Function: deserialize
	 * Deserializes potentially serialized instance of ClientData
	 * @serializedClientData: potentially serialized client data
	 * @returns: an instance of ClientData if serializedClientData is what it says it is, null if otherwise.
	 */
	public static ClientData deserialize(byte[] serializedClientData)
	{
		try
		{
			//Create a byte array input stream
			ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(serializedClientData);
			//And make an object input stream using arrayInputStream
			ObjectInputStream inputStream = new ObjectInputStream(arrayInputStream);
			//Now, read the client data from inputStream
			ClientData data = (ClientData) inputStream.readObject();
			//And close the input streams
			inputStream.close();
			arrayInputStream.close();
			//And return the function data
			return data;
		} catch (IOException | ClassNotFoundException e)
		{
			//Print the stack trace for the error that occurred
			e.printStackTrace();
		}
		
		//An error occurred, so return nothing
		return null;
	}
	
	/*
	 * Function: getDeviceName
	 * Returns name of device given by user
	 * @returns name of device
	 */
	public String getDeviceName()
	{
		return this.deviceName;
	}
	
	/*
	 * Function: getFunctionName
	 * Returns name of function to be ran on server
	 * @returns name of function
	 */
	public String getFunctionName()
	{
		return this.functionName;
	}
	
	/*
	 * Function: getParameters
	 * Returns parameters to be passed into function
	 * @returns function parameters
	 */
	public Object[] getParameters()
	{
		return this.parameters;
	}
	

	public byte[] serialize()
	{
		try
		{
			//Create an output stream for byte arrays
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			//And create an output stream for objects using arrayOutputStream
			ObjectOutputStream outputStream = new ObjectOutputStream(arrayOutputStream);
			//Now, use outputStream to convert ClientData to a byte array
			outputStream.writeObject(this);
			return arrayOutputStream.toByteArray();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//An error occurred, so return nothing
		return null;
	}
}
