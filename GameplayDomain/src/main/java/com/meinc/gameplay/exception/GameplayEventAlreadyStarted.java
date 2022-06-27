package com.meinc.gameplay.exception;

public class GameplayEventAlreadyStarted extends Exception
{
  private static final long serialVersionUID = 5494770397339687912L;

  public GameplayEventAlreadyStarted()
  {
    super("This gameplay event has already started.");
  }
}
