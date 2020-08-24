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

package com.americanexpress.unify.flowret;

import com.americanexpress.unify.jdocs.BaseUtils;
import com.americanexpress.unify.jdocs.UnifyException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

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
      System.out.println("Please specify directory path as a system property");
      System.out.println("Do not forget to supply the trailing /  e.g. C:/Temp/");
      System.exit(1);
    }

    ERRORS_FLOWRET.load();
    Flowret.init(10, 30000, "-");
  }

  @AfterAll
  protected static void close() {
    Flowret.instance().close();
  }

  private static void init(FlowretDao dao, ProcessComponentFactory factory, EventHandler handler, ISlaQueueManager sqm) {
    rts = Flowret.instance().getRunTimeService(dao, factory, handler, sqm);
  }

  private static void runJourney(String journey) {
    try {
      String json = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + ".json");
      String slaJson = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + "_sla.json");

      ProcessContext pc = null;
      if (new File(dirPath + "flowret_journey-1.json").exists() == false) {
        pc = rts.startCase("1", json, null, slaJson);
      }
      else {
        pc = rts.resumeCase("1");
      }

      while (pc != null) {
        System.out.println();
        pc = rts.resumeCase("1");
      }
    }
    catch (UnifyException e) {
      System.out.println("Exception -> " + e.getMessage());
    }
  }

  @Test
  protected void testJourney() {
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_journey");
  }

  @Test
  protected void testSla() {
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_journey");
  }

  @Test
  protected void testPersist() {
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourney("test_persist");
  }

}
