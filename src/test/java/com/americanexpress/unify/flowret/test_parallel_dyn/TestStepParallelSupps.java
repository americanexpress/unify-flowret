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

import com.americanexpress.unify.flowret.*;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * @author Deepak Arora
 */
public class TestStepParallelSupps implements InvokableStep {

  private String name = null;
  private ProcessContext pc = null;
  private static Map<String, String> responses = new ConcurrentHashMap<>();

  public TestStepParallelSupps(ProcessContext pc) {
    this.name = pc.getCompName();
    this.pc = pc;
  }

  public String getName() {
    return name;
  }

  public StepResponse executeStep() {
    StepResponse response = null;

    String s = MessageFormat.format("Step -> {0}, execution path -> {1}", pc.getStepName(), pc.getExecPathName());
    System.out.println(s);

    response = get(100, UnitResponseType.OK_PROCEED, null);
    try {
      Thread.sleep(1000);
    }
    catch (InterruptedException e) {
    }

    System.out.println("Exiting " + s);

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

}
