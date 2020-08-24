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
import com.americanexpress.unify.jdocs.ErrorTuple;

/*
 * @author Deepak Arora
 */
public class ExecPath {

  private String name = ".";

  // this contains the status of the execution path
  // this status will set to completed when
  // a) the process ends
  // b) when the execution path successfully reaches a join
  // c) when a parent execution path waiting at a parallel route successfully joins on the child Java threads
  //    this means that if a parent thread started three child threads, two of them went on to the join condition
  //    but the third pended somewhere, the parent execution path will still be marked complete
  // Reason is that when any child thread pends, the application pends and when it is resumed
  // we start with a single thread which first executes in the context of the pended child execution path
  // and then upon reaching the join assumes the role of the parent execution path. When it assumes the role
  // of the parent, the status of the parent execution path (which was marked completed previously)
  // is set to started to proceed further in the process

  private ExecPathStatus status = ExecPathStatus.STARTED;

  // this contains the start step when we start / resume the process
  // during the process this contains the last executed step
  private String step = "";

  // this contains the name of the workbasket in case a pend has occurred
  private String pendWorkBasket = "";

  // this contains the name of the previous workbasket in case of a pend
  private String prevPendWorkBasket = "";

  // To Be Cleared sla work basket. This will contain the name of the work basket for which SLA milestones
  // are to be cleared in case we receive an ok_pend_eor response
  private String tbcSlaWorkBasket = "";

  // this contains the response type return from the last step or route executed by this execution path
  private UnitResponseType unitResponseType = null;

  private ErrorTuple pendErrorTuple = new ErrorTuple();

  protected ExecPath(String name) {
    this.name = name;
  }

  protected void set(ExecPathStatus status, String step, String pendStep, UnitResponseType unitResponseType) {
    this.status = status;
    this.step = step;
    this.unitResponseType = unitResponseType;
  }

  public String getPrevPendWorkBasket() {
    return prevPendWorkBasket;
  }

  protected String getTbcSlaWorkBasket() {
    return tbcSlaWorkBasket;
  }

  protected void setTbcSlaWorkBasket(String tbcSlaWorkBasket) {
    this.tbcSlaWorkBasket = tbcSlaWorkBasket;
  }

  protected String getPendWorkBasket() {
    return pendWorkBasket;
  }

  protected void setPendErrorTuple(ErrorTuple pendErrorTuple) {
    this.pendErrorTuple = pendErrorTuple;
  }

  protected void setPrevPendWorkBasket(String prevPendWorkBasket) {
    this.prevPendWorkBasket = prevPendWorkBasket;
  }

  protected void setPendWorkBasket(String pendWorkBasket) {
    prevPendWorkBasket = this.pendWorkBasket;
    this.pendWorkBasket = pendWorkBasket;
  }

  protected boolean isSibling(ExecPath ep) {
    int i = BaseUtils.getCount(name, '.');
    int j = BaseUtils.getCount(ep.getName(), '.');
    if (i == j) {
      return true;
    }
    else {
      return false;
    }
  }

  protected String getParentExecPathName() {
    int index = BaseUtils.getIndexOfChar(name, '.', 3, false);
    return name.substring(0, index + 1);
  }

  protected UnitResponseType getUnitResponseType() {
    return unitResponseType;
  }

  protected void setStep(String step) {
    this.step = step;
  }

  protected String getStep() {
    return step;
  }

  protected void setStatus(ExecPathStatus status) {
    this.status = status;
  }

  protected String getName() {
    return name;
  }

  protected ExecPathStatus getStatus() {
    return status;
  }

  protected ErrorTuple getPendErrorTuple() {
    return pendErrorTuple;
  }

}
