package com.onedime.vrify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class FunctionData implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object[] parameters;
	private Object results;
	
	public FunctionData(Object[] parameters, Object results)
	{
		//Define parameters and results
		this.parameters = parameters;
		this.results = results;
	}
	
	/*
	 * Function: deserialize
	 * Deserializes potentially serialized function data
	 * @serializedFunctionData: byte array that is potentially serialized function data
	 * @returns: function data, if no error occurs, otherwise null.
	 */
	public static FunctionData deserialize(byte[] serializedFunctionData)
	{
		try
		{
			//Create a byte array input stream
			ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(serializedFunctionData);
			//And make an object input stream using arrayInputStream
			ObjectInputStream inputStream = new ObjectInputStream(arrayInputStream);
			//Now, read the function data from inputStream
			FunctionData data = (FunctionData) inputStream.readObject();
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
	public Object[] getParameters()
	{
		//Get the parameters
		return this.parameters;
	}
	
	public Object getResults()
	{
		//Get the results
		return this.results;
	}
	
	public void setParameters(Object[] parameters)
	{
		//Set the parameters
		this.parameters = parameters;
	}
	
	public void setResults(Object results)
	{
		//Set the results
		this.results = results;
	}
	
	public byte[] serialize()
	{
		try
		{
			//Create an output stream for byte arrays
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			//And create an output stream for objects using arrayOutputStream
			ObjectOutputStream outputStream = new ObjectOutputStream(arrayOutputStream);
			//Now, use outputStream to convert FunctionData to a byte array
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
