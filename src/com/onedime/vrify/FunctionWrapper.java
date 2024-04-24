package com.onedime.vrify;

import java.util.HashMap;
import java.util.Map;

public class FunctionWrapper 
{
	//Function interface, for passing parameters and getting returned data
	//from function in map
	public static interface Function extends java.util.function.Function<Object[], Object>{};
	//Map of functions and respective Function instances
	private Map<String, Function> functionMap;
	
	public FunctionWrapper()
	{
		//Initialize function map
		this.functionMap = new HashMap<>();
	}
	
	/*
	 *Function: addFunction
	 *Adds function to function map
	 *@param name - Name of function to be added
	 *@param function - Function class instance associated with function
	 */
	
	public void addFunction(String name, Function function)
	{
		//Add function to map
		this.functionMap.put(name, function);
	}
	
	/*
	 * Function: getFunction
	 * Returns function given name if it is found
	 * @param name - Name of function to look for
	 * @return Function from map, if one is found
	 */
	public Function getFunction(String name)
	{
		//Look for function name and return it
		return this.functionMap.get(name);
	}
	
	/*
	 * Function: run
	 * Runs function with name, if it is in map
	 * @param name - Name of function to be ran
	 * @param params - Parameters to be passed into function
	 * @return results and parameters for function after running
	 */
	public FunctionData run(String name, Object ...params) throws InterruptedException
	{
		//Look for function in map
		Function function = getFunction(name);
		//Check if the function was found
		if(function != null)
		{
			//Create a new function data instance
			FunctionData data = new FunctionData(params, null);
			//Run the function in a thread
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					//Run the function
					data.setResults(function.apply(params));
				}
			});
			
			//Run the function thread and wait to get response
			thread.start();
			thread.join();
			
			//And return the function data
			return data;
		}
		//Function was not found
		return null;
	}
}
