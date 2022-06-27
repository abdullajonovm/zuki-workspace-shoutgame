package com.meinc.server.sao;

import com.meinc.commons.account.IAccount;
import com.meinc.commons.appregistry.IAppRegistry;
import com.meinc.commons.cache.IWebCache;
import com.meinc.commons.team.ITeam;

public interface IPhoenixCommons
{
	/**
	 * @return Returns the team.
	 */
	public ITeam getTeam();
  
  /** 
   * @return returns the system-wide account management service.
   */
  public IAccount getAccount();  
  
  public IAppRegistry getAppRegistry();
  
  public IWebCache getWebCache();
}