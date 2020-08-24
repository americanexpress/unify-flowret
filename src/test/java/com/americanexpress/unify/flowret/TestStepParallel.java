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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * @author Deepak Arora
 */
public class TestStepParallel implements InvokableStep {

  private String name = null;
  private ProcessContext pc = null;
  private static Map<String, String> responses = new ConcurrentHashMap<>();

  public TestStepParallel(ProcessContext pc) {
    this.name = pc.getCompName();
    this.pc = pc;
  }

  public String getName() {
    return name;
  }

  public StepResponse executeStep() {
    String comp = pc.getCompName();
    StepResponse response = null;

    response = testClean();
    //    response = testErrorPendNoTickets();
    //    response = testOKPendNoTickets();
    //    response = testOKProceedWithTicket();
    //    response = testErrorPendAndTicket();
    //    response = testErrorPendAndTicket1();

    return response;
  }

  private StepResponse testClean() {
    return get(100, UnitResponseType.OK_PROCEED, null);
  }

  private StepResponse testErrorPendNoTickets() {
    String comp = pc.getCompName();
    StepResponse response = null;
    String[] pendComps = new String[] {"step_3", "step_5", "step_7"};

    if (BaseUtils.compareWithMany(comp, pendComps)) {
      response = get(100, UnitResponseType.ERROR_PEND, UnitResponseType.OK_PROCEED);
    }
    else {
      response = get(100, UnitResponseType.OK_PROCEED, null);
    }

    return response;
  }

  private StepResponse testRuntimeException() {
    throw new RuntimeException("Exception");
  }

  private StepResponse testOKProceedWithTicket() {
    String comp = pc.getCompName();
    StepResponse response = null;

    if (comp.equalsIgnoreCase("step_4")) {
      response = get(75, "reset", UnitResponseType.OK_PROCEED, UnitResponseType.OK_PROCEED);
    }
    else {
      response = get(100, UnitResponseType.OK_PROCEED, null);
    }

    return response;
  }

  private StepResponse testOKPendNoTickets() {
    String comp = pc.getCompName();
    StepResponse response = null;

    if ((comp.equalsIgnoreCase("step_4")) || (comp.equalsIgnoreCase("step_3a"))) {
      response = get(75, UnitResponseType.OK_PEND, UnitResponseType.OK_PROCEED);
    }
    else {
      response = get(100, UnitResponseType.OK_PROCEED, null);
    }

    return response;
  }

  private StepResponse testErrorPendAndTicket() {
    String comp = pc.getCompName();
    StepResponse response = null;

    // pend at step 2
    if (comp.equalsIgnoreCase("step_2")) {
      if (responses.get(comp) == null) {
        response = get(100, UnitResponseType.ERROR_PEND, null);
        responses.put(comp, comp);
      }
      else {
        response = get(100, UnitResponseType.OK_PROCEED, null);
      }
    }

    // raise a ticket at step 3
    if (comp.equalsIgnoreCase("step_3")) {
      if (responses.get(comp) == null) {
        response = get(100, "reset", UnitResponseType.OK_PROCEED, null);
        responses.put(comp, comp);
      }
      else {
        response = get(100, UnitResponseType.OK_PROCEED, null);
      }

    }

    if (response == null) {
      response = get(100, UnitResponseType.OK_PROCEED, null);
    }

    return response;
  }

  private StepResponse testErrorPendAndTicket1() {
    String comp = pc.getCompName();
    StepResponse response = null;

    // pend at step 2
    if (comp.equalsIgnoreCase("step_2")) {
      if (responses.get(comp) == null) {
        response = get(100, UnitResponseType.ERROR_PEND, null);
        responses.put(comp, comp);
      }
      else {
        response = get(100, UnitResponseType.OK_PROCEED, null);
      }
    }

    // raise a ticket at step 5
    if (comp.equalsIgnoreCase("step_5")) {
      if (responses.get(comp) == null) {
        response = get(100, "reset", UnitResponseType.OK_PROCEED, null);
        responses.put(comp, comp);
      }
      else {
        response = get(100, UnitResponseType.OK_PROCEED, null);
      }
    }

    if (response == null) {
      response = get(100, UnitResponseType.OK_PROCEED, null);
    }

    return response;
  }

  private StepResponse get(int percent, UnitResponseType first, UnitResponseType second) {
    int num = RandomGen.get(1, 100);
    if (num <= percent) {
      if (first == UnitResponseType.OK_PROCEED) {
        return new StepResponse(first, null, null);
      }
      else {
        return new StepResponse(first, null, "some_wb");
      }
    }
    else {
      if (second == UnitResponseType.OK_PROCEED) {
        return new StepResponse(second, null, null);
      }
      else {
        return new StepResponse(second, null, "some_wb");
      }
    }
  }

  private StepResponse get(int percent, String ticket, UnitResponseType first, UnitResponseType second) {
    int num = RandomGen.get(1, 100);
    if (num <= percent) {
      if (first == UnitResponseType.OK_PROCEED) {
        return new StepResponse(first, ticket, null);
      }
      else {
        return new StepResponse(first, null, "some_wb");
      }

    }
    else {
      if (second == UnitResponseType.OK_PROCEED) {
        return new StepResponse(second, null, null);
      }
      else {
        return new StepResponse(second, null, "some_wb");
      }
    }
  }

}
