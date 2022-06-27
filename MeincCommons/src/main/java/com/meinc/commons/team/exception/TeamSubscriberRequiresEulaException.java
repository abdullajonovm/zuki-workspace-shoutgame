package com.meinc.commons.team.exception;

import java.util.HashMap;
import java.util.Map;

import com.meinc.commons.domain.Subscriber;
import com.meinc.mrsoa.service.exception.DomainException;

public class TeamSubscriberRequiresEulaException extends RuntimeException implements DomainException {
	private static final long serialVersionUID = 1L;
	
  private Map<String, Object> _domainMap = new HashMap<String, Object>(1);
	
  public TeamSubscriberRequiresEulaException() {
    super();
  }

  public TeamSubscriberRequiresEulaException(String message) {
    super(message);
  }

  /**
   * @return Returns the subscriber.
   */
  public Subscriber getSubscriber()
  {
    return (Subscriber) _domainMap.get("subscriber");
  }

  /**
   * @param subscriber The subscriber to set.
   */
  public void setSubscriber(Subscriber subscriber)
  {
    _domainMap.put("subscriber", subscriber);
  }

  public Map<String, Object> getDomainMap() {
    return _domainMap;
  }

  public void setDomainMap(Map<String, Object> domainMap) {
    _domainMap = domainMap;
  }
}
