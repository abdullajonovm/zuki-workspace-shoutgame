package com.meinc.hosted.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import sun.misc.BASE64Encoder;

import com.meinc.commons.domain.Account;
import com.meinc.commons.domain.Contact;
import com.meinc.commons.domain.Location;
import com.meinc.commons.domain.Organization;
import com.meinc.commons.domain.Subscriber;

public class Utility
{  
  public static boolean isEmpty(Object val)
  {
  	return val == null || val.toString().trim().length() == 0;
  }
  
  /**
   * Return true if the value passed in is 1, false for all others.
   * 
   * 
   * @param val 0 or 1 for false or true
   * @return boolean True if value is 1.
   * @throws IllegalArgumentException If can't convert to a numeric value.
   */
  public static boolean getBoolFromInt(String val) 
  throws IllegalArgumentException
  {
    final int YES_VAL = 1;
    
    int intVal = -1;
    boolean result = false;
    
    try
    {
      intVal = Integer.parseInt(val);  
    }
    catch (NumberFormatException nfe)
    {
      throw new IllegalArgumentException("Expected 0 or 1 instead found: " + val);
    }
    
    if (intVal == YES_VAL)
      result = true;
    
    return result;        
  }
  

  public static List<Integer> getIntegerList(List<?> list)
  {
    List<Integer> result = new ArrayList<Integer>();
    int i, count;

    if (list != null)
    {
      count = list.size();
      
      for (i = 0; i < count; i++)
      {
        result.add((Integer) list.get(i));  
      }           
    }
    
    return result;
  }
   
  public static List<Account> getAccountList(List<?> list)
  {
    List<Account> result = new ArrayList<Account>();
    int i, count;
    
    if (list != null)
    {
      count = list.size();
      for (i = 0; i < count; i++)
      {
        result.add((Account) list.get(i));  
      }      
    }
    
    return result;
  }
  
  public static List<Subscriber> getSubscriberList(List<?> list)
  {
    List<Subscriber> result = new ArrayList<Subscriber>();
    int i, count;
    
    if (list != null)
    {
      count = list.size();
      for (i = 0; i < count; i++)
      {
        result.add((Subscriber) list.get(i));  
      }      
    }
    
    return result;
  }
  
  public static List<Contact> getContactList(List<?> list)
  {
    List<Contact> result = new ArrayList<Contact>();
    int i, count;
    
    if (list != null)
    {
      count = list.size();
      for (i = 0; i < count; i++)
      {
        result.add((Contact) list.get(i));  
      }      
    }
    
    return result;
  }   
  
  public static List<Organization> getOrganizationList(List<?> list)
  {
    List<Organization> result = new ArrayList<Organization>();
    int i, count;
    
    if (list != null)
    {
      count = list.size();
      for (i = 0; i < count; i++)
      {
        result.add((Organization) list.get(i));  
      }      
    }
    
    return result;
  }
  
  public static List<Location> getLocationList(List<?> list)
  {
    List<Location> result = new ArrayList<Location>();
    int i, count;
    
    if (list != null)
    {
      count = list.size();
      for (i = 0; i < count; i++)
      {
        result.add((Location) list.get(i));  
      }      
    }
    
    return result;
  }   
  
  /**
   * Generate a temporary passord.  Take the string passed in, add the current
   * time to it and then use SHA-256 to create a one-way encrypted string
   * based on that value.  Then take the first 10 alpha/numeric characters
   * of that value and return it as the temporary password.
   * 
   * @param val The value to start out with to base the password on.
   * @return String The new temporary password.
   */
  public static String generateTempPassword(String val)
  {
    String encryptedString = oneWayEncrypt(val + System.currentTimeMillis());
    char[] chr = encryptedString.toCharArray();
    StringBuilder sb = new StringBuilder();
    int count = 0;

    for (char c : chr)
    {
      if ((c >= '0' && c <= '9' ) ||
          ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')))
      {
        sb.append(c);
        count++;
        if (count > 9)
          break;
      }
    } 
        
    return sb.toString();
  } 
  
  /**
   * Encrypt a string using the SHA-256 hash algorithm.
   * 
   * @param plaintext
   * @return the encrypted value
   * @throws NoSuchAlgorithmException if SHA-256 isn't supported by this JVM
   * @throws UnsupportedEncodingException if UTF-8 isn't supported by this JVM
   */
  public static String oneWayEncrypt(String plaintext) 
  {
    //java5 supports: MD2, MD5, SHA-1 (or SHA), SHA-256, SHA-384, and SHA-512
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      //will not happen
      e.printStackTrace();
      return plaintext;
    } 
    try {
      md.update(plaintext.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      //will not happen
      e.printStackTrace();
      return plaintext;
    } 
    byte raw[] = md.digest(); 
    String hash = (new BASE64Encoder()).encode(raw); 
    return hash;
  }  
}


