package com.meinc.commons.domain;

import java.io.Serializable;

import com.meinc.commons.domain.Subscriber;

public class Session implements Serializable
{
  private static final long serialVersionUID = 909311348763283532L;
  private Subscriber _subscriber;
	private String _sessionId;
	
	public Session(Subscriber subscriber, String sessionId)
	{
		_subscriber = subscriber;
    _sessionId = sessionId;
	}

	public Subscriber getSubscriber()
	{
		return _subscriber;
	}

	public String getSessionId()
	{
		return _sessionId;
	}
}
