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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/*
 * @author Deepak Arora
 */
public class TestFlowretReopen {

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
    RouteResponseFactory.clear();
  }

  private static void init(FlowretDao dao, ProcessComponentFactory factory, EventHandler handler, ISlaQueueManager sqm) {
    rts = Flowret.instance().getRunTimeService(dao, factory, handler, sqm);
  }

  private static void runJourney(String journey, MemoryDao dao) {
    String json = BaseUtils.getResourceAsString(TestFlowretReopen.class, "/flowret/" + journey + ".json");
    String slaJson = null;

    try {
      slaJson = BaseUtils.getResourceAsString(TestFlowretReopen.class, "/flowret/" + journey + "_sla.json");
    }
    catch (Exception e) {
      // nothing to do
    }

    ProcessContext pc = null;
    if (dao.read("flowret_process_info-1.json") == null) {
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

    // here we now reopen the case
    pc = rts.reopenCase("1", "reopen_ticket", true, "reopen_wb");

    // and now we resume the case
    rts.resumeCase("1");
  }

  @Test
  void testClean() {
    MemoryDao dao = new MemoryDao();
    String methodName = new Object() {
    }.getClass().getEnclosingMethod().getName();
    String path = baseDirPath + simpleClassName + "/" + methodName + "/";
    List<String> branches = new ArrayList<>();
    branches.add("yes");
    RouteResponseFactory.addResponse("route2", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route4", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route5", UnitResponseType.OK_PROCEED, branches, null);
    init(dao, new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_journey_reopen", dao);
    TestManager.writeFiles(writeFiles, path, dao.getDocumentMap());
    TestManager.myAssertEqualsTodo(writeToConsole, simpleClassName + "." + methodName, null);
  }

}
