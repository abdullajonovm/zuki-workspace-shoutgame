package com.meinc.commons.proxy;

import com.meinc.commons.account.IAccount;
import com.meinc.commons.appregistry.IAppRegistry;
import com.meinc.commons.cache.IWebCache;
import com.meinc.commons.cache.WebCacheURL;
import com.meinc.commons.team.ITeam;
import com.meinc.server.sao.IPhoenixCommons;

public class PhoenixCommonsProxy implements IPhoenixCommons {
	public static final String PHOENIX_NAMESPACE = "phoenix-service";
	
	private IAccount _account = new AccountServiceClientProxy();
	private ITeam _team = new HostedTeamServiceClientProxy();
	private IAppRegistry _appRegistry = new PhoenixAppRegistry();

	public IAccount getAccount() {
		return _account;
	}

	public ITeam getTeam() {
		return _team;
	}

	public IAppRegistry getAppRegistry() {
		return _appRegistry;
	}

  public IWebCache getWebCache() {
    return WebCacheURL.getInstance();
  }
}
