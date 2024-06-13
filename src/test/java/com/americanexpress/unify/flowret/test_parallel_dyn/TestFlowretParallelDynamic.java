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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.lang.invoke.MethodHandles;

/*
 * @author Deepak Arora
 */
public class TestFlowretParallelDynamic {

  private static String baseDirPath = "./target/test-data-results/";
  private static Rts rts = null;
  private static String simpleClassName = MethodHandles.lookup().lookupClass().getSimpleName();

  // set to true if you want to log to disk to trouble shoot any specific test case
  private static boolean writeFiles = false;

  // set to true if you want to log to console
  private static boolean writeToConsole = false;

  @BeforeAll
  protected static void beforeAll() {
    TestManager.init(System.out, new ByteArrayOutputStream(), 10, 30000);
  }

  @BeforeEach
  protected void beforeEach() {
    TestManager.reset();
    StepResponseFactory.clear();
  }

  public static void setScenario1() {
    // nothing to do
  }

  private static void runJourney(String journey, MemoryDao dao) {
    String json = BaseUtils.getResourceAsString(TestFlowretParallelDynamic.class, "/flowret/" + journey + ".json");

    if (dao.read("flowret_journey-3.json ") == null) {
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
    MemoryDao dao = new MemoryDao();
    String methodName = new Object() {
    }.getClass().getEnclosingMethod().getName();
    String path = baseDirPath + simpleClassName + "/" + methodName + "/";
    setScenario1();
    init(dao, new TestComponentFactoryParallelSupps(), new TestHandler(), null);
    runJourney("parallel_dyn_test", dao);
    TestManager.writeFiles(writeFiles, path, dao.getDocumentMap());
    TestManager.myAssertEqualsTodo(writeToConsole, simpleClassName + "." + methodName, null);
  }

}
