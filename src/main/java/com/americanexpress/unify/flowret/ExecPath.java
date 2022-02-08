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

  // the status is started if the thread corresponding to this exec path is running
  // if the thread terminates then the status will be marked as complete
  private ExecPathStatus status = ExecPathStatus.STARTED;

  // this contains the start step when we start / resume the process
  // during the process this contains the last executed step
  private String step = "";

  // this contains the name of the ticket raised by this execpath
  private String ticket = "";

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

  protected void set(ExecPathStatus status, String step, UnitResponseType unitResponseType) {
    this.status = status;
    this.step = step;
    this.unitResponseType = unitResponseType;
  }

  protected void set(String step, UnitResponseType unitResponseType) {
    this.step = step;
    this.unitResponseType = unitResponseType;
  }

  public String getPrevPendWorkBasket() {
    return prevPendWorkBasket;
  }

  protected void setTicket(String ticket) {
    this.ticket = ticket;
  }

  protected String getTicket() {
    return ticket;
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
    String ppn = ".";

    // get number of dots in the execpath
    int num = BaseUtils.getCount(name, '.');

    int num1 = num - 2;
    if (num1 > 0) {
      int index = BaseUtils.getIndexOfChar(name, '.', num1, true);
      ppn = name.substring(0, index + 1);
    }

    return ppn;
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

  protected void setUnitResponseType(UnitResponseType unitResponseType) {
    this.unitResponseType = unitResponseType;
  }

}
