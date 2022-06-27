package com.meinc.gameplay.exception;

public class PollAlreadyStarted extends Exception
{
  private static final long serialVersionUID = 5494770397339687912L;

  public PollAlreadyStarted()
  {
    super("This poll has already complted.");
  }
}
