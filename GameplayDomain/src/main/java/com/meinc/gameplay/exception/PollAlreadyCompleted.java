package com.meinc.gameplay.exception;

public class PollAlreadyCompleted extends Exception
{
  private static final long serialVersionUID = 5494770397339687912L;

  public PollAlreadyCompleted()
  {
    super("This poll has already started.");
  }
}
