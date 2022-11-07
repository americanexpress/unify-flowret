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
import com.americanexpress.unify.base.UnifyException;
import com.americanexpress.unify.jdocs.Document;
import com.americanexpress.unify.jdocs.JDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.americanexpress.unify.flowret.EventType.ON_PROCESS_PEND;

/*
 * @author Deepak Arora
 */
public final class Rts {

  private static Logger logger = LoggerFactory.getLogger(Rts.class);

  // variables are protected so that they can be accessed by classes in the same package
  protected FlowretDao dao = null;
  protected ProcessComponentFactory factory = null;
  protected EventHandler eventHandler = null;
  protected ProcessDefinition pd = null;
  protected Document slad = null;
  protected ProcessInfo pi = null;
  protected ISlaQueueManager slaQm = null;
  protected String lastPendWorkBasket = null;
  protected String lastPendStep = null;

  protected Rts(FlowretDao dao, ProcessComponentFactory factory, EventHandler eventHandler, ISlaQueueManager slaQm) {
    this.dao = dao;
    this.factory = factory;
    this.eventHandler = eventHandler;
    this.slaQm = slaQm;
  }

  protected void invokeEventHandler(EventType event, ProcessContext pc) {
    if (eventHandler == null) {
      return;
    }

    String wb = pc.getPendWorkBasket();
    wb = (wb == null) ? "" : wb;
    logger.info("Case id -> {}, raising event -> {}, comp name -> {}, work basket -> {}", pi.getCaseId(), event.name(), pc.getCompName(), wb);

    switch (event) {
      case ON_PROCESS_START:
      case ON_PROCESS_RESUME:
      case ON_PROCESS_COMPLETE:
      case ON_PROCESS_PEND:
      case ON_PROCESS_REOPEN:
        try {
          eventHandler.invoke(event, pc);
          if ((slad != null) && (slaQm != null)) {
            raiseSlaEvent(event, pc);
          }
          if (event == ON_PROCESS_PEND) {
            // set the prev pend work basket
            ExecPath ep = pi.getExecPath(pi.getPendExecPath());
            ep.setPrevPendWorkBasket(ep.getPendWorkBasket());
          }
        }
        catch (Exception e) {
          // we log an error but we do not stop and the application has generated an error and we are not responsible for that
          logger.error("Error encountered while invoking event. Case id -> {}, event type -> {}, error message -> {}", pi.getCaseId(), event.name(), e.getMessage());
          logger.error(BaseUtils.getStackTrace(e));
        }
        break;

      case ON_PERSIST:
      case ON_TICKET_RAISED:
        try {
          pi.getLock().lock();
          eventHandler.invoke(event, pc);
        }
        finally {
          pi.getLock().unlock();
        }
        break;
    }

  }

  public boolean isCaseStarted(String caseId) {
    String key = CONSTS_FLOWRET.DAO.PROCESS_INFO + CONSTS_FLOWRET.DAO.SEP + caseId;
    Document d = dao.read(key);
    if (d == null) {
      return false;
    }
    else {
      return true;
    }
  }

  private void abortIfStarted(String caseId) {
    String key = CONSTS_FLOWRET.DAO.PROCESS_INFO + CONSTS_FLOWRET.DAO.SEP + caseId;

    Document d = dao.read(key);
    if (d != null) {
      throw new UnifyException("flowret_err_1", caseId);
    }
  }

  public ProcessContext startCase(String caseId, String journeyJson, ProcessVariables pvs, String journeySlaJson) {
    if (pvs == null) {
      pvs = new ProcessVariables();
    }

    abortIfStarted(caseId);

    // check if the journey definition file exists. If it does, then we need to treat it as a case of
    // crash where the process was successfully started but the first step could not be executed
    String key = CONSTS_FLOWRET.DAO.JOURNEY + CONSTS_FLOWRET.DAO.SEP + caseId;
    boolean hasAlreadyStarted = false;
    Document d = dao.read(key);
    if (d != null) {
      hasAlreadyStarted = true;
    }

    // read the process definition and get process info
    d = new JDocument(journeyJson);
    dao.write(CONSTS_FLOWRET.DAO.JOURNEY + CONSTS_FLOWRET.DAO.SEP + caseId, d);
    pd = Utils.getProcessDefinition(d);
    pi = Utils.getProcessInfo(dao, caseId, pd);

    // write and get the sla configuration
    if (journeySlaJson != null) {
      slad = new JDocument(journeySlaJson);
      dao.write(CONSTS_FLOWRET.DAO.JOURNEY_SLA + CONSTS_FLOWRET.DAO.SEP + caseId, slad);
    }

    // update process variables
    List<ProcessVariable> list = pvs.getListOfProcessVariables();
    for (ProcessVariable pv : list) {
      pi.setProcessVariable(pv);
    }

    boolean bContinue = true;
    ProcessContext pc = null;
    if (hasAlreadyStarted == false) {
      logger.info("Case id -> " + pi.getCaseId() + ", successfully created case");

      // invoke event handler
      pc = ProcessContext.forEvent(EventType.ON_PROCESS_START, this, ".");
      try {
        invokeEventHandler(EventType.ON_PROCESS_START, pc);
      }
      catch (Exception e) {
        bContinue = false;
        pc = null;
        logger.info("Case id -> " + pi.getCaseId() + ", aborting as application exception encountered while raising event");
        logger.info("Case id -> " + pi.getCaseId() + ", exception details -> " + e.getMessage());
        logger.info("Case id -> " + pi.getCaseId() + ", exception stack -> " + e.getStackTrace());
      }
    }

    // start case
    if (bContinue == true) {
      pc = resumeCase(caseId, false, null);
    }

    return pc;
  }

  private ProcessContext resumeCase(String caseId, boolean raiseResumeEvent, ProcessVariables pvs) {
    if (raiseResumeEvent == true) {
      // we are being called on our own
      // read process definition
      String key = CONSTS_FLOWRET.DAO.JOURNEY + CONSTS_FLOWRET.DAO.SEP + caseId;
      Document d = dao.read(key);
      if (d == null) {
        throw new UnifyException("flowret_err_2", caseId);
      }
      pd = Utils.getProcessDefinition(d);
      pi = Utils.getProcessInfo(dao, caseId, pd);
      pi.isPendAtSameStep = true;

      // update process variables. We will add or update the ones passed in but not delete any
      if (pvs != null) {
        List<ProcessVariable> list = pvs.getListOfProcessVariables();
        for (ProcessVariable pv : list) {
          pi.setProcessVariable(pv);
        }
      }

      // read sla configuration
      key = CONSTS_FLOWRET.DAO.JOURNEY_SLA + CONSTS_FLOWRET.DAO.SEP + caseId;
      slad = dao.read(key);
    }

    // check if we have already completed
    if (pi.isCaseCompleted() == true) {
      throw new UnifyException("flowret_err_6", pi.getCaseId());
    }

    boolean bContinue = true;
    ProcessContext pc = null;
    try {
      if (raiseResumeEvent) {
        pc = ProcessContext.forEvent(EventType.ON_PROCESS_RESUME, this, pi.getPendExecPath());
        invokeEventHandler(EventType.ON_PROCESS_RESUME, pc);
      }

      if (pi.getTicket().isEmpty() == false) {
        pc = ProcessContext.forEvent(EventType.ON_TICKET_RAISED, this, pi.getPendExecPath());
        invokeEventHandler(EventType.ON_TICKET_RAISED, pc);
      }

    }
    catch (Exception e) {
      bContinue = false;
      pc = null;
      logger.info("Case id -> " + pi.getCaseId() + ", aborting as application exception encountered while raising event");
      logger.info("Case id -> " + pi.getCaseId() + ", exception details -> " + e.getMessage());
      logger.info("Case id -> " + pi.getCaseId() + ", exception stack -> " + e.getStackTrace());
    }

    if (bContinue == true) {
      // initiate on the current thread
      ExecThreadTask task = new ExecThreadTask(this);
      pc = task.execute();
    }

    return pc;
  }

  public ProcessContext resumeCase(String caseId) {
    return resumeCase(caseId, true, null);
  }

  public ProcessContext resumeCase(String caseId, ProcessVariables pvs) {
    return resumeCase(caseId, true, pvs);
  }

  public ProcessContext reopenCase(String caseId, String ticket, boolean pendBeforeResume, String pendWb) {
    if (pendBeforeResume == true) {
      if (BaseUtils.isNullOrEmpty(pendWb)) {
        throw new UnifyException("flowret_err_14", caseId);
      }
    }
    if (BaseUtils.isNullOrEmpty(ticket)) {
      throw new UnifyException("flowret_err_15", caseId);
    }

    // read journey file, process definition, process info and sla file
    String key = CONSTS_FLOWRET.DAO.JOURNEY + CONSTS_FLOWRET.DAO.SEP + caseId;
    Document d = dao.read(key);
    if (d == null) {
      throw new UnifyException("flowret_err_2", caseId);
    }
    pd = Utils.getProcessDefinition(d);
    pi = Utils.getProcessInfo(dao, caseId, pd);
    pi.isPendAtSameStep = false;
    key = CONSTS_FLOWRET.DAO.JOURNEY_SLA + CONSTS_FLOWRET.DAO.SEP + caseId;
    slad = dao.read(key);

    // check that the case should be completed
    if (pi.isCaseCompleted() == false) {
      throw new UnifyException("flowret_err_13", caseId);
    }

    // update relevant fields in the process info
    pi.getSetter().setPendExecPath(".");
    pi.setCaseCompleted(false);
    ExecPath ep = pi.getExecPath(".");
    if (pendBeforeResume == true) {
      ep.setPendWorkBasket(pendWb);
      ep.setUnitResponseType(UnitResponseType.OK_PEND);
    }
    ep.setTicket(ticket);
    pi.getSetter().setTicket(ticket);

    // write back the process info
    dao.write(CONSTS_FLOWRET.DAO.PROCESS_INFO + CONSTS_FLOWRET.DAO.SEP + caseId, pi.getDocument());

    ProcessContext pc = null;

    // invoke event handler
    try {
      pc = ProcessContext.forEvent(EventType.ON_PROCESS_REOPEN, this, ".");
      invokeEventHandler(EventType.ON_PROCESS_REOPEN, pc);

      pc = ProcessContext.forEvent(ON_PROCESS_PEND, this, ".");
      invokeEventHandler(ON_PROCESS_PEND, pc);
    }
    catch (Exception e) {
      logger.info("Case id -> " + pi.getCaseId() + ", exception encountered while raising event");
      logger.info("Case id -> " + pi.getCaseId() + ", exception details -> " + e.getMessage());
      logger.info("Case id -> " + pi.getCaseId() + ", exception stack -> " + e.getStackTrace());
    }

    // resume the case if required
    if (pendBeforeResume == false) {
      pc = resumeCase(caseId, true, null);
    }

    return pc;
  }

  private void raiseSlaEvent(EventType event, ProcessContext pc) {
    Document d = null;

    switch (event) {
      case ON_PROCESS_START: {
        Utils.enqueueCaseStartMilestones(pc, slad, slaQm);
        break;
      }

      case ON_PROCESS_REOPEN: {
        Utils.enqueueCaseRestartMilestones(pc, slad, slaQm);
        break;
      }

      case ON_PROCESS_PEND: {
        ExecPath ep = pi.getExecPath(pi.getPendExecPath());
        String prevPendWorkBasket = ep.getPrevPendWorkBasket();
        String pendWorkBasket = ep.getPendWorkBasket();
        String tbcWorkBasket = ep.getTbcSlaWorkBasket();

        if (pi.isPendAtSameStep == false) {
          if (prevPendWorkBasket.equals(tbcWorkBasket)) {
            Utils.dequeueWorkBasketMilestones(pc, prevPendWorkBasket, slad, slaQm);
          }
          else {
            Utils.dequeueWorkBasketMilestones(pc, prevPendWorkBasket, slad, slaQm);
            Utils.dequeueWorkBasketMilestones(pc, tbcWorkBasket, slad, slaQm);
          }
          Utils.enqueueWorkBasketMilestones(pc, SlaMilestoneSetupOn.work_basket_exit, prevPendWorkBasket, slad, slaQm);
          Utils.enqueueWorkBasketMilestones(pc, SlaMilestoneSetupOn.work_basket_entry, pendWorkBasket, slad, slaQm);
          ep.setTbcSlaWorkBasket("");
          break;
        }

        // handling is_pend_at_same_step
        if (prevPendWorkBasket.equals(pendWorkBasket) == false) {
          // means that the first pend at this step was a pend_eor or error pend
          if (ep.getUnitResponseType() == UnitResponseType.ERROR_PEND) {
            if (prevPendWorkBasket.equals(tbcWorkBasket)) {
              Utils.enqueueWorkBasketMilestones(pc, SlaMilestoneSetupOn.work_basket_entry, pendWorkBasket, slad, slaQm);
            }
            else {
              Utils.dequeueWorkBasketMilestones(pc, prevPendWorkBasket, slad, slaQm);
              Utils.enqueueWorkBasketMilestones(pc, SlaMilestoneSetupOn.work_basket_exit, prevPendWorkBasket, slad, slaQm);
              Utils.enqueueWorkBasketMilestones(pc, SlaMilestoneSetupOn.work_basket_entry, pendWorkBasket, slad, slaQm);
            }
          }
          else if (ep.getUnitResponseType() == UnitResponseType.OK_PEND_EOR) {
            if (prevPendWorkBasket.equals(tbcWorkBasket)) {
              Utils.dequeueWorkBasketMilestones(pc, prevPendWorkBasket, slad, slaQm);
              Utils.enqueueWorkBasketMilestones(pc, SlaMilestoneSetupOn.work_basket_exit, prevPendWorkBasket, slad, slaQm);
              Utils.enqueueWorkBasketMilestones(pc, SlaMilestoneSetupOn.work_basket_entry, pendWorkBasket, slad, slaQm);
              ep.setTbcSlaWorkBasket(pendWorkBasket);
            }
            else {
              Utils.dequeueWorkBasketMilestones(pc, prevPendWorkBasket, slad, slaQm);
              Utils.enqueueWorkBasketMilestones(pc, SlaMilestoneSetupOn.work_basket_exit, prevPendWorkBasket, slad, slaQm);

              if (pendWorkBasket.equals(tbcWorkBasket) == false) {
                Utils.dequeueWorkBasketMilestones(pc, tbcWorkBasket, slad, slaQm);
                Utils.enqueueWorkBasketMilestones(pc, SlaMilestoneSetupOn.work_basket_exit, tbcWorkBasket, slad, slaQm);
                Utils.enqueueWorkBasketMilestones(pc, SlaMilestoneSetupOn.work_basket_entry, pendWorkBasket, slad, slaQm);
                ep.setTbcSlaWorkBasket(pendWorkBasket);
              }
              else {
                // nothing to do
              }
            }
          }
          else if (ep.getUnitResponseType() == UnitResponseType.OK_PEND) {
            // this situation cannot happen
          }
        }
        else {
          // nothing to do
        }

        break;
      }

      case ON_PROCESS_RESUME: {
        ExecPath ep = pi.getExecPath(pi.getPendExecPath());
        String pendWorkBasket = ep.getPendWorkBasket();
        UnitResponseType urt = ep.getUnitResponseType();
        if (urt == UnitResponseType.OK_PEND_EOR) {
          // set it to be used in the next pend or when the process moves ahead
          ep.setTbcSlaWorkBasket(pendWorkBasket);
        }
        break;
      }

      case ON_PROCESS_COMPLETE:
        slaQm.dequeueAll(pc);
        break;

    }
  }

}
