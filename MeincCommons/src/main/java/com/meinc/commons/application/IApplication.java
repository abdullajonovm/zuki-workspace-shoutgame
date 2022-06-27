package com.meinc.commons.application;

import com.meinc.commons.helper.Parameters;
import com.meinc.commons.helper.Result;

/**
 * A class implementing this interface must be able to process a given set of commands.
 * 
 * @author shawker
 *
 */
public interface IApplication
{
	public static final int PRIVATE_CONTACT_MODIFIER = 0; 

	/**
	 * Determines whether or not this class knows how to process a given command.
	 *  
	 * @param cmd which command to execute
	 * @return
	 */
	public boolean canProcessCommand(int cmd);
	
	/**
	 * Whether or not the user is authorized to execute the given command.
	 * 
	 * @param cmd which command to execute
	 * @param params
	 * @return
	 */
	public boolean isAuthorized(int cmd, Parameters params);
	
	/**
	 * Process then given command.
	 * 
	 * @param cmd which command to execute
	 * @param params
	 * @return the result of the processing
	 */
	public Result processCommand(int cmd, Parameters params);
	
}
