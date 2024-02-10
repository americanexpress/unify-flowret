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

package com.americanexpress.unify.flowret.sample;

import com.americanexpress.unify.flowret.InvokableStep;
import com.americanexpress.unify.flowret.ProcessContext;
import com.americanexpress.unify.flowret.StepResponse;
import com.americanexpress.unify.flowret.UnitResponseType;

public class SampleStep implements InvokableStep {

  private String name = null;
  private ProcessContext pc = null;

  public SampleStep(ProcessContext pc) {
    this.name = pc.getCompName();
    this.pc = pc;
  }

  public String getName() {
    return name;
  }

  public StepResponse executeStep() {
    String compName = pc.getCompName();

    if (compName.equals("start")) {
      return new StepResponse(UnitResponseType.OK_PROCEED, "", "");
    }

    if (compName.equals("get_part_info")) {
      return new StepResponse(UnitResponseType.OK_PROCEED, "", "");

      // try below in case you would like to return an error pend
      // in which case the step will be re-executed
      // int value = new Random().nextInt(2);
      // if (value == 0) {
      //   return new StepResponse(UnitResponseType.OK_PROCEED, "", "");
      // }
      //  else {
      //    return new StepResponse(UnitResponseType.ERROR_PEND, "", "SOME_WORK_BASKET");
      // }

      // or try below in case you would like to have only a single pend post which
      // the process moves ahead
      // return new StepResponse(UnitResponseType.OK_PEND, "", "SOME_WORK_BASKET");
    }

    if (compName.equals("get_part_inventory")) {
      return new StepResponse(UnitResponseType.OK_PROCEED, "", "");
    }

    if (compName.equals("ship_part")) {
      return new StepResponse(UnitResponseType.OK_PROCEED, "", "");
    }

    if (compName.equals("cancel_order")) {
      return new StepResponse(UnitResponseType.OK_PROCEED, "", "");
    }

    return null;
  }

}
