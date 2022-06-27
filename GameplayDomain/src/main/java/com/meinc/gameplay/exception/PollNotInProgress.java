package com.meinc.gameplay.exception;

public class PollNotInProgress 
extends RuntimeException
{
  private static final long serialVersionUID = 5494770397339687912L;

  public PollNotInProgress()
  {
    super("This poll is not yet in progress.");
  }
}
