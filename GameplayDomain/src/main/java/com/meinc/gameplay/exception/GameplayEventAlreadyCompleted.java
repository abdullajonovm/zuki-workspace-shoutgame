package com.meinc.gameplay.exception;

public class GameplayEventAlreadyCompleted 
extends RuntimeException
{
  private static final long serialVersionUID = 5494770397339687912L;

  public GameplayEventAlreadyCompleted()
  {
    super("This gameplay event has already completed.");
  }
}
