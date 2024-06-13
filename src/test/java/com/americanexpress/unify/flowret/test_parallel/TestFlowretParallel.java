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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.lang.invoke.MethodHandles;

/*
 * @author Deepak Arora
 */
public class TestFlowretParallel {

  private static String baseDirPath = "./target/test-data-results/";
  private static Rts rts = null;
  private static String simpleClassName = MethodHandles.lookup().lookupClass().getSimpleName();

  // set to true if you want to log to disk to trouble shoot any specific test case
  private static boolean writeFiles = false;

  // set to true if you want to log to console
  private static boolean writeToConsole = false;

  @BeforeAll
  protected static void beforeAll() {
    TestManager.init(System.out, new ByteArrayOutputStream(), 20, 30000);
  }

  @BeforeEach
  protected void beforeEach() {
    TestManager.reset();
    StepResponseFactory.clear();
  }

  private static void init(FlowretDao dao, ProcessComponentFactory factory, EventHandler handler, ISlaQueueManager sqm) {
    rts = Flowret.instance().getRunTimeService(dao, factory, handler, sqm);
  }

  // 3 branches, happy path i.e. all branches proceed
  public static void setScenario1() {
    // nothing to do
  }

  // 3 branches, 1st branch ok proceeds, 2nd branch pends, 3rd branch pends
  public static void setScenario2() {
    StepResponseFactory.addResponse("step_2_1", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_2", UnitResponseType.OK_PEND, "test_wb", "");
    StepResponseFactory.addResponse("step_2_2", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_3", UnitResponseType.OK_PEND, "test_wb", "");
    StepResponseFactory.addResponse("step_2_3", UnitResponseType.OK_PROCEED, "", "");
  }

  // 3 branches, all 3 error pend
  public static void setScenario2_1() {
    StepResponseFactory.addResponse("step_2_1", UnitResponseType.ERROR_PEND, "error_wb", "");
    StepResponseFactory.addResponse("step_2_1", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_2", UnitResponseType.ERROR_PEND, "error_wb", "");
    StepResponseFactory.addResponse("step_2_2", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_3", UnitResponseType.ERROR_PEND, "error_wb", "");
    StepResponseFactory.addResponse("step_2_3", UnitResponseType.OK_PROCEED, "", "");
  }

  // 3 branches, 1st branch ok proceeds, 2nd branch pends and 3rd branch raises ticket
  // then a further pend after ticket is raised
  public static void setScenario3() {
    StepResponseFactory.addResponse("step_2_1", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_2", UnitResponseType.OK_PEND, "test_wb", "");
    StepResponseFactory.addResponse("step_2_2", UnitResponseType.OK_PROCEED, "", "");

    // we add delay to ensure that the ticket is set after above two branches are executed
    StepResponseFactory.addResponse("step_2_3", UnitResponseType.OK_PROCEED, "", "reject", 500);

    StepResponseFactory.addResponse("step_4", UnitResponseType.OK_PEND, "test_wb", "");
    StepResponseFactory.addResponse("step_4", UnitResponseType.OK_PROCEED, "", "");
  }

  // 3 branches, 1st branch ok proceeds, 2nd branch pends and 3rd branch raises ticket
  // with a pend, then a further pend after ticket is raised
  public static void setScenario4() {
    StepResponseFactory.addResponse("step_2_1", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_2", UnitResponseType.OK_PEND, "test_wb", "");
    StepResponseFactory.addResponse("step_2_2", UnitResponseType.OK_PROCEED, "", "");

    StepResponseFactory.addResponse("step_2_3", UnitResponseType.OK_PEND, "test_wb", "reject", 1000);

    StepResponseFactory.addResponse("step_4", UnitResponseType.OK_PEND, "test_wb", "");
    StepResponseFactory.addResponse("step_4", UnitResponseType.OK_PROCEED, "", "");
  }

  private static void runJourney(String journey, MemoryDao dao) {
    String json = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + ".json");
    String slaJson = null;

    slaJson = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + "_sla.json");
    if (dao.read("flowret_process_info-1.json") == null) {
      rts.startCase("1", json, null, slaJson);
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
    MemoryDao dao = new MemoryDao();
    String methodName = new Object() {
    }.getClass().getEnclosingMethod().getName();
    String path = baseDirPath + simpleClassName + "/" + methodName + "/";
    setScenario1();
    init(dao, new TestComponentFactoryParallel(), new TestHandler(), null);
    runJourney("parallel_test", dao);
    TestManager.writeFiles(writeFiles, path, dao.getDocumentMap());
    TestManager.myAssertEquals2(writeToConsole, simpleClassName + "." + methodName, "/flowret/test_parallel/test_scenario_1_expected.txt");
  }

  @Test
  void testScenario2() {
    MemoryDao dao = new MemoryDao();
    String methodName = new Object() {
    }.getClass().getEnclosingMethod().getName();
    String path = baseDirPath + simpleClassName + "/" + methodName + "/";
    setScenario2();
    init(dao, new TestComponentFactoryParallel(), new TestHandler(), null);
    runJourney("parallel_test", dao);
    TestManager.writeFiles(writeFiles, path, dao.getDocumentMap());
    TestManager.myAssertEquals2(writeToConsole, simpleClassName + "." + methodName, "/flowret/test_parallel/test_scenario_2_expected.txt");
  }

  @Test
  void testScenario2_1() {
    MemoryDao dao = new MemoryDao();
    String methodName = new Object() {
    }.getClass().getEnclosingMethod().getName();
    String path = baseDirPath + simpleClassName + "/" + methodName + "/";
    setScenario2_1();
    init(dao, new TestComponentFactoryParallel(), new TestHandler(), null);
    runJourney("parallel_test", dao);
    TestManager.writeFiles(writeFiles, path, dao.getDocumentMap());
    TestManager.myAssertEquals2(writeToConsole, simpleClassName + "." + methodName, "/flowret/test_parallel/test_scenario_2_1_expected.txt");
  }

  @Test
  void testScenario3() {
    MemoryDao dao = new MemoryDao();
    String methodName = new Object() {
    }.getClass().getEnclosingMethod().getName();
    String path = baseDirPath + simpleClassName + "/" + methodName + "/";
    setScenario3();
    init(dao, new TestComponentFactoryParallel(), new TestHandler(), null);
    runJourney("parallel_test", dao);
    TestManager.writeFiles(writeFiles, path, dao.getDocumentMap());
    TestManager.myAssertEquals2(writeToConsole, simpleClassName + "." + methodName, "/flowret/test_parallel/test_scenario_3_expected.txt");
  }

  @Test
  void testScenario4() {
    MemoryDao dao = new MemoryDao();
    String methodName = new Object() {
    }.getClass().getEnclosingMethod().getName();
    String path = baseDirPath + simpleClassName + "/" + methodName + "/";
    setScenario4();
    init(dao, new TestComponentFactoryParallel(), new TestHandler(), null);
    runJourney("parallel_test", dao);
    TestManager.writeFiles(writeFiles, path, dao.getDocumentMap());
    TestManager.myAssertEquals2(writeToConsole, simpleClassName + "." + methodName, "/flowret/test_parallel/test_scenario_4_expected.txt");
  }

}
