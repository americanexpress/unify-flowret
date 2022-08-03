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

import com.americanexpress.unify.base.BaseUtils;
import com.americanexpress.unify.base.UnifyException;
import com.americanexpress.unify.flowret.*;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * @author Deepak Arora
 */
public class TestFlowret {

  // NOTE -> while setting the contents of the expected files, do not use the IntelliJ editor
  // When we run the assertEquals method, for some reason, the empty space at the end of the line in the
  // contents of the expected file is being trimmed. This does not happen if we use Notepad++ to
  // save the contents of the expected files

  private static String dirPath = "./target/test-data-results/";
  private static Rts rts = null;
  private static PrintStream previousConsole = null;
  private static ByteArrayOutputStream newConsole = null;

  @BeforeAll
  protected static void setEnv() {
    previousConsole = System.out;
    newConsole = new ByteArrayOutputStream();
    System.setOut(new PrintStream(newConsole));

    File directory = new File(dirPath);
    if (!directory.exists()) {
      directory.mkdir();
    }

    ERRORS_FLOWRET.load();
    Flowret.init(10, 30000, "-");
    //    Flowret.instance().setWriteAuditLog(false);
    //    Flowret.instance().setWriteProcessInfoAfterEachStep(false);
  }

  @BeforeEach
  protected void beforeEach() {
    com.aexp.acq.unify.flowret.TestUtils.deleteFiles(dirPath);
    StepResponseFactory.clear();
    newConsole.reset();
  }

  @AfterEach
  protected void afterEach() {
    // nothing to do
  }

  @AfterAll
  protected static void afterAll() {
    System.setOut(previousConsole);
    Flowret.instance().close();
    com.aexp.acq.unify.flowret.TestUtils.deleteFiles(dirPath);
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

  // pend scenario to check last pend work basket feature
  public static void setScenario5() {
    StepResponseFactory.addResponse("step3", UnitResponseType.OK_PEND, "comp3_wb", "");
    StepResponseFactory.addResponse("step11", UnitResponseType.OK_PEND, "comp11_wb", "");
    StepResponseFactory.addResponse("step13", UnitResponseType.ERROR_PEND, "tech", "");
    StepResponseFactory.addResponse("step13", UnitResponseType.ERROR_PEND, "tech", "");
    StepResponseFactory.addResponse("step13", UnitResponseType.ERROR_PEND, "tech", "");
    StepResponseFactory.addResponse("step13", UnitResponseType.ERROR_PEND, "tech", "");
    StepResponseFactory.addResponse("step13", UnitResponseType.ERROR_PEND, "tech", "");
    StepResponseFactory.addResponse("step14", UnitResponseType.ERROR_PEND, "comp14_wb", "");

    List<String> branches = new ArrayList<>();
    branches.add("yes");
    RouteResponseFactory.addResponse("route2", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route4", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route5", UnitResponseType.OK_PROCEED, branches, null);
  }

  private void myAssertEquals(String testCase, String resourcePath) {
    String s = newConsole.toString();
    String output = BaseUtils.getWithoutCarriageReturn(s);
    String expected = BaseUtils.getResourceAsString(TestFlowret.class, resourcePath);
    expected = BaseUtils.getWithoutCarriageReturn(expected);
    assertEquals(expected, output);
    previousConsole.println();
    previousConsole.println();
    previousConsole.println("*********************** " + testCase + " ***********************");
    previousConsole.println();
    previousConsole.println(s);
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
    myAssertEquals("testClean", "/flowret/test_singular/test_clean_expected.txt");
  }

  @Test
  void testScenario1() {
    setScenario1();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), null);
    runJourney("test_journey");
    myAssertEquals("testScenario1", "/flowret/test_singular/test_scenario_1_expected.txt");
  }

  @Test
  void testScenario1WithSla() {
    setScenario1();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_journey");
    myAssertEquals("testScenario1WithSla", "/flowret/test_singular/test_scenario_1_sla_expected.txt");
  }

  @Test
  void testScenario2() {
    setScenario2();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), null);
    runJourney("test_journey");
    myAssertEquals("testScenario2", "/flowret/test_singular/test_scenario_2_expected.txt");
  }

  @Test
  void testScenario2WithSla() {
    setScenario2();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_journey");
    myAssertEquals("testScenario2WithSla", "/flowret/test_singular/test_scenario_2_sla_expected.txt");
  }

  @Test
  void testScenario3() {
    setScenario3();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), null);
    runJourney("test_journey");
    myAssertEquals("testScenario3", "/flowret/test_singular/test_scenario_3_expected.txt");
  }

  @Test
  void testScenario3WithSla() {
    setScenario3();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_journey");
    myAssertEquals("testScenario3WithSla", "/flowret/test_singular/test_scenario_3_sla_expected.txt");
  }

  @Test
  void testScenario4() {
    setScenario4();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), null);
    runJourney("test_journey");
    myAssertEquals("testScenario4", "/flowret/test_singular/test_scenario_4_expected.txt");
  }

  @Test
  void testScenario4WithSla() {
    setScenario4();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_journey");
    myAssertEquals("testScenario4WithSla", "/flowret/test_singular/test_scenario_4_sla_expected.txt");
  }

  @Test
  void testScenario5() {
    setScenario5();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), null);
    runJourney("test_journey");
    // myAssertEquals("testScenario5", "/flowret/test_singular/test_scenario_1_expected.txt");
  }

  @Test
  void testPersist() {
    // set responses todo
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_persist");
    myAssertEquals("testPersist", "/flowret/test_singular/test_persist_expected.txt");
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
    myAssertEquals("testResume", "/flowret/test_singular/test_resume_expected.txt");
  }

}
