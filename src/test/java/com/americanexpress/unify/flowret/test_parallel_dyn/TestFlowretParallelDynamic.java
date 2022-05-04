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

package com.americanexpress.unify.flowret.test_parallel_dyn;

import com.americanexpress.unify.base.BaseUtils;
import com.americanexpress.unify.flowret.*;
import org.junit.jupiter.api.*;

import java.io.File;

/*
 * @author Deepak Arora
 */
public class TestFlowretParallelDynamic {

  private static String dirPath = "./target/test-data-results/";
  private static Rts rts = null;
  private static FileDao dao = null;
  private static ProcessComponentFactory factory = null;
  private static EventHandler handler = null;

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
    com.aexp.acq.unify.flowret.TestUtils.deleteFiles(dirPath);
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

  public static void setScenario1() {
    // nothing to do
  }

  private static void runJourney(String journey) {
    String json = BaseUtils.getResourceAsString(TestFlowretParallelDynamic.class, "/flowret/" + journey + ".json");

    if (new File(dirPath + "flowret_journey-3.json ").exists() == false) {
      rts.startCase("3", json, null, null);
    }
    else {
      rts.resumeCase("3");
    }
  }

  private static void init(FlowretDao dao, ProcessComponentFactory factory, EventHandler handler, ISlaQueueManager sqm) {
    rts = Flowret.instance().getRunTimeService(dao, factory, handler, sqm);
  }

  @Test
  void testScenario1() {
    setScenario1();
    init(new FileDao(dirPath), new TestComponentFactoryParallelSupps(), new TestHandler(), null);
    runJourney("parallel_dyn_test");
  }

}
