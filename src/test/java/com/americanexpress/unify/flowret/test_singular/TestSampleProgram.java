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
import com.americanexpress.unify.flowret.*;

import java.io.File;

/*
 * @author Deepak Arora
 */
public class TestSampleProgram {

  private static String dirPath = "./target/test-data-results/";
  private static Rts rts = null;

  public static void main(String[] args) {
    File directory = new File(dirPath);
    if (!directory.exists()) {
      directory.mkdir();
    }
    TestManager.deleteFiles(dirPath);

    ERRORS_FLOWRET.load();
    Flowret.init(10, 30000, "-");
    StepResponseFactory.clear();

    // foo1("test_journey_wms");
    // foo2("test_journey_wms");
    foo3("test_journey_1");

    Flowret.instance().close();
  }

  private static void foo1(String journey) {
    setScenario1();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourneyWithWms1(journey);
  }

  private static void foo2(String journey) {
    setScenario2();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourneyWithWms2(journey);
  }

  private static void foo3(String journey) {
    setScenario3();
    init(new FileDao(dirPath), new TestComponentFactory(), new TestHandler(), new TestSlaQueueManager());
    runJourneyWithoutWms(journey);
  }

  private static void runJourneyWithWms1(String journey) {
    String json = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + ".json");
    String slaJson = null;

    try {
      slaJson = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + "_sla.json");
    }
    catch (Exception e) {
      // nothing to do
    }

    if (new File(dirPath + "flowret_process_info-1.json").exists() == false) {
      rts.startCase("1", json, null, slaJson);
    }

    Wms wms = Flowret.instance().getWorkManagementService(new FileDao(dirPath), new TestWorkManager(), new TestSlaQueueManager());
    wms.changeWorkBasket("1", "wb_2");

    rts.resumeCase("1");
  }

  private static void runJourneyWithWms2(String journey) {
    String json = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + ".json");
    String slaJson = null;

    try {
      slaJson = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + "_sla.json");
    }
    catch (Exception e) {
      // nothing to do
    }

    if (new File(dirPath + "flowret_process_info-1.json").exists() == false) {
      rts.startCase("1", json, null, slaJson);
    }

    Wms wms = Flowret.instance().getWorkManagementService(new FileDao(dirPath), new TestWorkManager(), new TestSlaQueueManager());
    wms.changeWorkBasket("1", "wb_2");
    wms.changeWorkBasket("1", "wb_3");
    wms.changeWorkBasket("1", "wb_4");
    wms.changeWorkBasket("1", "wb_5");

    rts.resumeCase("1");
  }

  private static void runJourneyWithoutWms(String journey) {
    String json = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + ".json");
    String slaJson = null;

    try {
      slaJson = BaseUtils.getResourceAsString(TestFlowret.class, "/flowret/" + journey + "_sla.json");
    }
    catch (Exception e) {
      // nothing to do
    }

    if (new File(dirPath + "flowret_process_info-1.json").exists() == false) {
      rts.startCase("1", json, null, slaJson);
    }

  }

  private static void init(FlowretDao dao, ProcessComponentFactory factory, EventHandler handler, ISlaQueueManager sqm) {
    rts = Flowret.instance().getRunTimeService(dao, factory, handler, sqm);
  }

  private static void setScenario1() {
    StepResponseFactory.addResponse("step3", UnitResponseType.OK_PEND_EOR, "wb_1", "");
    StepResponseFactory.addResponse("step3", UnitResponseType.OK_PEND_EOR, "wb_1", "");
  }

  private static void setScenario2() {
    StepResponseFactory.addResponse("step3", UnitResponseType.OK_PEND_EOR, "wb_1", "");
  }

  public static void setScenario3() {
    StepResponseFactory.addResponse("step3", UnitResponseType.OK_PEND_EOR, "wb_1", "");
  }

}
