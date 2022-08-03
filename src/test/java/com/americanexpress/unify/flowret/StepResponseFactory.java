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
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/*
 * @author Deepak Arora
 */
public class StepResponseFactory {

  private static Map<String, Queue<StepResponse>> actions = new HashMap<>();
  private static Map<String, Queue<Long>> delays = new HashMap<>();

  public synchronized static void addResponse(String stepName, UnitResponseType urt, String wb, String ticket) {
    add(stepName, urt, wb, ticket, 0);
  }

  public synchronized static void addResponse(String stepName, UnitResponseType urt, String wb, String ticket, long delayInMs) {
    add(stepName, urt, wb, ticket, delayInMs);
  }

  private static void add(String stepName, UnitResponseType urt, String wb, String ticket, long delayInMs) {
    StepResponse r = new StepResponse(urt, ticket, wb);
    Queue<StepResponse> q = actions.get(stepName);
    if (q == null) {
      q = new LinkedList<>();
      actions.put(stepName, q);
    }
    q.add(r);

    // put the delay
    Queue<Long> q1 = delays.get(stepName);
    if (q1 == null) {
      q1 = new LinkedList<>();
      delays.put(stepName, q1);
    }
    q1.add(delayInMs);
  }

  public synchronized static TestStepResponse getResponse(String stepName) {
    StepResponse r = new StepResponse(UnitResponseType.OK_PROCEED, "", "");
    Queue<StepResponse> q = actions.get(stepName);
    if (q != null) {
      if (q.size() > 0) {
        r = q.remove();
      }
    }

    // for setting a break point only
    if (stepName.equals("step16")) {
      int i = 0;
    }

    long delay = 0;
    Queue<Long> q1 = delays.get(stepName);
    if (q1 != null) {
      if (q1.size() > 0) {
        delay = q1.remove();
      }
    }

    return new TestStepResponse(r, delay);
  }

  public synchronized static void clear() {
    actions.clear();
    delays.clear();
  }

}
