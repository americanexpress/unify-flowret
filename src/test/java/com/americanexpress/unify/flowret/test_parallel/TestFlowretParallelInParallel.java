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
import com.americanexpress.unify.jdocs.ERRORS_JDOCS;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * @author Deepak Arora
 */
public class TestFlowretParallelInParallel {

  private static String dirPath = "./target/test-data-results/";
  private static Rts rts = null;
  private static FileDao dao = null;
  private static ProcessComponentFactory factory = null;
  private static EventHandler handler = null;
  private static PrintStream previousConsole = null;
  private static ByteArrayOutputStream newConsole = null;

  private void myAssertEquals(String testCase, String resourcePath) {
    String output = newConsole.toString();
    String s = output;
    output = com.aexp.acq.unify.flowret.TestUtils.getSortedWithoutCrLf(output);
    String expected = BaseUtils.getResourceAsString(TestFlowret.class, resourcePath);
    expected = com.aexp.acq.unify.flowret.TestUtils.getSortedWithoutCrLf(expected);
    assertEquals(expected, output);
    previousConsole.println();
    previousConsole.println();
    previousConsole.println("*********************** " + testCase + " ***********************");
    previousConsole.println();
    previousConsole.println(s);
  }

  @BeforeAll
  protected static void setEnv() throws Exception {
    previousConsole = System.out;
    newConsole = new ByteArrayOutputStream();
    System.setOut(new PrintStream(newConsole));

    File directory = new File(dirPath);
    if (!directory.exists()) {
      directory.mkdir();
    }

    ERRORS_FLOWRET.load();
    Flowret.init(20, 30000, "-");
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

  public static void setScenario1() {
    // all happy path first
  }

  private static void init(FlowretDao dao, ProcessComponentFactory factory, EventHandler handler, ISlaQueueManager sqm) {
    ERRORS_JDOCS.load();
    rts = Flowret.instance().getRunTimeService(dao, factory, handler, sqm);
  }

  private static void runJourney(String journey) {
    String json = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + ".json");
    Utils.validateJourneyDefinition(json);
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
    init(new FileDao(dirPath), new TestComponentFactoryParallelInParallel(), new TestHandler(), null);
    runJourney("parallel_in_parallel");
    myAssertEquals("testScenario1", "/flowret/test_parallel_in_parallel/test_scenario_1_expected.txt");
  }

}
