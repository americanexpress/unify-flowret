package com.americanexpress.unify.flowret.test_singular;

import com.americanexpress.unify.flowret.*;

public class TestSampleProgram1 {

  private static String dirPath = "./target/test-data-results/";
  private static Rts rts = null;

  public static void main(String[] args) {
    ERRORS_FLOWRET.load();
    Flowret.init(10, 30000, "-");
    foo3();
    Flowret.instance().close();
  }

  private static void foo3() {
    rts = Flowret.instance().getRunTimeService(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    rts.resumeCase("1");
  }

}
