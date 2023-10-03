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

import com.americanexpress.unify.base.BaseUtils;
import com.americanexpress.unify.base.ErrorTuple;
import com.americanexpress.unify.jdocs.Document;
import com.americanexpress.unify.jdocs.JDocument;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/*
 * @author Deepak Arora
 */
public class ProcessInfo {

  private final String caseId;
  private final ProcessDefinition pd;
  private ReentrantLock lock = new ReentrantLock(true);

  private boolean isComplete = false;

  // shared variables that will be updated by threads
  private Map<String, ProcessVariable> pvMap = new ConcurrentHashMap<>();

  // ticket raised
  private volatile String ticket = "";

  // whether the component that raised the ticket returned an OK_PEND or not
  private volatile UnitResponseType ticketUrt = UnitResponseType.OK_PROCEED;

  // pend exec path
  private volatile String pendExecPath = "";

  // variables which are thread specific
  private Map<String, ExecPath> execPaths = Collections.synchronizedSortedMap(new TreeMap<>());

  // this variable is populated while writing and is for trouble shooting and information only
  // this variable is not used while reading the document
  private volatile Unit lastUnitExecuted = null;

  protected volatile boolean isPendAtSameStep = false;

  private Setter setter = null;

  protected ProcessInfo(String caseId, ProcessDefinition pd) {
    this.caseId = caseId;
    this.pd = pd;
    setter = new Setter();
  }

  private void setLastUnitExecuted(Unit lastUnitExecuted) {
    this.lastUnitExecuted = lastUnitExecuted;
  }

  protected Unit getLastUnitExecuted() {
    return lastUnitExecuted;
  }

  protected UnitResponseType getTicketUrt() {
    return ticketUrt;
  }

  private void setTicketUrt(UnitResponseType ticketUrt) {
    this.ticketUrt = ticketUrt;
  }

  protected Lock getLock() {
    return lock;
  }

  protected String getCaseId() {
    return caseId;
  }

  private void setPendExecPath(String pendExecPath) {
    // go ahead if we are trying to clear
    if (pendExecPath.isEmpty() == true) {
      this.pendExecPath = pendExecPath;
    }
    else {
      // if the pend is not on "." execution path then
      // we need to set the pend exec path to the one that is deepest in the hierarchy
      // we need to do this so that the unravelling can take place correctly
      // we determine this by counting the number of "."

      // however if the pend happened in the "." execution path, then it means that we have
      // moved ahead of parallel processing in the branches (due to a ticket) and in this case
      // the pends in the previous branches does not matter

      if (pendExecPath.equals(".")) {
        this.pendExecPath = pendExecPath;
      }
      else {
        int oldDepth = BaseUtils.getCount(this.pendExecPath, '.');
        int newDepth = BaseUtils.getCount(pendExecPath, '.');

        if (newDepth > oldDepth) {
          this.pendExecPath = pendExecPath;
        }
        else {
          // do nothing
        }
      }
    }
  }

  protected String getPendExecPath() {
    return pendExecPath;
  }

  private void setTicket(String ticket) {
    if (ticket.isEmpty() == true) {
      this.ticket = ticket;
    }
    else {
      // set only if it is already empty
      if (this.ticket.isEmpty() == true) {
        this.ticket = ticket;
      }
    }
  }

  protected String getTicket() {
    return ticket;
  }

  protected void removeExecPath(String name) {
    execPaths.remove(name);
  }

  protected void setProcessVariable(ProcessVariable v) {
    ProcessVariable newPv = new ProcessVariable(v.getName(), v.getType(), v.getValue());
    pvMap.put(newPv.getName(), newPv);
  }

  protected ProcessVariables getProcessVariables() {
    return new ProcessVariables(pvMap);
  }

  protected ExecPath getExecPath(String name) {
    return execPaths.get(name);
  }

  protected List<ExecPath> getExecPaths() {
    synchronized (execPaths) {
      List<ExecPath> list = execPaths.values().stream().collect(Collectors.toList());
      return list;
    }
  }

  protected void setExecPath(ExecPath ep) {
    execPaths.put(ep.getName(), ep);
  }

  protected void clearExecPaths() {
    execPaths.clear();
  }

  protected String getPendWorkBasket() {
    return execPaths.get(pendExecPath).getPendWorkBasket();
  }

  protected ErrorTuple getPendErrorTuple() {
    return execPaths.get(pendExecPath).getPendErrorTuple();
  }

  protected boolean isCaseStarted() {
    if (execPaths.size() == 0) {
      return false;
    }
    else {
      return true;
    }
  }

  protected boolean isCaseCompleted() {
    return isComplete;
  }

  protected void setCaseCompleted() {
    this.isComplete = true;
  }

  protected void setCaseCompleted(boolean isComplete) {
    this.isComplete = isComplete;
  }

  protected void clearPendWorkBaskets() {
    synchronized (execPaths) {
      List<ExecPath> paths = new ArrayList<>(execPaths.values());
      for (ExecPath path : paths) {
        path.setPendWorkBasket("");
      }
    }
  }

  protected Document getDocument() {
    Document d = new JDocument();

    // write last executed unit details
    if (lastUnitExecuted == null) {
      d.setString("$.process_info.last_executed_step", "");
      d.setString("$.process_info.last_executed_comp_name", "");
    }
    else {
      d.setString("$.process_info.last_executed_step", lastUnitExecuted.getName());
      d.setString("$.process_info.last_executed_comp_name", lastUnitExecuted.getComponentName());
    }

    // write pend info
    d.setString("$.process_info.pend_exec_path", pendExecPath);

    // write ts
    d.setLong("$.process_info.ts", Instant.now().toEpochMilli());

    // write isComplete status
    d.setBoolean("$.process_info.is_complete", isComplete);

    // write process variables
    int i = 0;
    for (ProcessVariable var : pvMap.values()) {
      d.setString("$.process_info.process_variables[%].name", var.getName(), i + "");
      d.setString("$.process_info.process_variables[%].value", var.getValueAsString(), i + "");
      d.setString("$.process_info.process_variables[%].type", var.getType().toString().toLowerCase(), i + "");
      i++;
    }

    // write execution paths
    i = 0;
    synchronized (execPaths) {
      for (ExecPath path : execPaths.values()) {
        d.setString("$.process_info.exec_paths[%].name", path.getName(), i + "");
        d.setString("$.process_info.exec_paths[%].status", path.getStatus().toString().toLowerCase(), i + "");

        String s = path.getStep();
        d.setString("$.process_info.exec_paths[%].step", s, i + "");

        if (s.equals("end")) {
          d.setString("$.process_info.exec_paths[%].comp_name", s, i + "");
        }
        else {
          s = pd.getUnit(s).getComponentName();
          d.setString("$.process_info.exec_paths[%].comp_name", s, i + "");
        }

        s = path.getPendWorkBasket();
        d.setString("$.process_info.exec_paths[%].pend_workbasket", s, i + "");

        s = path.getTicket();
        d.setString("$.process_info.exec_paths[%].ticket", s, i + "");

        ErrorTuple et = path.getPendErrorTuple();
        d.setString("$.process_info.exec_paths[%].pend_error.code", et.getErrorCode(), i + "");
        d.setString("$.process_info.exec_paths[%].pend_error.message", et.getErrorMessage(), i + "");
        d.setString("$.process_info.exec_paths[%].pend_error.details", et.getErrorMessage(), i + "");
        d.setBoolean("$.process_info.exec_paths[%].pend_error.is_retyable", et.isRetryable(), i + "");

        s = path.getPrevPendWorkBasket();
        d.setString("$.process_info.exec_paths[%].prev_pend_workbasket", s, i + "");

        s = path.getTbcSlaWorkBasket();
        d.setString("$.process_info.exec_paths[%].tbc_sla_workbasket", s, i + "");

        if (path.getUnitResponseType() != null) {
          d.setString("$.process_info.exec_paths[%].unit_response_type", path.getUnitResponseType().toString().toLowerCase(), i + "");
        }
        i++;
      }
    }

    // write ticket info
    if (ticket != null) {
      d.setString("$.process_info.ticket", ticket);
    }

    return d;
  }

  public Setter getSetter() {
    return this.setter;
  }

  protected class Setter {

    public Setter setTicket(String ticket) {
      ProcessInfo.this.setTicket(ticket);
      if (ticket.isEmpty() == true) {
        ProcessInfo.this.setTicketUrt(UnitResponseType.OK_PROCEED);
      }
      return this;
    }

    public Setter setTicketUrt(UnitResponseType ticketUrt) {
      ProcessInfo.this.setTicketUrt(ticketUrt);
      return this;
    }

    public Setter setPendExecPath(String pendExecPath) {
      ProcessInfo.this.setPendExecPath(pendExecPath);
      return this;
    }

    public Setter setLastUnitExecuted(Unit unit) {
      ProcessInfo.this.setLastUnitExecuted(unit);
      return this;
    }

  }

}
