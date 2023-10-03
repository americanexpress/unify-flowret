/*
 * Copyright 2020 American Express Travel Related Services Company, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.americanexpress.unify.flowret.test_parallel_in_dyn_parallel;

import com.americanexpress.unify.base.BaseUtils;
import com.americanexpress.unify.base.UnifyException;
import com.americanexpress.unify.flowret.*;
import org.junit.jupiter.api.*;

import java.io.File;

/*
 * @author Deepak Arora
 */
public class TestFlowretPidp {

  private static String dirPath = "./target/test-data-results/";
  private static Rts rts = null;

  @BeforeAll
  protected static void setEnv() throws Exception {
    File directory = new File(dirPath);
    if (!directory.exists()) {
      directory.mkdir();
    }

    ERRORS_FLOWRET.load();
    Flowret.init(10, 30000, "-");
  }

  @BeforeEach
  protected void beforeEach() {
    TestUtils.deleteFiles(dirPath);
    StepResponseFactory.clear();
  }

  @AfterEach
  protected void afterEach() {
    // nothing to do
  }

  @AfterAll
  protected static void afterAll() {
    Flowret.instance().close();
  }

  // happy path
  public static void setScenario0() {
    // nothing to do
  }

  // pend in a step in dynamic parallel route scope
  public static void setScenario1() {
    StepResponseFactory.addResponse("r2_1_s1", UnitResponseType.ERROR_PEND, "tech", "");
  }

  private static void runJourney(String journey) {
    String json = BaseUtils.getResourceAsString(TestFlowretPidp.class, "/flowret/" + journey + ".json");

    if (new File(dirPath + "flowret_journey-3.json ").exists() == false) {
      rts.startCase("3", json, null, null);
    }

    try {
      while (true) {
        System.out.println();
        rts.resumeCase("3");
      }
    }
    catch (UnifyException e) {
      System.out.println("Exception -> " + e.getMessage());
    }

  }

  private static void init(FlowretDao dao, ProcessComponentFactory factory, EventHandler handler, ISlaQueueManager sqm) {
    rts = Flowret.instance().getRunTimeService(dao, factory, handler, sqm);
  }

  @Test
  void testScenario0() {
    setScenario0();
    init(new FileDao(dirPath), new TestComponentFactoryPidp(), new TestHandler(), null);
    runJourney("pidp_test");
  }

  @Test
  void testScenario1() {
    setScenario1();
    init(new FileDao(dirPath), new TestComponentFactoryPidp(), new TestHandler(), null);
    runJourney("pidp_test");
  }

}
