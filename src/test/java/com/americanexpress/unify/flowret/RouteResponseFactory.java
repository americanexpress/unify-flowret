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

import java.util.*;

/*
 * @author Deepak Arora
 */
public class RouteResponseFactory {

  private static Map<String, Queue<RouteResponse>> actions = new HashMap<>();

  public synchronized static void addResponse(String stepName, UnitResponseType urt, List<String> branches, String wb) {
    RouteResponse r = new RouteResponse(urt, branches, wb);
    Queue<RouteResponse> q = actions.get(stepName);
    if (q == null) {
      q = new LinkedList<>();
      actions.put(stepName, q);
    }
    q.add(r);
  }

  public synchronized static RouteResponse getResponse(String stepName) {
    List<String> branches = new ArrayList<>();
    branches.add("no");
    RouteResponse r = new RouteResponse(UnitResponseType.OK_PROCEED, branches, "");

    Queue<RouteResponse> q = actions.get(stepName);
    if (q != null) {
      if (q.size() > 0) {
        r = q.remove();
      }
    }

    return r;
  }

  public synchronized static void clear() {
    actions.clear();
  }

}
