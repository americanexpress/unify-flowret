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

import java.util.HashMap;
import java.util.Map;

/*
 * @author Deepak Arora
 */
public class TestStep implements InvokableStep {

  private String name = null;
  private ProcessContext pc = null;
  private static Map<String, TestData[]> responses = new HashMap<>();
  private static Map<String, Integer> counter = new HashMap<>();

  static {
    setup();
  }

  private int getAndIncr(String comp) {
    int i = counter.get(comp);
    counter.put(comp, i + 1);
    return i;
  }

  public TestStep(ProcessContext pc) {
    this.name = pc.getCompName();
    this.pc = pc;
  }

  public String getName() {
    return name;
  }

  public StepResponse executeStep() {
    StepResponse response = null;

    while (true) {
      if (name.equalsIgnoreCase("comp3")) {
        response = new StepResponse(UnitResponseType.OK_PEND, null, "comp3_wb");
        break;
      }

      if (name.equalsIgnoreCase("comp11")) {
        TestData td = responses.get("comp11")[getAndIncr("comp11")];
        response = new StepResponse(td.urt, null, td.wb);
        break;
      }

      if (name.equalsIgnoreCase("comp16")) {
        int temp = RandomGen.get(1, 4);
        if (temp <= 0) {
          response = new StepResponse(UnitResponseType.OK_PROCEED, "final_step", null);
        }
        else {
          response = new StepResponse(UnitResponseType.OK_PROCEED, null, null);
        }
        break;
      }


      //      if (name.equalsIgnoreCase("comp30")) {
      //        int temp = RandomGen.get(1, 4);
      //        if (temp <= 3) {
      //          response = new StepResponse(UnitResponseType.ERROR_PEND, null, "err_wb");
      //        }
      //        else {
      //          response = new StepResponse(UnitResponseType.OK_PROCEED, null, null);
      //        }
      //        break;
      //      }

      response = new StepResponse(UnitResponseType.OK_PROCEED, null, null);
      break;
    }
    return response;
  }

  private static void setup() {
    TestData[] td = new TestData[4];
    td[0] = new TestData(UnitResponseType.OK_PEND_EOR, "comp11_wb");
    td[1] = new TestData(UnitResponseType.ERROR_PEND, "comp11_err1");
    td[2] = new TestData(UnitResponseType.ERROR_PEND, "comp11_err2");
    td[3] = new TestData(UnitResponseType.OK_PROCEED, "");

    responses.put("comp11", td);
    counter.put("comp11", 0);
  }

}

class TestData {

  protected UnitResponseType urt = null;
  protected String wb = "";

  protected TestData(UnitResponseType urt, String wb) {
    this.urt = urt;
    this.wb = wb;
  }

}
