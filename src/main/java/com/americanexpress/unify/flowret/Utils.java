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

import com.americanexpress.unify.jdocs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @author Deepak Arora
 */
public class Utils {

  private static Logger logger = LoggerFactory.getLogger(Utils.class);

  protected static ProcessInfo getProcessInfo(FlowretDao dao, String caseId, ProcessDefinition pd) {
    ProcessInfo pi = new ProcessInfo(caseId, pd);

    Document d = dao.read(CONSTS_FLOWRET.DAO.PROCESS_INFO + CONSTS_FLOWRET.DAO.SEP + caseId);
    if (d == null) {
      // set process variables from process definition
      for (ProcessVariable pv : pd.getProcessVariables()) {
        pi.setProcessVariable(pv);
      }
    }
    else {
      sanitize(d, caseId, pd);
      getProcessVariablesFromProcessInfo(pi, d);
      getExecPaths(pi, d);
      getTicketInfo(pi, d);

      // set the pend info
      String s = d.getString("$.process_info.pend_exec_path");
      if (s != null) {
        pi.getSetter().setPendExecPath(s);
      }

      // set is complete
      Boolean isComplete = d.getBoolean("$.process_info.is_complete");
      if (isComplete != null) {
        if (isComplete == true) {
          pi.setCaseCompleted();
        }
      }
    }

    return pi;
  }

  private static void setIsComplete(Document pid) {
    Boolean isComplete = pid.getBoolean("$.process_info.is_complete");
    boolean b = true;
    if (isComplete == null) {
      int size = pid.getArraySize("$.process_info.exec_paths[]");
      for (int i = 0; i < size; i++) {
        String status = pid.getString("$.process_info.exec_paths[%].status", i + "");
        String wb = pid.getString("$.process_info.exec_paths[%].pend_workbasket", i + "");

        if (status.equals("started")) {
          b = false;
          break;
        }

        if (wb.isEmpty() == false) {
          b = false;
          break;
        }
      }

      isComplete = b;
      pid.setBoolean("$.process_info.is_complete", isComplete);
    }
  }

  private static String getShortestExecPath(Document pid) {
    String sep = null;
    int snum = 0;
    int size = pid.getArraySize("$.process_info.exec_paths[]");
    for (int i = 0; i < size; i++) {
      String ep = pid.getString("$.process_info.exec_paths[%].name", i + "");
      if (sep == null) {
        sep = ep;
        snum = BaseUtils.getCount(ep, '.');
      }
      else {
        int num = BaseUtils.getCount(ep, '.');
        if (num < snum) {
          snum = num;
          sep = ep;
        }
      }
    }
    return sep;
  }

  private static boolean checkAndSetTicketInExecPath(Document pid) {
    boolean isTicketRaised = false;

    String ticket = pid.getString("$.process_info.ticket");
    if ((ticket != null) && (ticket.isEmpty() == false)) {
      // a ticket was set and the step pended
      String epName = pid.getString("$.process_info.pend_exec_path");
      if (epName.isEmpty() == true) {
        // arbitrarily select the shortest exec path just so that we can resume
        epName = getShortestExecPath(pid);
        pid.setString("$.process_info.pend_exec_path", epName);
        pid.setString("$.process_info.exec_paths[name=%].pend_workbasket", "flowret_temp_hold");
        pid.setString("$.process_info.exec_paths[name=%].unit_response_type", UnitResponseType.OK_PEND.toString().toLowerCase(), epName);
      }

      // set ticket as blank in all exec paths
      int size = pid.getArraySize("$.process_info.exec_paths[]");
      for (int i = 0; i < size; i++) {
        pid.setString("$.process_info.exec_paths[%].ticket", "", i + "");
      }

      // set the ticket field in pended exec path
      String s = pid.getString("$.process_info.exec_paths[name=%].name", epName);
      if (s != null) {
        pid.setString("$.process_info.exec_paths[name=%].ticket", ticket, epName);
      }
      isTicketRaised = true;
    }
    return isTicketRaised;
  }

  private static void checkExecPathCompletion(Document pid, String caseId, ProcessDefinition pd) {
    int size = pid.getArraySize("$.process_info.exec_paths[]");
    int oldLevel = 0;

    for (int i = 0; i < size; i++) {
      // get status
      String epName = pid.getString("$.process_info.exec_paths[%].name", i + "");
      ExecPathStatus epStatus = ExecPathStatus.valueOf(pid.getString("$.process_info.exec_paths[%].status", i + "").toUpperCase());

      if (epStatus == ExecPathStatus.STARTED) {
        // we have an exec path that could not go to completion. Set status and wb
        pid.setString("$.process_info.exec_paths[%].status", ExecPathStatus.COMPLETED.toString().toLowerCase(), i + "");
        String wb = pid.getString("$.process_info.exec_paths[%].pend_workbasket", i + "");
        if (wb == null) {
          pid.setString("$.process_info.exec_paths[%].pend_workbasket", "flowret_temp_hold", i + "");
        }

        String urt = pid.getString("$.process_info.exec_paths[%].unit_response_type", i + "");
        if (urt == null) {
          // urt is null and so we could not start on this unit. Set urt to ok_pend_eor so that we can execute again
          pid.setString("$.process_info.exec_paths[%].unit_response_type", UnitResponseType.OK_PEND_EOR.toString().toLowerCase(), i + "");
          logger.info("Case id -> {}, exec path -> {}, found urt as null, replacing with ok_pend_eor", caseId, epName);
        }
        else {
          // urt has a value
          Unit unit = pd.getUnit(pid.getString("$.process_info.exec_paths[%].step", i + ""));
          if ((unit.getType() == UnitType.P_ROUTE) || (unit.getType() == UnitType.P_ROUTE_DYNAMIC)) {
            // urt can be ok_proceed or error_pend. In case of error pend we can let it remain as it is and it will
            // be picked up automatically
            // in case of ok_proceed, we do nothing. This will not cover the scenario that the child processes all reached join and completed
            // but before the parent thread could join on child threads the crash happened. Since practically
            // it is not possible to take care of every situation, we will live with this risk hoping that one of the child
            // process would not have completed in which case we should be OK
          }
          else if (unit.getType() == UnitType.S_ROUTE) {
            // urt can be ok_proceed or error_pend. If ok_proceed we need to replace with ok_pend_eor
            // as we need the rule to evaluate once again to decide where to go. For error pend we can
            // leave it as it is
            if (urt.equals(UnitResponseType.OK_PROCEED.toString().toLowerCase()) == true) {
              pid.setString("$.process_info.exec_paths[%].unit_response_type", UnitResponseType.OK_PEND_EOR.toString().toLowerCase(), i + "");
            }
            else {
              // nothing to do
            }
          }
          else {
            // we are at a step
            // urt can be ok_proceed, ok_pend_eor, ok_pend or error_pend
            // replace ok_proceed with ok_pend. Rest can ramin as they are
            if (urt.equals(UnitResponseType.OK_PROCEED.toString().toLowerCase()) == true) {
              pid.setString("$.process_info.exec_paths[%].unit_response_type", UnitResponseType.OK_PEND.toString().toLowerCase(), i + "");
              logger.info("Case id -> {}, exec path -> {}, found step with urt as ok_proceed, replacing with ok_pend", caseId, epName);
            }
            else {
              // nothing to do
            }
          }
        }
      }
    }
  }

  private static void setPendExecPath(Document pid, String caseId) {
    boolean isComplete = pid.getBoolean("$.process_info.is_complete");
    if (isComplete == false) {
      String pendExecPath = pid.getString("$.process_info.pend_exec_path");
      if (pendExecPath == null) {
        pendExecPath = "";
      }

      if (pendExecPath.isEmpty() == true) {
        int size = pid.getArraySize("$.process_info.exec_paths[]");
        int oldLevel = 0;
        for (int i = 0; i < size; i++) {
          // pend to the deepest exec path
          String epName = pid.getString("$.process_info.exec_paths[%].name", i + "");
          String wb = pid.getString("$.process_info.exec_paths[%].pend_workbasket", i + "");

          if ((wb != null) && (wb.isEmpty() == false)) {
            int newLevel = BaseUtils.getCount(epName, '.');
            if (newLevel > oldLevel) {
              pendExecPath = epName;
              oldLevel = newLevel;
            }
          }
        }

        if (pendExecPath.isEmpty() == false) {
          pid.setString("$.process_info.pend_exec_path", pendExecPath);
        }
        else {
          logger.info("Case id -> {}, could not find a exec path to pend", caseId);
          throw new UnifyException("flowret_err_12", caseId);
        }
      }
    }
  }

  private static void sanitize(Document pid, String caseId, ProcessDefinition pd) {
    // check for existence of field is_complete
    // if not there then we are dealing with a file created by the previous version of flowret
    // in this case examine all exec paths and if all say completed without a pend
    // then we assume case is complete

    // also check for existence of field ticket in each exec path. If not found then we are
    // dealing with an application created by a previous version of flowret
    // we need to do this only if the ticket is set else the new version will self correct
    // in this case select the pend exec path and set the ticket field there

    // this function will fix the data in the process info file to handle for possible orphaned applications
    // orphaned applications can result when the jvm crashes. In case of a jvm crash we will attempt to
    // recover from the last known state of the process. At worst, one step / route (per execution path) which could not log itself
    // into process info file will get executed once again and that is unavoidable from an orchestration perspective

    // To handle a step / route getting executed once again in case of a crash, the application services
    // which are invoked may need to take some extra care of being idempotent so as to avoid unwanted side affects

    // the logic is like this
    // First identify if we are dealing with an orphaned case. The logic for doing this is to
    // traverse through all exec paths.

    // If the status is started then it means that the execution path did not get a chance to complete

    // For all such execution paths, set the status to complete
    // Further for those which last executed a step, set the urt to ok pend and the workbasket. For others set it to ok proceed
    // Lastly set the deepest level as pend exec path (only if ticket is not outstanding)

    // this logic should work for both single and multithreaded use cases

    setIsComplete(pid);

    boolean isTicket = checkAndSetTicketInExecPath(pid);
    if (isTicket == false) {
      checkExecPathCompletion(pid, caseId, pd);
    }

    setPendExecPath(pid, caseId);
  }

  private static List<ProcessVariable> getProcessVariablesFromProcessInfo(Document d) {
    int size = d.getArraySize("$.process_info.process_variables[]");
    List<ProcessVariable> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      String name = d.getString("$.process_info.process_variables[%].name", i + "");
      String value = d.getString("$.process_info.process_variables[%].value", i + "");
      String type = d.getString("$.process_info.process_variables[%].type", i + "");
      ProcessVariableType pvt = ProcessVariableType.valueOf(type.toUpperCase());
      Object vo = getValueAsObject(pvt, value);
      ProcessVariable pv = new ProcessVariable(name, ProcessVariableType.valueOf(type.toUpperCase()), vo);
      list.add(pv);
    }
    return list;
  }

  private static Object getValueAsObject(ProcessVariableType type, String value) {
    Object vo = null;

    switch (type) {
      case BOOLEAN: {
        vo = new Boolean(value);
        break;
      }

      case LONG: {
        vo = new Long(value);
        break;
      }

      case INTEGER: {
        vo = new Integer(value);
        break;
      }

      case STRING: {
        vo = value;
        break;
      }
    }

    return vo;
  }

  private static List<ProcessVariable> getProcessVariablesFromProcessDefinition(Document d) {
    int size = d.getArraySize("$.journey.process_variables[]");
    List<ProcessVariable> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      String name = d.getString("$.journey.process_variables[%].name", i + "");
      String value = d.getString("$.journey.process_variables[%].value", i + "");
      String type = d.getString("$.journey.process_variables[%].type", i + "");
      ProcessVariableType pvt = ProcessVariableType.valueOf(type.toUpperCase());
      Object vo = getValueAsObject(pvt, value);
      ProcessVariable pv = new ProcessVariable(name, ProcessVariableType.valueOf(type.toUpperCase()), vo);
      list.add(pv);
    }
    return list;
  }

  private static void getProcessVariablesFromProcessInfo(ProcessInfo pi, Document d) {
    List<ProcessVariable> list = getProcessVariablesFromProcessInfo(d);
    for (ProcessVariable pv : list) {
      pi.setProcessVariable(pv);
    }

    int size = d.getArraySize("$.process_info.process_variables[]");
    for (int i = 0; i < size; i++) {
      String name = d.getString("$.process_info.process_variables[%].name", i + "");
      String value = d.getString("$.process_info.process_variables[%].value", i + "");
      String type = d.getString("$.process_info.process_variables[%].type", i + "");
      ProcessVariableType pvt = ProcessVariableType.valueOf(type.toUpperCase());
      Object vo = getValueAsObject(pvt, value);
      ProcessVariable pv = new ProcessVariable(name, ProcessVariableType.valueOf(type.toUpperCase()), vo);
      pi.setProcessVariable(pv);
    }
  }

  private static void getExecPaths(ProcessInfo pi, Document d) {
    int size = d.getArraySize("$.process_info.exec_paths[]");
    for (int i = 0; i < size; i++) {
      String name = d.getString("$.process_info.exec_paths[%].name", i + "");
      String status = d.getString("$.process_info.exec_paths[%].status", i + "");
      String step = d.getString("$.process_info.exec_paths[%].step", i + "");
      String pendWorkBasket = d.getString("$.process_info.exec_paths[%].pend_workbasket", i + "");
      String prevPendWorkBasket = d.getString("$.process_info.exec_paths[%].prev_pend_workbasket", i + "");
      String tbcSlaWorkBasket = d.getString("$.process_info.exec_paths[%].tbc_sla_workbasket", i + "");

      ErrorTuple et = new ErrorTuple();
      String errorCode = d.getString("$.process_info.exec_paths[%].error.code", i + "");
      if (errorCode != null) {
        String errorMessage = d.getString("$.process_info.exec_paths[%].error.message", i + "");
        String errorDetails = d.getString("$.process_info.exec_paths[%].error.details", i + "");
        boolean isRetryable = d.getBoolean("$.process_info.exec_paths[%].error.is_retryable", i + "");
        et.setErrorCode(errorCode);
        et.setErrorMessage(errorMessage);
        et.setErrorDetails(errorDetails);
        et.setRetryable(isRetryable);
      }

      String surt = d.getString("$.process_info.exec_paths[%].unit_response_type", i + "");
      UnitResponseType urt = null;
      if (surt != null) {
        urt = UnitResponseType.valueOf(surt.toUpperCase());
      }

      ExecPath ep = new ExecPath(name);
      ep.set(ExecPathStatus.valueOf(status.toUpperCase()), step, urt);
      ep.setPendWorkBasket(pendWorkBasket);
      ep.setPendErrorTuple(et);
      ep.setPrevPendWorkBasket(prevPendWorkBasket);
      ep.setTbcSlaWorkBasket(tbcSlaWorkBasket);
      pi.setExecPath(ep);
    }
  }

  private static void getTicketInfo(ProcessInfo pi, Document d) {
    String ticket = d.getString("$.process_info.ticket");
    if (ticket == null) {
      ticket = "";
    }
    pi.getSetter().setTicket(ticket);
  }

  protected static ProcessDefinition getProcessDefinition(Document d) {
    ProcessDefinition pd = new ProcessDefinition();

    pd.setName(d.getString("$.journey.name"));

    // process variables
    if (d.pathExists("$.journey.process_variables[]")) {
      List<ProcessVariable> list = getProcessVariablesFromProcessDefinition(d);
      pd.setProcessVariables(list);
    }

    // tickets
    if (d.pathExists("$.journey.tickets[]")) {
      int size = d.getArraySize("$.journey.tickets[]");
      for (int i = 0; i < size; i++) {
        String name = d.getString("$.journey.tickets[%].name", i + "");
        String stepName = d.getString("$.journey.tickets[%].step", i + "");
        Ticket t = new Ticket(name, stepName);
        pd.setTicket(t);
      }
    }

    // flows
    {
      int size = d.getArraySize("$.journey.flow[]");
      for (int i = 0; i < size; i++) {
        String si = i + "";

        UnitType type = null;
        String s = d.getString("$.journey.flow[%].type", si);
        if (s == null) {
          type = UnitType.STEP;
        }
        else {
          type = UnitType.valueOf(s.toUpperCase());
        }

        Unit unit = null;
        switch (type) {
          case STEP:
            unit = getStep(d, si);
            break;

          case S_ROUTE:
            unit = getRoute(d, si, UnitType.S_ROUTE);
            break;

          case P_ROUTE:
            unit = getRoute(d, si, UnitType.P_ROUTE);
            break;

          case P_ROUTE_DYNAMIC:
            unit = getRoute(d, si, UnitType.P_ROUTE_DYNAMIC);
            break;

          case PAUSE:
            unit = getPause(d, si);
            break;

          case PERSIST:
            unit = getPersist(d, si);
            break;

          case P_JOIN:
            unit = getJoin(d, si);
            break;
        }

        pd.addUnit(unit);
      }
    }

    return pd;
  }

  private static Unit getStep(Document d, String si) {
    String name = d.getString("$.journey.flow[%].name", si);
    String component = d.getString("$.journey.flow[%].component", si);
    String next = d.getString("$.journey.flow[%].next", si);
    String userData = d.getString("$.journey.flow[%].user_data", si);
    return new Step(name, component, next, userData);
  }

  private static Unit getPause(Document d, String si) {
    String name = d.getString("$.journey.flow[%].name", si);
    String next = d.getString("$.journey.flow[%].next", si);
    return new Pause(name, next);
  }

  private static Unit getPersist(Document d, String si) {
    String name = d.getString("$.journey.flow[%].name", si);
    String next = d.getString("$.journey.flow[%].next", si);
    return new Persist(name, next);
  }

  private static Unit getJoin(Document d, String si) {
    String name = d.getString("$.journey.flow[%].name", si);
    String next = d.getString("$.journey.flow[%].next", si);
    return new Join(name, next);
  }

  private static Unit getRoute(Document d, String si, UnitType type) {
    String name = d.getString("$.journey.flow[%].name", si);
    String component = d.getString("$.journey.flow[%].component", si);
    String userData = d.getString("$.journey.flow[%].user_data", si);
    String next = d.getString("$.journey.flow[%].next", si);
    boolean hasBranches = d.pathExists("$.journey.flow[%].branches[]", si);

    if ((type == UnitType.P_ROUTE) && (next != null)) {
      throw new UnifyException("flowret_err_9");
    }

    if ((type == UnitType.P_ROUTE_DYNAMIC) && (hasBranches == true)) {
      throw new UnifyException("flowret_err_10");
    }

    Route route = null;
    if (next != null) {
      route = new Route(name, component, userData, next, type);
    }
    else {
      Map<String, Branch> branches = new HashMap<>();
      int size = d.getArraySize("$.journey.flow[%].branches[]", si);
      for (int i = 0; i < size; i++) {
        String bname = d.getString("$.journey.flow[%].branches[%].name", si, i + "");
        String next1 = d.getString("$.journey.flow[%].branches[%].next", si, i + "");
        Branch branch = new Branch(bname, next1);
        branches.put(bname, branch);
      }
      route = new Route(name, component, userData, branches, type);
    }

    return route;
  }

  protected static void enqueueCaseStartMilestones(ProcessContext pc, Document slad, ISlaQueueManager slaQm) {
    Document d = slad;
    Document md = new JDocument();
    int j = 0;
    int size = d.getArraySize("$.milestones[]");

    for (int i = 0; i < size; i++) {
      String s = d.getString("$.milestones[%].setup_on", i + "");
      if (s.equals(SlaMilestoneSetupOn.case_start.toString())) {
        md.setContent(d, "$.milestones[%]", "$.milestones[%]", i + "", j + "");
        j++;
      }
    }

    size = md.getArraySize("$.milestones[]");
    if (size > 0) {
      logger.info("Case id -> {}, raising sla milestones enqueue event on case start for milestones -> {}", pc.getCaseId(), md.getPrettyPrintJson());
      slaQm.enqueue(pc, md);
    }
  }

  protected static void dequeueWorkBasketMilestones(ProcessContext pc, String wb, ISlaQueueManager slaQm) {
    if (wb.isEmpty()) {
      return;
    }

    logger.info("Case id -> {}, raising sla milestones dequeue event on exit of work basket -> {}", pc.getCaseId(), wb);
    slaQm.dequeue(pc, wb);
  }

  protected static void enqueueWorkBasketMilestones(ProcessContext pc, SlaMilestoneSetupOn setupOn, String
          wb, Document slad, ISlaQueueManager slaQm) {
    Document d = slad;
    Document md = new JDocument();
    int j = 0;
    int size = d.getArraySize("$.milestones[]");

    for (int i = 0; i < size; i++) {
      String s = d.getString("$.milestones[%].setup_on", i + "");
      if (s.equals(setupOn.toString())) {
        if (d.getString("$.milestones[%].work_basket_name", i + "").equals(wb)) {
          md.setContent(d, "$.milestones[%]", "$.milestones[%]", i + "", j + "");
          j++;
        }
      }
    }

    size = md.getArraySize("$.milestones[]");
    if (size > 0) {
      logger.info("Case id -> {}, raising sla milestones enqueue event on -> {} of work basket -> {} for milestones -> {}", pc.getCaseId(), setupOn.toString(), wb, md.getPrettyPrintJson());
      slaQm.enqueue(pc, md);
    }
  }

  protected static void writeAuditLog(FlowretDao dao, ProcessInfo pi, Unit lastUnit, List<String> branches, String
          compName) {
    // write the process info as audit log
    long seq = dao.incrCounter("flowret_audit_log_counter-" + pi.getCaseId());
    String s = String.format("%05d", seq);
    String key = CONSTS_FLOWRET.DAO.AUDIT_LOG + CONSTS_FLOWRET.DAO.SEP + pi.getCaseId() + "_" + s + "_" + compName;

    if (lastUnit != null) {
      pi.getSetter().setLastUnitExecuted(lastUnit);
    }
    Document d = pi.getDocument();

    // special handling if the last unit executed was a route
    if (branches != null) {
      for (int i = 0; i < branches.size(); i++) {
        String branch = branches.get(i);
        d.setArrayValueString("$.process_info.branches[%]", branch, i + "");
      }
    }

    dao.write(key, d);
  }

}
