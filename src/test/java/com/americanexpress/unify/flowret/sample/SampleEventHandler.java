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

import com.americanexpress.unify.flowret.EventHandler;
import com.americanexpress.unify.flowret.EventType;
import com.americanexpress.unify.flowret.ProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleEventHandler implements EventHandler {

  private static Logger logger = LoggerFactory.getLogger(SampleEventHandler.class);

  @Override
  public void invoke(EventType event, ProcessContext pc) {
    logger.info("Received event -> {}", event.toString());
  }

}
