package com.meinc.gameplay.exception;

public class GameplayEventNotCompletedYet extends Exception
{
  private static final long serialVersionUID = 5494770397339687912L;

  public GameplayEventNotCompletedYet()
  {
    super("This gameplay event is not yet completed.");
  }
}
