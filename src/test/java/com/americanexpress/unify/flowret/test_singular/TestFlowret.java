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

package com.americanexpress.unify.flowret.test_singular;

import com.americanexpress.unify.flowret.*;
import com.americanexpress.unify.jdocs.BaseUtils;
import com.americanexpress.unify.jdocs.UnifyException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
 * @author Deepak Arora
 */
public class TestFlowret {

  private static String dirPath = null;
  private static Rts rts = null;

  @BeforeAll
  protected static void setEnv() {
    dirPath = System.getenv("TestFlowretDirPath");
    if (dirPath == null) {
      //      System.out.println("Please specify directory path as a system property");
      //      System.out.println("Do not forget to supply the trailing /  e.g. C:/Temp/");
      //      System.exit(1);
      dirPath = "C:/Deepak/Temp/";
    }

    ERRORS_FLOWRET.load();
    Flowret.init(10, 30000, "-");
    //    Flowret.instance().setWriteAuditLog(false);
    //    Flowret.instance().setWriteProcessInfoAfterEachStep(false);
  }

  @BeforeEach
  protected void beforeEach() {
    StepResponseFactory.clear();
  }

  @AfterAll
  protected static void close() {
    Flowret.instance().close();
  }

  private static void init(FlowretDao dao, ProcessComponentFactory factory, EventHandler handler, ISlaQueueManager sqm) {
    rts = Flowret.instance().getRunTimeService(dao, factory, handler, sqm);
  }

  private static void runJourney(String journey) {
    String json = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + ".json");
    String slaJson = null;

    try {
      slaJson = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + "_sla.json");
    }
    catch (Exception e) {
      // nothing to do
    }

    ProcessContext pc = null;
    if (new File(dirPath + "flowret_process_info-1.json").exists() == false) {
      pc = rts.startCase("1", json, null, slaJson);
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

  // pend scenario without ticket
  public static void setScenario1() {
    StepResponseFactory.addResponse("step3", UnitResponseType.OK_PEND, "comp3_wb", "");
    StepResponseFactory.addResponse("step11", UnitResponseType.OK_PEND_EOR, "comp11_wb", "");
    StepResponseFactory.addResponse("step11", UnitResponseType.ERROR_PEND, "comp11_err1", "");
    StepResponseFactory.addResponse("step11", UnitResponseType.ERROR_PEND, "comp11_err2", "");
    StepResponseFactory.addResponse("step13", UnitResponseType.ERROR_PEND, "comp13_err3", "");
    StepResponseFactory.addResponse("step13", UnitResponseType.ERROR_PEND, "comp13_err3", "");
    StepResponseFactory.addResponse("step13", UnitResponseType.ERROR_PEND, "comp13_err3", "");
    StepResponseFactory.addResponse("step14", UnitResponseType.ERROR_PEND, "comp14_wb", "");

    List<String> branches = new ArrayList<>();
    branches.add("yes");
    RouteResponseFactory.addResponse("route2", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route4", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route5", UnitResponseType.OK_PROCEED, branches, null);
  }

  // scenario with ticket no pend
  public static void setScenario2() {
    StepResponseFactory.addResponse("step16", UnitResponseType.OK_PROCEED, "", "final_step");

    List<String> branches = new ArrayList<>();
    branches.add("yes");
    RouteResponseFactory.addResponse("route3", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route4", UnitResponseType.OK_PROCEED, branches, null);
  }

  // scenario with ticket and pend
  public static void setScenario3() {
    StepResponseFactory.addResponse("step16", UnitResponseType.OK_PEND, "some_wb", "final_step");

    List<String> branches = new ArrayList<>();
    branches.add("yes");
    RouteResponseFactory.addResponse("route3", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route4", UnitResponseType.OK_PROCEED, branches, null);
  }

  // scenario with ticket and pend eor
  public static void setScenario4() {
    StepResponseFactory.addResponse("step16", UnitResponseType.OK_PEND_EOR, "some_wb", "final_step");

    List<String> branches = new ArrayList<>();
    branches.add("yes");
    RouteResponseFactory.addResponse("route3", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route4", UnitResponseType.OK_PROCEED, branches, null);
  }

  @Test
  void testClean() {
    List<String> branches = new ArrayList<>();
    branches.add("yes");
    RouteResponseFactory.addResponse("route2", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route4", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route5", UnitResponseType.OK_PROCEED, branches, null);

    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), null);
    runJourney("test_journey");
  }

  @Test
  void testScenario1() {
    setScenario1();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), null);
    runJourney("test_journey");
  }

  @Test
  void testScenario1WithSla() {
    setScenario1();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_journey");
  }

  @Test
  void testScenario2() {
    setScenario2();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), null);
    runJourney("test_journey");
  }

  @Test
  void testScenario2WithSla() {
    setScenario2();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_journey");
  }

  @Test
  void testScenario3() {
    setScenario3();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), null);
    runJourney("test_journey");
  }

  @Test
  void testScenario3WithSla() {
    setScenario3();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_journey");
  }

  @Test
  void testScenario4() {
    setScenario4();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), null);
    runJourney("test_journey");
  }

  @Test
  void testScenario4WithSla() {
    setScenario4();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_journey");
  }

  @Test
  void testPersist() {
    // set responses todo
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_persist");
  }

  @Test
  void testResume() {
    List<String> branches = new ArrayList<>();
    branches.add("yes");
    RouteResponseFactory.addResponse("route2", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route4", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route5", UnitResponseType.OK_PROCEED, branches, null);

    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), null);
    runJourney("test_journey");
  }

}
