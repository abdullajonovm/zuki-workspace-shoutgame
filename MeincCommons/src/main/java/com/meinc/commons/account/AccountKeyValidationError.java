/**
 * 
 *
 * Copyright 2005 2006 The SCO Group, Inc. All rights reserved.
 */
package com.meinc.commons.account;

/**
 * @author bxgrant
 */
public class AccountKeyValidationError extends RuntimeException
{
  private static final long serialVersionUID = 1L;
  public static final int TYPE_UNKNOWN = 0;
  public static final int TYPE_BLANK = 1;
  public static final int TYPE_FIRST_CHAR_MUST_BE_CHAR = 2;
  public static final int TYPE_LAST_CHAR_MUST_BE_LETTER_OR_NUM = 3;  
  public static final int TYPE_MAX_LENGTH_EXCEEDED = 4;  
  public static final int TYPE_INVALID_CHAR_FOUND = 5;  
  
  private int _type = TYPE_UNKNOWN;

  /**
   * Default constructor.
   */
  public AccountKeyValidationError()
  {
    super();
  }

  public AccountKeyValidationError(String message)
  {
    super(message);
  }

  public AccountKeyValidationError(String message, Throwable ex)
  {
    super(message, ex);
  }
  
  public AccountKeyValidationError(int type)
  {
    _type = type;
  }

  public AccountKeyValidationError(Throwable ex)
  {
    super(ex);
  }

  /**
   * Getter for type member.
  
   * @return AccountKeyValidationException Returns the type.
   */
  public int getType()
  {
    return _type;
  }

  /**
   * Setter for member type.
  
   * @param type The type to set.
   */
  public void setType(int type)
  {
    _type = type;
  }

}
