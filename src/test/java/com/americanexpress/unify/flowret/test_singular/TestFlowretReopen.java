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
public class TestFlowretReopen {

  private static String dirPath = null;
  private static Rts rts = null;

  @BeforeAll
  protected static void setEnv() {
    dirPath = System.getenv("TestFlowretDirPath");
    if (dirPath == null) {
      //      System.out.println("Please specify directory path as a system property");
      //      System.out.println("Do not forget to supply the trailing /  e.g. C:/Temp/");
      //      System.exit(1);
      dirPath = "./target/test-data-results2/";
      File directory = new File(dirPath);
      if (!directory.exists()) {
        directory.mkdir();
      }
    }

    ERRORS_FLOWRET.load();
    Flowret.init(10, 30000, "-");
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
    String json = BaseUtils.getResourceAsString(TestFlowretReopen.class, "/flowret/" + journey + ".json");
    String slaJson = null;

    try {
      slaJson = BaseUtils.getResourceAsString(TestFlowretReopen.class, "/flowret/" + journey + "_sla.json");
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

    // here we now reopen the case
    pc = rts.reopenCase("1", "reopen_ticket", true, "reopen_wb");

    // and now we resume the case
    rts.resumeCase("1");
  }

  @Test
  void testClean() {
    List<String> branches = new ArrayList<>();
    branches.add("yes");
    RouteResponseFactory.addResponse("route2", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route4", UnitResponseType.OK_PROCEED, branches, null);
    RouteResponseFactory.addResponse("route5", UnitResponseType.OK_PROCEED, branches, null);

    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_journey_reopen");
  }

}
