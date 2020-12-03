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

  public synchronized static void addResponse(String stepName, UnitResponseType urt, String wb, String ticket) {
    StepResponse r = new StepResponse(urt, ticket, wb);
    Queue<StepResponse> q = actions.get(stepName);
    if (q == null) {
      q = new LinkedList<>();
      actions.put(stepName, q);
    }
    q.add(r);
  }

  public synchronized static StepResponse getResponse(String stepName) {
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

    return r;
  }

  public synchronized static void clear() {
    actions.clear();
  }

}
