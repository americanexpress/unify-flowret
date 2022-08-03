package com.americanexpress.unify.flowret;

public class TestStepResponse {

  private StepResponse stepResponse = null;
  private long delay = 0;

  public TestStepResponse(StepResponse stepResponse, long delay) {
    this.stepResponse = stepResponse;
    this.delay = delay;
  }

  public StepResponse getStepResponse() {
    return stepResponse;
  }

  public long getDelay() {
    return delay;
  }

}
