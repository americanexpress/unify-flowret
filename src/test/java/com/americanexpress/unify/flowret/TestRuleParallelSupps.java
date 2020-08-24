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

/*
 * @author Deepak Arora
 */
public class TestRuleParallelSupps implements InvokableRoute {

  private String name = null;
  private ProcessContext pc = null;

  public TestRuleParallelSupps(ProcessContext pc) {
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
    ProcessVariables pvs = pc.getProcessVariables();
    String execPathPvName = "supp_exec_path_name";
    String processSuppsPvName = "process_supps";

    while (true) {
      if (BaseUtils.compareWithMany(name, "route_0")) {
        Boolean processSupps = pvs.getBoolean(processSuppsPvName);
        if (processSupps == null) {
          pvs.setValue(processSuppsPvName, ProcessVariableType.BOOLEAN, true);
          branches.add("yes");
        }
        else {
          pvs.setValue(processSuppsPvName, ProcessVariableType.BOOLEAN, false);
          branches.add("no");
        }

        break;
      }

      if (BaseUtils.compareWithMany(name, "route_1_c")) {
        pvs.setValue(execPathPvName, ProcessVariableType.STRING, pc.getExecPathName());

        // simulate 5 supps
        branches.add("ai_index_1");
        branches.add("ai_index_2");
        branches.add("ai_index_3");
        branches.add("ai_index_4");
        branches.add("ai_index_5");

        break;
      }

      break;
    }

    resp = new RouteResponse(UnitResponseType.OK_PROCEED, branches, null);

    return resp;
  }

}
