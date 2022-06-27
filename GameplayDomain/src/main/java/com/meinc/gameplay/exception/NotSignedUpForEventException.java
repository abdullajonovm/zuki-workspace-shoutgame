package com.meinc.gameplay.exception;

public class NotSignedUpForEventException 
extends RuntimeException
{
  private static final long serialVersionUID = 5494770397339687912L;

  public NotSignedUpForEventException()
  {
    super("This gameplay event has already completed.");
  }
}
