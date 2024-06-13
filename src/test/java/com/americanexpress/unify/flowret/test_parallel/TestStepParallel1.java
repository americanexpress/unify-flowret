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
import com.americanexpress.unify.flowret.*;

/*
 * @author Deepak Arora
 */
public class TestStepParallel1 implements InvokableStep {

  private String name = null;
  private ProcessContext pc = null;

  public TestStepParallel1(ProcessContext pc) {
    this.name = pc.getCompName();
    this.pc = pc;
  }

  public String getName() {
    return name;
  }

  public StepResponse executeStep() {
    String stepName = pc.getStepName();
    if (stepName.equals("step_wait_1") || stepName.equals("step_wait_2") || stepName.equals("step_wait_3")) {
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    TestStepResponse tsr = StepResponseFactory.getResponse(stepName);
    StepResponse sr = tsr.getStepResponse();
    long delay = tsr.getDelay();
    if (delay > 0) {
      BaseUtils.sleep(delay);
    }
    return sr;
  }

}

