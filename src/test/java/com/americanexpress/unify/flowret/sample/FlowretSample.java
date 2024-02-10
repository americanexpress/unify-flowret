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

import com.americanexpress.unify.base.BaseUtils;
import com.americanexpress.unify.base.UnifyException;
import com.americanexpress.unify.flowret.ERRORS_FLOWRET;
import com.americanexpress.unify.flowret.Flowret;
import com.americanexpress.unify.flowret.Rts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FlowretSample {

  private static Logger logger = LoggerFactory.getLogger(FlowretSample.class);

  // this is windows path - change as required - also change on mac as required
  public static final String DIR_PATH = "C:/temp/flowret/";

  public static void main(String[] args) {
    // clear out base directory
    deleteFiles(DIR_PATH);

    // initialize flowret
    ERRORS_FLOWRET.load();
    Flowret.init(10, 30000, "-");
    Flowret.instance().setWriteAuditLog(true);
    Flowret.instance().setWriteProcessInfoAfterEachStep(true);

    // wire up the flowret runtime service
    SampleFlowretDao dao = new SampleFlowretDao(DIR_PATH);
    SampleComponentFactory factory = new SampleComponentFactory();
    SampleEventHandler handler = new SampleEventHandler();
    Rts rts = Flowret.instance().getRunTimeService(dao, factory, handler, null);

    // get the process to run
    String json = BaseUtils.getResourceAsString(FlowretSample.class, "/flowret/sample/order_part.json");

    // start the process - 1 is the case id
    rts.startCase("1", json, null, null);

    // resume if we had pended somewhere till an exception is thrown
    try {
      while (true) {
        logger.info("\n");
        rts.resumeCase("1");
      }
    }
    catch (UnifyException e) {
      logger.error("Exception -> " + e.getMessage());
    }

  }

  public static void deleteFiles(String dirPath) {
    try {
      Files.walk(Paths.get(dirPath))
              .filter(Files::isRegularFile)
              .map(Path::toFile)
              .forEach(File::delete);
    }
    catch (IOException e) {
      logger.error("Exception -> " + e.getMessage());
    }
  }

}
