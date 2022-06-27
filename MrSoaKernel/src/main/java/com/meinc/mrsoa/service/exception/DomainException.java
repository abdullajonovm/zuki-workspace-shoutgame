package com.meinc.mrsoa.service.exception;

import java.util.Map;

/**
 * Allows implementing Exceptions to contain domain objects that can be
 * successfully thrown across service method calls.
 * 
 * @author mpontius
 */
public interface DomainException {
  public Map<String,Object> getDomainMap();
  public void setDomainMap(Map<String,Object> domainMap);
}
