package com.meinc.mrsoa.service.exception;

public class AllServersBusy extends RuntimeException {

  private static final long serialVersionUID = -5828178644955595574L;

  public AllServersBusy() {
    super("All remote servers are busy");
  }

}
