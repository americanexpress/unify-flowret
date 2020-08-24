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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
 * @author Deepak Arora
 */
public class TestRule implements InvokableRoute {

  private String name = null;
  private ProcessContext pc = null;

  public TestRule(ProcessContext pc) {
    this.name = pc.getCompName();
    this.pc = pc;
  }

  public String getName() {
    return name;
  }

  public RouteResponse executeRoute() {
    List<String> branches = new ArrayList<>();
    RouteResponse resp = null;
    String name = pc.getCompName();

    while (true) {
      Random random = new Random();

      while (true) {
        if (BaseUtils.compareWithMany(name, "rcomp4", "rcomp2", "rcomp5")) {
          branches.add("yes");
          resp = new RouteResponse(UnitResponseType.OK_PROCEED, branches, null);
          break;
        }

        // for parallel processing use case only
        if (name.equalsIgnoreCase("route_1")) {
          branches.add("1");
          branches.add("2");
          branches.add("3");
          resp = new RouteResponse(UnitResponseType.OK_PROCEED, branches, null);
          break;
        }

        {
          branches.add("no");
          resp = new RouteResponse(UnitResponseType.OK_PROCEED, branches, null);
          break;
        }
      }

      break;
    }

    return resp;
  }

}
