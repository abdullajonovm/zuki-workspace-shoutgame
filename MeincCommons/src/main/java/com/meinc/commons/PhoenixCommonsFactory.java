package com.meinc.commons;

import com.meinc.commons.proxy.PhoenixCommonsProxy;
import com.meinc.server.sao.IPhoenixCommons;


/**
 * This class exists to go retrieve the instance of the IPhoenixCommons all
 * projects should use.
 * 
 * @author bxgrant
 */
public class PhoenixCommonsFactory {

	private static IPhoenixCommons _commons = new PhoenixCommonsProxy();
	
	public static IPhoenixCommons getInstance() {
		return _commons;
	}
}
