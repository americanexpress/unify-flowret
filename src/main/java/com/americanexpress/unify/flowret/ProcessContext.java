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

import com.americanexpress.unify.jdocs.ErrorTuple;

/*
 * @author Deepak Arora
 */
public class ProcessContext {

  private String journeyName = null;
  private String caseId = null;
  private String stepName = null;
  private String compName = null;
  private String userData = null;
  private UnitType compType = null;
  private ProcessVariables processVariables = null;
  private String execPathName = null;
  private String pendWorkBasket = null;
  private ErrorTuple pendErrorTuple = new ErrorTuple(); // only valid for pend event
  private boolean isPendAtSameStep = false;

  public ProcessContext(String journeyName, String caseId, String stepName, String compName, String userData, UnitType compType, ProcessVariables processVariables, String execPathName) {
    this.journeyName = journeyName;
    this.caseId = caseId;
    this.stepName = stepName;
    this.compName = compName;
    this.userData = userData;
    this.compType = compType;
    if (processVariables != null) {
      this.processVariables = processVariables;
    }
    this.execPathName = execPathName;
  }

  private ProcessContext() {
  }

  public boolean isPendAtSameStep() {
    return isPendAtSameStep;
  }

  public String getPendWorkBasket() {
    return pendWorkBasket;
  }

  public String getExecPathName() {
    return execPathName;
  }

  public String getJourneyName() {
    return journeyName;
  }

  public String getCaseId() {
    return caseId;
  }

  public String getStepName() {
    return stepName;
  }

  public String getCompName() {
    return compName;
  }

  public UnitType getCompType() {
    return compType;
  }

  public ErrorTuple getPendErrorTuple() {
    return pendErrorTuple;
  }

  public String getUserData() {
    return userData;
  }

  public ProcessVariables getProcessVariables() {
    return processVariables;
  }

  public static ProcessContext forEvent(EventType eventType, Rts rts, String epName) {
    ProcessContext pc = new ProcessContext();
    ProcessDefinition pd = rts.pd;
    ProcessInfo pi = rts.pi;

    pc.journeyName = pd.getName();
    pc.caseId = pi.getCaseId();
    pc.execPathName = epName;
    pc.processVariables = pi.getProcessVariables();
    pc.compName = "";
    pc.isPendAtSameStep = pi.isPendAtSameStep;

    switch (eventType) {
      case ON_PERSIST:
      case ON_PROCESS_COMPLETE:
      case ON_PROCESS_START:
        break;

      case ON_PROCESS_PEND:
        pc.stepName = pi.getExecPath(pi.getPendExecPath()).getStep();
        pc.compName = pd.getUnit(pc.stepName).getComponentName();
        pc.userData = pd.getUnit(pc.stepName).getUserData();
        pc.compType = pd.getUnit(pc.stepName).getType();
        pc.pendWorkBasket = pi.getPendWorkBasket();
        pc.pendErrorTuple = pi.getPendErrorTuple();
        break;

      case ON_PROCESS_RESUME:
        pc.stepName = pi.getExecPath(pi.getPendExecPath()).getStep();
        pc.compName = pd.getUnit(pc.stepName).getComponentName();
        pc.pendWorkBasket = pi.getPendWorkBasket();
        break;

      case ON_TICKET_RAISED:
        pc.stepName = pi.getExecPath(epName).getStep();
        pc.compName = pd.getUnit(pc.stepName).getComponentName();
        pc.userData = pd.getUnit(pc.stepName).getUserData();
        pc.compType = pd.getUnit(pc.stepName).getType();
        break;
    }

    return pc;
  }

  public static ProcessContext forWms(ProcessDefinition pd, ProcessInfo pi) {
    ProcessContext pc = new ProcessContext();
    pc.journeyName = pd.getName();
    pc.caseId = pi.getCaseId();
    pc.execPathName = pi.getPendExecPath();
    pc.processVariables = pi.getProcessVariables();
    pc.isPendAtSameStep = pi.isPendAtSameStep;
    pc.stepName = pi.getExecPath(pi.getPendExecPath()).getStep();
    pc.compName = pd.getUnit(pc.stepName).getComponentName();
    pc.userData = pd.getUnit(pc.stepName).getUserData();
    pc.compType = pd.getUnit(pc.stepName).getType();
    pc.pendWorkBasket = pi.getPendWorkBasket();
    pc.pendErrorTuple = pi.getPendErrorTuple();
    return pc;
  }

}
