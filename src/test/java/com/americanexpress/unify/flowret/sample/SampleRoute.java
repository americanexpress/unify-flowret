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

import com.americanexpress.unify.flowret.InvokableRoute;
import com.americanexpress.unify.flowret.ProcessContext;
import com.americanexpress.unify.flowret.RouteResponse;
import com.americanexpress.unify.flowret.UnitResponseType;

import java.util.ArrayList;
import java.util.List;

public class SampleRoute implements InvokableRoute {

  private String name = null;
  private ProcessContext pc = null;

  public SampleRoute(ProcessContext pc) {
    this.name = pc.getCompName();
    this.pc = pc;
  }

  public String getName() {
    return name;
  }

  public RouteResponse executeRoute() {
    String compName = pc.getCompName();
    if (compName.equals("is_part_available")) {
      List<String> branches = new ArrayList<>();
      branches.add("Yes");
      return new RouteResponse(UnitResponseType.OK_PROCEED, branches, "");
    }
    return null;
  }

}
