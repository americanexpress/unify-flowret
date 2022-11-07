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
import com.americanexpress.unify.base.BlockOnOfferQueue;
import com.americanexpress.unify.base.RejectedItemHandler;
import com.americanexpress.unify.base.UnifyException;
import com.americanexpress.unify.flowret.CONSTS_FLOWRET.DAO;
import com.americanexpress.unify.jdocs.JDocument;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
 * @author Deepak Arora
 */
public class Flowret {

  private static Flowret singleton = new Flowret();
  private int maxThreads = 10;
  private int idleTimeout = 30000;
  private String errorWorkbasket = null;
  private ExecutorService es = null;
  private volatile boolean writeAuditLog = true;
  private volatile boolean writeProcessInfoAfterEachStep = true;

  /**
   * @return an instance of Flowret
   */
  public static Flowret instance() {
    return singleton;
  }

  /**
   * Get the run time service of Flowret
   *
   * @param dao      An object called on by Flowret for persisting the state of the process to the data store
   * @param factory  An object called upon by Flowret to get an instance of an object on which to invoke step and route execute methods
   * @param listener An object on which the application call back events are passed
   * @param slaQm    An object on which the SLA enqueue and dequeue events are passed
   * @return
   */
  public Rts getRunTimeService(FlowretDao dao, ProcessComponentFactory factory, EventHandler listener, ISlaQueueManager slaQm) {
    return new Rts(dao, factory, listener, slaQm);
  }

  /**
   * Get the work manager service of Flowret
   *
   * @param dao   An object called on by Flowret for persisting the state of the process to the data store
   * @param wm    An object whose methods are called by Flowret to do work management functions
   * @param slaQm An object on which the SLA enqueue and dequeue events are passed
   * @return
   */
  public Wms getWorkManagementService(FlowretDao dao, WorkManager wm, ISlaQueueManager slaQm) {
    return new Wms(dao, wm, slaQm);
  }

  private Flowret() {
  }

  /**
   * Method that is called for initializing Flowret
   *
   * @param maxThreads  specifies the number of threads used for parallel processing
   * @param idleTimeout specifies the time out in milliseconds after which parallel processing threads will die out if idle
   * @param typeIdSep   specifies the separator character to use to separate the type and the id in the document name used to persist in the data store
   */
  public static void init(int maxThreads, int idleTimeout, String typeIdSep) {
    init(maxThreads, idleTimeout, typeIdSep, "flowret_error");
  }

  public static void init(int maxThreads, int idleTimeout, String typeIdSep, String errorWorkbasket) {
    Flowret am = instance();
    am.maxThreads = maxThreads;
    am.idleTimeout = idleTimeout;
    BlockOnOfferQueue<Runnable> q = new BlockOnOfferQueue(new ArrayBlockingQueue<>(am.maxThreads * 2));
    am.es = new ThreadPoolExecutor(am.maxThreads, am.maxThreads, am.idleTimeout, TimeUnit.MILLISECONDS, q, new RejectedItemHandler());
    DAO.SEP = typeIdSep;
    am.errorWorkbasket = errorWorkbasket;
    ERRORS_FLOWRET.load();
    String json = BaseUtils.getResourceAsString(Utils.class, "/flowret/models/flowret_journey.json");
    JDocument.loadDocumentModel("flowret_journey", json);
    json = BaseUtils.getResourceAsString(Utils.class, "/flowret/models/flowret_journey_sla.json");
    JDocument.loadDocumentModel("flowret_journey_sla", json);
  }

  /**
   * Method that is used to close Flowret
   */
  public static void close() {
    singleton.es.shutdown();
    try {
      singleton.es.awaitTermination(5, TimeUnit.MINUTES);
    }
    catch (InterruptedException e) {
      // should never happen
      throw new UnifyException("flowret_err_7", e);
    }
    singleton.es = null;
  }

  public int getMaxThreads() {
    return maxThreads;
  }

  public int getIdleTimeout() {
    return idleTimeout;
  }

  protected ExecutorService getExecutorService() {
    return es;
  }

  public void setWriteAuditLog(boolean writeAuditLog) {
    this.writeAuditLog = writeAuditLog;
  }

  public void setWriteProcessInfoAfterEachStep(boolean writeProcessInfoAfterEachStep) {
    this.writeProcessInfoAfterEachStep = writeProcessInfoAfterEachStep;
  }

  public boolean isWriteAuditLog() {
    return writeAuditLog;
  }

  public boolean isWriteProcessInfoAfterEachStep() {
    return writeProcessInfoAfterEachStep;
  }

  public String getErrorWorkbasket() {
    return errorWorkbasket;
  }

}
