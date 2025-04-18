package com.americanexpress.unify.flowret;

public class PendStatus {

  private String workBasket = null;
  private String execPath = null;

  public PendStatus(String workBasket, String execPath) {
    this.workBasket = workBasket;
    this.execPath = execPath;
  }

  public String getWorkBasket() {
    return workBasket;
  }

  public String getExecPath() {
    return execPath;
  }

}
