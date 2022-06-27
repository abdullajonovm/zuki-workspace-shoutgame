package com.meinc.commons.domain;

import java.io.Serializable;

public class ReferenceId implements Serializable
{
  private static final long serialVersionUID = -2698257303747739566L;
 
  private int _accountId;
  private int _referenceId;
  
  public ReferenceId()
  {
  }
  
  public ReferenceId(int accountId, int referenceId)
  {
	  _accountId = accountId;
	  _referenceId = referenceId;
  }
  
  public int getReferenceId()
  {
    return _referenceId;
  }

  public void setReferenceId(int referenceId)
  {
	  _referenceId = referenceId;
  }
  
  public int getAccountId()
  {
    return _accountId;
  }

  public void setAccountId(int accountId)
  {
	  _accountId = accountId;
  }
}
