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

import com.americanexpress.unify.base.UnifyException;
import com.americanexpress.unify.jdocs.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/*
 * @author Deepak Arora
 */
public class ExecThreadTask implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(ExecThreadTask.class);

  // these variables are shared across threads
  private Rts rts = null;
  private ProcessDefinition pd = null;
  private ProcessInfo pi = null;

  // these variables are thread specific
  private ExecPath execPath = null;
  private boolean isRootThread = false;

  // this variable is used to suppress the writing of the audit log for parallel route thread when it joins on its child threads
  // basically for a parallel route we would have written the audit log before creating the threads and we do not
  // want to write it again after the join or pend condition is reached and the parent thread completes
  private boolean writeAuditLog = true;

  protected ExecThreadTask(Rts rts) {
    this.pd = rts.pd;
    this.pi = rts.pi;
    this.rts = rts;
  }

  @Override
  public void run() {
    execute();
  }

  // return not null if the process was run and null otherwise
  protected ProcessContext execute() {
    if (onStart() == false) {
      return null;
    }

    // start the recursive play of threads from here
    String next = null;
    Unit unit = pd.getUnit(execPath.getStep());
    ProcessContext pc = null;

    outer:
    while (true) {
      if (unit == null) {
        next = "end";
      }
      else {
        switch (unit.getType()) {
          case STEP: {
            next = processStep((Step)unit);

            if (next == null) {
              break outer;
            }
            break;
          }

          case P_ROUTE:
          case P_ROUTE_DYNAMIC: {
            next = processParallelRoute((Route)unit);

            try {
              pi.getLock().lock();

              if (next == null) {
                if (isRootThread == true) {
                  String ticketName = pi.getTicket();
                  if (ticketName.isEmpty() == false) {
                    Ticket ticket = pd.getTicket(ticketName);
                    if (pi.getTicketUrt() == UnitResponseType.OK_PROCEED) {
                      if (execPath.getName().equals(".")) {
                        // raise event
                        pc = ProcessContext.forEvent(EventType.ON_TICKET_RAISED, rts, execPath.getName());
                        rts.invokeEventHandler(EventType.ON_TICKET_RAISED, pc);

                        // we set next, clear out ticket and proceed
                        next = ticket.getStep();
                        pi.getSetter().setTicket("");
                      }
                      else {
                        // mark current execution path as completed
                        // become the "." execution path and continue
                        logger.info("Case id -> " + pi.getCaseId() + ", child thread going to assume parent role, execution path -> " + execPath.getName());

                        // raise event
                        pc = ProcessContext.forEvent(EventType.ON_TICKET_RAISED, rts, execPath.getName());
                        rts.invokeEventHandler(EventType.ON_TICKET_RAISED, pc);

                        execPath.set(ExecPathStatus.COMPLETED, unit.getName(), UnitResponseType.OK_PROCEED);
                        ExecPath ep = new ExecPath(".");
                        ep.set(ticket.getStep(), UnitResponseType.OK_PROCEED);
                        execPath = ep;
                        pi.setExecPath(ep);
                        next = ticket.getStep();
                        pi.getSetter().setTicket("");
                      }
                    }
                    else {
                      // the ticket is asking us to pend
                      ExecPath tep = getTicketRaisingExecPath();
                      ExecPath ep = new ExecPath(".");
                      ep.setPendWorkBasket(tep.getPendWorkBasket());
                      ep.setPrevPendWorkBasket(tep.getPrevPendWorkBasket());
                      ep.setTbcSlaWorkBasket(tep.getTbcSlaWorkBasket());
                      ep.set(ExecPathStatus.COMPLETED, tep.getStep(), pi.getTicketUrt());

                      pi.clearPendWorkBaskets();
                      pi.setExecPath(ep);
                      pi.getSetter().setPendExecPath(".");
                      execPath = ep;
                    }

                    if (next == null) {
                      break outer;
                    }
                    else {
                      break;
                    }
                  }
                }
                break outer;
              }
            }
            finally {
              pi.getLock().unlock();
            }

            break;
          }

          case S_ROUTE: {
            next = processSingularRoute((Route)unit);
            if (next == null) {
              break outer;
            }
            break;
          }

          case PAUSE: {
            processPause((Pause)unit);
            next = null;
            break outer;
          }

          case PERSIST: {
            next = processPersist((Persist)unit);
            if (next == null) {
              break outer;
            }
            break;
          }

          case P_JOIN: {
            next = processJoin((Join)unit);
            if (next == null) {
              break outer;
            }
            break;
          }
        }
      }

      if (next.equalsIgnoreCase("end") == false) {
        writeProcessInfoAndAuditLog(pi, unit, Flowret.instance().isWriteProcessInfoAfterEachStep());
      }
      else {
        execPath.set(ExecPathStatus.COMPLETED, execPath.getStep(), UnitResponseType.OK_PROCEED);

        try {
          pi.getLock().lock();
          pi.getSetter().setPendExecPath("");
          pi.setCaseCompleted();
        }
        finally {
          pi.getLock().unlock();
        }

        break outer;
      }

      unit = pd.getUnit(next);
    }

    if (isRootThread == true) {
      if (next == null) {
        if (pi.getTicket().isEmpty() == false) {
          pc = ProcessContext.forEvent(EventType.ON_TICKET_RAISED, rts, execPath.getName());
          rts.invokeEventHandler(EventType.ON_TICKET_RAISED, pc);
        }

        pc = ProcessContext.forEvent(EventType.ON_PROCESS_PEND, rts, execPath.getName());
        rts.invokeEventHandler(EventType.ON_PROCESS_PEND, pc);
      }
      else {
        if (next.equalsIgnoreCase("end")) {
          pc = ProcessContext.forEvent(EventType.ON_PROCESS_COMPLETE, rts, execPath.getName());
          rts.invokeEventHandler(EventType.ON_PROCESS_COMPLETE, pc);
        }
        else {
          // this will not happen
        }
      }
      writeProcessInfoAndAuditLog(pi, unit, true);
    }
    else {
      writeProcessInfoAndAuditLog(pi, unit, true);
    }

    return pc;
  }

  private ExecPath getTicketRaisingExecPath() {
    List<ExecPath> paths = pi.getExecPaths();
    ExecPath ep = null;
    for (ExecPath path : paths) {
      String ticket = path.getTicket();
      if (ticket.isEmpty() == false) {
        ep = path;
        break;
      }
    }
    return ep;
  }

  // return true if we need to proceed with running the process else false
  private boolean onStart() {
    boolean start = true;

    while (true) {
      if (execPath != null) {
        // a thread has invoked a child. Nothing to do in the case but just move ahead
        break;
      }

      // this means that we are starting up from a call to resume case
      isRootThread = true;
      execPath = getStartContext(pd, pi);
      if (execPath == null) {
        start = false;
        break;
      }

      // clear out the pend and ticket information as we are now about to run
      pi.getSetter().setPendExecPath("").setTicket("");
      break;
    }

    return start;
  }

  // this function is only called when we are starting by reading the contents of the process info file
  private ExecPath getStartContext(ProcessDefinition pd, ProcessInfo pi) {
    ExecPath ep = null;

    while (true) {
      // check if we have never ever started
      if (pi.isCaseStarted() == false) {
        ep = new ExecPath(".");
        ep.set(ExecPathStatus.STARTED, "start", null);
        pi.setExecPath(ep);
        break;
      }

      {
        // we are in a pended state. We first check if a ticket is set
        if (pi.getTicket().isEmpty() == false) {
          Ticket ticket = pd.getTicket(pi.getTicket());

          // clear out process information
          pi.getSetter().setPendExecPath("").setTicket("");
          pi.clearExecPaths();

          // assign just one exec path to start with
          ep = new ExecPath(".");
          ep.set(ExecPathStatus.STARTED, ticket.getStep(), null);
          pi.setExecPath(ep);
          break;
        }
      }

      {
        // Check if we are pended in a pause state
        // if we are, set the next step of exec path to the one pointed to by pause
        ep = pi.getExecPath(pi.getPendExecPath());
        Unit c = pd.getUnit(ep.getStep());
        if (c.getType() == UnitType.PAUSE) {
          Pause p = (Pause)c;
          ep.set(ExecPathStatus.STARTED, p.getNext(), null);
          break;
        }
      }

      // we are pended at a step or a route
      {
        ep = pi.getExecPath(pi.getPendExecPath());
        UnitResponseType urt = ep.getUnitResponseType();
        switch (urt) {
          case OK_PEND: {
            // we are at a step and we need to start from the next step
            Step pendStep = (Step)pd.getUnit(ep.getStep());
            ep.set(ExecPathStatus.STARTED, pendStep.getNext(), null);
            ep.setPendWorkBasket("");
            break;
          }

          case OK_PEND_EOR: {
            // we may be at a step or a route
            ep.set(ExecPathStatus.STARTED, ep.getStep(), null);
            ep.setPendWorkBasket("");
            break;
          }

          case OK_PROCEED: {
            // we will never face this condition as we would have moved ahead in the process if
            // a component was to return this value
            throw new UnifyException("flowret_err_5", pi.getCaseId());
          }

          case ERROR_PEND: {
            // we are pended on a step or route after an error and so we need to start from there
            ep.set(ExecPathStatus.STARTED, ep.getStep(), null);
            ep.setPendWorkBasket("");
            break;
          }
        }
        break;
      }
    }

    return ep;
  }

  private String processStep(Step step) {
    String next = null;

    logger.info("Case id -> " + pi.getCaseId() + ", executing step -> " + step.getName() + ", component -> " + step.getComponentName() + ", execution path -> " + execPath.getName());

    StepResponse resp = executeStep(step);

    try {
      pi.getLock().lock();

      UnitResponseType urt = resp.getUnitResponseType();

      if ((urt == UnitResponseType.OK_PROCEED) || (urt == UnitResponseType.OK_PEND)) {
        pi.isPendAtSameStep = false;
      }

      switch (urt) {
        case OK_PEND:
        case OK_PEND_EOR: {
          logger.info("Case id -> " + pi.getCaseId() + ", pending at step -> " + step.getName() + ", component -> " + step.getComponentName() + ", execution path -> " + execPath.getName());

          execPath.set(ExecPathStatus.COMPLETED, step.getName(), urt);
          execPath.setPendWorkBasket(resp.getWorkBasket());

          // we first check if there is already a ticket set
          if (pi.getTicket().isEmpty() == false) {
            // we only need to update the process variables and terminate
            logger.info("Case id -> " + pi.getCaseId() + ", abandoning as ticket is already set -> " + pi.getTicket() + ", component -> " + step.getComponentName() + ", execution path -> " + execPath.getName());
            break;
          }

          // now we check if we have raised a ticket
          String ticketName = resp.getTicket();
          if (ticketName.isEmpty() == false) {
            // in this case set the ticket and pend exec path
            logger.info("Case id -> " + pi.getCaseId() + ", encountered ticket -> " + ticketName + ", component -> " + step.getComponentName() + ", execution path -> " + execPath.getName());
            execPath.setTicket(ticketName);
            pi.getSetter().setTicketUrt(urt).setPendExecPath(execPath.getName()).setTicket(ticketName);
            break;
          }

          // there is no ticket existing or raised. We do normal processing
          pi.getSetter().setPendExecPath(execPath.getName()).setTicket("");

          break;
        }

        case OK_PROCEED: {
          execPath.set(step.getName(), urt);

          // again we first check if there is already a ticket set
          if (pi.getTicket().isEmpty() == false) {
            // we only need to update the process variables and terminate
            logger.info("Case id -> " + pi.getCaseId() + ", abandoning as ticket is already set -> " + pi.getTicket() + ", component -> " + step.getComponentName() + ", execution path -> " + execPath.getName());
            execPath.set(ExecPathStatus.COMPLETED, step.getName(), urt);
            break;
          }

          // check if this call has raised a ticket
          String ticketName = resp.getTicket();
          if (ticketName.isEmpty() == false) {
            logger.info("Case id -> " + pi.getCaseId() + ", encountered ticket -> " + ticketName + ", component -> " + step.getComponentName() + ", execution path -> " + execPath.getName());

            pi.getSetter().setTicketUrt(UnitResponseType.OK_PROCEED).setTicket(resp.getTicket());

            if (isRootThread == true) {
              // raise event
              rts.invokeEventHandler(EventType.ON_TICKET_RAISED, ProcessContext.forEvent(EventType.ON_TICKET_RAISED, rts, execPath.getName()));

              if (execPath.getName().equals(".")) {
                // we only need to set next, clear out ticket and proceed
                Ticket ticket = pd.getTicket(ticketName);
                next = ticket.getStep();
                pi.getSetter().setTicket("");
              }
              else {
                // mark current execution path as completed
                // become the "." execution path and continue
                logger.info("Case id -> " + pi.getCaseId() + ", child thread going to assume parent role (\".\"), execution path -> " + execPath.getName());

                execPath.set(ExecPathStatus.COMPLETED, step.getName(), urt);
                ExecPath ep = new ExecPath(".");
                ep.set(ExecPathStatus.STARTED, step.getName(), urt);
                execPath = ep;
                pi.setExecPath(ep);
                Ticket ticket = pd.getTicket(ticketName);
                next = ticket.getStep();
                pi.getSetter().setTicket("");
              }
            }
            else {
              // we are a child thread and our parent is running and so we need to set ticket and terminate
              // and let the parent handle the ticket
              logger.info("Case id -> " + pi.getCaseId() + ", child thread exiting, execution path -> " + execPath.getName());
              execPath.set(ExecPathStatus.COMPLETED, step.getName(), urt);
              execPath.setTicket(ticketName);
              pi.getSetter().setTicket(ticketName);
            }
          }
          else {
            // no ticket is raised hence do normal processing
            next = step.getNext();
          }

          break;
        }

        case ERROR_PEND: {
          logger.info("Case id -> " + pi.getCaseId() + ", pending at step -> " + step.getName() + ", component -> " + step.getComponentName() + ", execution path -> " + execPath.getName());
          execPath.set(ExecPathStatus.COMPLETED, step.getName(), urt);
          execPath.setPendWorkBasket(resp.getWorkBasket());
          execPath.setPendErrorTuple(resp.getErrorTuple());
          pi.getSetter().setPendExecPath(execPath.getName());
          break;
        }
      }
    }
    finally {
      pi.getLock().unlock();
    }

    return next;
  }

  private String processPersist(Persist step) {
    String next = null;
    try {
      logger.info("Case id -> " + pi.getCaseId() + ", executing persist step -> " + step.getName() + ", execution path -> " + execPath.getName());
      pi.isPendAtSameStep = false;
      rts.invokeEventHandler(EventType.ON_PERSIST, ProcessContext.forEvent(EventType.ON_PERSIST, rts, execPath.getName()));
      execPath.set(step.getName(), UnitResponseType.OK_PROCEED);
      next = step.getNext();
      return next;
    }
    catch (Exception e) {
      logger.info("Case id -> " + pi.getCaseId() + ", pending at persist step -> " + step.getName() + ", execution path -> " + execPath.getName());
      execPath.set(step.getName(), UnitResponseType.ERROR_PEND);
      execPath.setPendWorkBasket("flowret_error");
      return null;
    }
  }

  private void setChildExecPaths(Route route, List<String> branches) {
    for (int i = 0; i < branches.size(); i++) {
      String branchName = branches.get(i);
      String execPathName = execPath.getName() + route.getName() + "." + branchName + ".";
      ExecPath ep = new ExecPath(execPathName);
      if (route.getNext() != null) {
        ep.set(route.getNext(), null);
      }
      else {
        ep.set(route.getBranch(branchName).getNext(), null);
      }
      pi.setExecPath(ep);
    }
  }

  private String processParallelRoute(Route route) {
    String next = null;

    logger.info("Case id -> " + pi.getCaseId() + ", executing parallel routing rule -> " + route.getName() + ", execution path -> " + execPath.getName());

    RouteResponse resp = executeRule(route);

    UnitResponseType urt = resp.getUnitResponseType();

    if (urt == UnitResponseType.OK_PROCEED) {
      pi.isPendAtSameStep = false;
      execPath.set(route.getName(), urt);
    }
    else {
      execPath.set(route.getName(), urt);
      execPath.setPendWorkBasket(resp.getWorkBasket());
      execPath.setPendErrorTuple(resp.getErrorTuple());
    }

    setChildExecPaths(route, resp.getBranches());

    try {
      pi.getLock().lock();

      if (Flowret.instance().isWriteProcessInfoAfterEachStep() == true) {
        writeProcessInfo(pi, route);
      }

      writeAuditLog(pi, route, resp.getBranches());
      writeAuditLog = false;
    }
    finally {
      pi.getLock().unlock();
    }

    switch (urt) {
      case OK_PEND:
      case OK_PEND_EOR: {
        // this situation will not arise as a route cannot specify a pend upon successful execution
        throw new UnifyException("flowret_err_8");
      }

      case OK_PROCEED: {
        String joinPoint = executeThreads(execPath, route, resp.getBranches());

        if (joinPoint != null) {
          // we have reached the join point and all threads that were supposed to reach the join point have completed
          // in this case we are the parent thread and we move ahead in the process
          if (pi.getTicket().isEmpty() == false) {
            if (isRootThread == true) {
              if (execPath.equals(".")) {
                next = pd.getTicket(pi.getTicket()).getStep();
              }
              else {
                try {
                  pi.getLock().lock();

                  // mark current execution path as completed
                  // become the parent execution path and continue
                  execPath.set(ExecPathStatus.COMPLETED, route.getName(), urt);
                  ExecPath ep = new ExecPath(execPath.getParentExecPathName());
                  ep.set(route.getName(), urt);
                  execPath = ep;
                  pi.setExecPath(ep);
                  Ticket ticket = pd.getTicket(pi.getTicket());
                  next = ticket.getStep();
                  pi.getSetter().setTicket("");
                }
                finally {
                  pi.getLock().unlock();
                }
              }
            }
            else {
              // mark myself completed and let the parent handle ticket
              execPath.set(ExecPathStatus.COMPLETED, route.getName(), urt);
            }
          }
          else {
            Join j = (Join)pd.getUnit(joinPoint);
            next = j.getNext();
          }

          break;
        }

        // we reach here because we are the main thread and some child thread has pended
        // in this case we are going to terminate and so set ourselves as completed
        execPath.set(ExecPathStatus.COMPLETED, route.getName(), urt);
        break;
      }

      case ERROR_PEND: {
        logger.info("Case id -> " + pi.getCaseId() + ", pending at parallel route -> " + route.getName() + ", component -> " + route.getComponentName() + ", execution path -> " + execPath.getName());

        try {
          pi.getLock().lock();
          pi.getSetter().setPendExecPath(execPath.getName());
        }
        finally {
          pi.getLock().unlock();
        }
        break;
      }
    }

    return next;
  }

  private String processSingularRoute(Route route) {
    String next = null;

    logger.info("Case id -> " + pi.getCaseId() + ", executing singular routing rule -> " + route.getName() + ", execution path -> " + execPath.getName());

    RouteResponse resp = executeRule(route);

    try {
      pi.getLock().lock();

      UnitResponseType urt = resp.getUnitResponseType();

      if ((urt == UnitResponseType.OK_PROCEED) || (urt == UnitResponseType.OK_PEND)) {
        pi.isPendAtSameStep = false;
      }

      writeAuditLog(pi, route, resp.getBranches());
      writeAuditLog = false;

      switch (urt) {
        case OK_PEND:
        case OK_PEND_EOR: {
          // this situation will not arise as a route cannot specify a pend upon successful execution
          throw new UnifyException("anexdeus_err_8");
        }

        case OK_PROCEED: {
          execPath.set(route.getName(), urt);
          String branchName = resp.getBranches().get(0);
          next = route.getBranch(branchName).getNext();
          break;
        }

        case ERROR_PEND: {
          logger.info("Case id -> " + pi.getCaseId() + ", pending at route -> " + route.getName() + ", component -> " + route.getComponentName() + ", execution path -> " + execPath.getName());
          execPath.set(route.getName(), urt);
          execPath.setPendWorkBasket(resp.getWorkBasket());
          execPath.setPendErrorTuple(resp.getErrorTuple());
          pi.getSetter().setPendExecPath(execPath.getName());
          break;
        }
      }
    }
    finally {
      pi.getLock().unlock();
    }

    return next;
  }

  private void processPause(Pause pause) {
    logger.info("Case id -> " + pi.getCaseId() + ", executing pause step -> " + pause.getName() + ", execution path -> " + execPath.getName());
    try {
      pi.getLock().lock();
      execPath.set(pause.getName(), UnitResponseType.OK_PEND);
      execPath.setPendWorkBasket("flowret_pause");
      pi.getSetter().setPendExecPath(execPath.getName());
    }
    finally {
      pi.getLock().unlock();
    }
  }

  private String processJoin(Join join) {
    String next = null;

    logger.info("Case id -> " + pi.getCaseId() + ", handling join for execution path -> " + execPath.getName());

    try {
      pi.getLock().lock();

      // mark myself as complete first. This will reflect in pi
      execPath.set(ExecPathStatus.COMPLETED, join.getName(), UnitResponseType.OK_PROCEED);

      // check if all siblings have completed
      // this is used for correctly unravelling the pended process. The other approach
      // could have been to explicitly select the next pended execution path, become that
      // execution path and start from there. May be done in the future
      boolean isComplete = true;
      ExecPath pendedEp = null;
      List<ExecPath> paths = pi.getExecPaths();
      for (ExecPath path : paths) {
        // ignore self
        if (path.getName().equals(path)) {
          continue;
        }

        if (execPath.isSibling(path)) {
          if (path.getPendWorkBasket().isEmpty() == false) {
            isComplete = false;
            pendedEp = path;
            break;
          }
        }
      }

      if (isComplete == true) {
        // we need to become parent and continue processing
        ExecPath parentEp = pi.getExecPath(execPath.getParentExecPathName());
        if (parentEp.getStatus() == ExecPathStatus.COMPLETED) {
          parentEp.set(ExecPathStatus.STARTED, join.getName(), UnitResponseType.OK_PROCEED);
          execPath = parentEp;
          next = join.getNext();
        }
        else {
          // parent thread is running and will will let that thread take over and so nothing to do
        }
      }
      else {
        if (pendedEp != null) {
          pi.getSetter().setPendExecPath(pendedEp.getName());
        }
      }
    }
    finally {
      pi.getLock().unlock();
    }

    return next;
  }

  private String executeThreads(ExecPath parentExecPath, Route route, List<String> branches) {
    int count = branches.size();
    ExecThreadTask[] tasks = new ExecThreadTask[count];
    Future<?>[] futures = new Future[count];
    ExecutorService es = Flowret.instance().getExecutorService();

    for (int i = 0; i < count; i++) {
      String branchName = branches.get(i);
      String next = route.getNext();

      if (next != null) {
        next = route.getNext();
      }
      else {
        Branch branch = route.getBranch(branchName);
        next = branch.getNext();
      }

      ExecPath ep = new ExecPath(parentExecPath.getName() + route.getName() + "." + branchName + ".");
      ep.setStep(pd.getUnit(next).getName());
      ExecThreadTask in = new ExecThreadTask(rts);
      in.execPath = ep;
      tasks[i] = in;
      pi.setExecPath(ep);
    }

    // start threads
    for (int i = 0; i < count; i++) {
      futures[i] = es.submit(tasks[i]);
    }

    // wait for threads to finish
    for (int i = 0; i < tasks.length; i++) {
      try {
        futures[i].get();
      }
      catch (InterruptedException | ExecutionException e) {
        // should never happen
        throw new UnifyException("flowret_err_5", e, pi.getCaseId());
      }
    }

    // check if all have completed
    boolean isPend = false;
    String joinPoint = null;
    for (int i = 0; i < tasks.length; i++) {
      ExecThreadTask in = tasks[i];
      ExecPath ep = in.execPath;
      joinPoint = ep.getStep();

      if (ep.getPendWorkBasket().isEmpty() == false) {
        isPend = true;
        break;
      }
    }

    if (isPend == false) {
      return joinPoint;
    }
    else {
      return null;
    }
  }

  private RouteResponse executeRule(Route route) {
    RouteResponse rr = null;
    ProcessContext pc = null;

    try {
      ProcessComponentFactory factory = rts.factory;
      pc = new ProcessContext(pd.getName(), pi.getCaseId(), route.getName(), route.getComponentName(), route.getUserData(), route.getType(), pi.getProcessVariables(), execPath.getName());
      InvokableRoute rule = (InvokableRoute)factory.getObject(pc);
      rr = rule.executeRoute();
    }
    catch (Exception e) {
      logger.error("Exception encountered while executing rule. Case id -> {}, step_name -> {}, comp_name -> {}", pi.getCaseId(), pc.getStepName(), pc.getCompName());
      logger.error("Exception details -> {}", e.getMessage());
      rr = new RouteResponse(UnitResponseType.ERROR_PEND, null, "flowret_error");
    }

    return rr;
  }

  private StepResponse executeStep(Step step) {
    StepResponse sr = null;
    ProcessContext pc = null;

    try {
      ProcessComponentFactory factory = rts.factory;
      pc = new ProcessContext(pd.getName(), pi.getCaseId(), step.getName(), step.getComponentName(), step.getUserData(), UnitType.STEP, pi.getProcessVariables(), execPath.getName(), rts.lastPendWorkBasket, rts.lastPendStep, pi.isPendAtSameStep);
      InvokableStep iStep = (InvokableStep)factory.getObject(pc);
      sr = iStep.executeStep();
    }
    catch (Exception e) {
      logger.error("Exception encountered while executing step. Case id -> {}, step_name -> {}, comp_name -> {}", pi.getCaseId(), pc.getStepName(), pc.getCompName());
      logger.error("Exception details -> {}", e.getMessage());
      sr = new StepResponse(UnitResponseType.ERROR_PEND, null, "flowret_error");
    }

    return sr;
  }

  private void writeProcessInfoAndAuditLog(ProcessInfo pi, Unit lastUnit, boolean writeProcessInfo) {
    try {
      pi.getLock().lock();

      if (writeProcessInfo == true) {
        writeProcessInfo(pi, lastUnit);
      }

      writeAuditLog(pi, lastUnit, null);
    }
    finally {
      pi.getLock().unlock();
    }
  }

  private void writeProcessInfo(ProcessInfo pi, Unit lastUnit) {
    pi.getSetter().setLastUnitExecuted(lastUnit);
    Document d = pi.getDocument();
    rts.dao.write(CONSTS_FLOWRET.DAO.PROCESS_INFO + CONSTS_FLOWRET.DAO.SEP + pi.getCaseId(), d);
  }

  private void writeAuditLog(ProcessInfo pi, Unit lastUnit, List<String> branches) {
    if (Flowret.instance().isWriteAuditLog() == false) {
      return;
    }

    if (writeAuditLog == false) {
      writeAuditLog = true;
      return;
    }

    if (lastUnit == null) {
      Utils.writeAuditLog(rts.dao, pi, null, branches, "end");
    }
    else {
      Utils.writeAuditLog(rts.dao, pi, lastUnit, branches, lastUnit.getName());
    }

  }

}
