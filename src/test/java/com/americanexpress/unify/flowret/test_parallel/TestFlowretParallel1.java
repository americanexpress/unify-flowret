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

package com.americanexpress.unify.flowret.test_parallel;

import com.americanexpress.unify.base.BaseUtils;
import com.americanexpress.unify.base.UnifyException;
import com.americanexpress.unify.flowret.*;
import com.americanexpress.unify.flowret.test_singular.TestFlowret;
import org.junit.jupiter.api.*;

import java.io.File;

/*
 * @author Deepak Arora
 */
public class TestFlowretParallel1 {

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
    TestUtils.deleteFiles(dirPath);
  }

  // 3 branches, happy path i.e. all branches proceed
  public static void setScenario1() {
    // nothing to do
  }

  private static void init(FlowretDao dao, ProcessComponentFactory factory, EventHandler handler, ISlaQueueManager sqm) {
    rts = Flowret.instance().getRunTimeService(dao, factory, handler, sqm);
  }

  private static void runJourney(String journey) {
    String json = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + ".json");
    if (new File(dirPath + "flowret_process_info-1.json").exists() == false) {
      rts.startCase("1", json, null, null);
    }

    try {
      while (true) {
        System.out.println();
        rts.resumeCase("1");
      }
    }
    catch (UnifyException e) {
      System.out.println("Exception -> " + e.getMessage());
    }
  }

  @Test
  void testScenario1() {
    setScenario1();
    init(new FileDao(dirPath), new TestComponentFactoryParallel1(), new TestHandler(), null);
    runJourney("parallel_test_1");
  }

}
