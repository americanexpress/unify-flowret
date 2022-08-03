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

import com.americanexpress.unify.flowret.InvokableRoute;
import com.americanexpress.unify.flowret.ProcessContext;
import com.americanexpress.unify.flowret.RouteResponse;
import com.americanexpress.unify.flowret.UnitResponseType;

import java.util.ArrayList;
import java.util.List;

/*
 * @author Deepak Arora
 */
public class TestRuleParallelInParallel implements InvokableRoute {

  private String name = null;
  private ProcessContext pc = null;

  public TestRuleParallelInParallel(ProcessContext pc) {
    this.name = pc.getCompName();
    this.pc = pc;
  }

  public String getName() {
    return name;
  }

  public RouteResponse executeRoute() {
    List<String> branches = new ArrayList<>();
    RouteResponse resp = null;
    String stepName = pc.getStepName();

    if ((stepName.equalsIgnoreCase("route_1")
            || (stepName.equalsIgnoreCase("route_b1_1"))
            || (stepName.equalsIgnoreCase("route_b2_1"))
            || (stepName.equalsIgnoreCase("route_b3_1")))) {
      branches.add("1");
      branches.add("2");
      branches.add("3");
      resp = new RouteResponse(UnitResponseType.OK_PROCEED, branches, null);
    }

    return resp;
  }

}
