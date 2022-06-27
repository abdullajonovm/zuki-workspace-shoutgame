package com.meinc.gameplay.exception;

public class PollNotCompletedYet extends Exception
{
  private static final long serialVersionUID = 5494770397339687912L;

  public PollNotCompletedYet()
  {
    super("This poll is not yet completed.");
  }
}
