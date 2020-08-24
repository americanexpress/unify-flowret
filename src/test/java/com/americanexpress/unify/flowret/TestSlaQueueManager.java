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

import com.americanexpress.unify.jdocs.Document;

/*
 * @author Deepak Arora
 */
public class TestSlaQueueManager implements ISlaQueueManager {

  @Override
  public void enqueue(ProcessContext pc, Document milestones) {
    System.out.println("Received enqueue request. Json below");
    System.out.println(milestones.getPrettyPrintJson());
  }

  @Override
  public void dequeue(ProcessContext pc, String wb) {
    System.out.println("Received dequeue request for workbasket -> " + wb);
  }

  @Override
  public void dequeueAll(ProcessContext pc) {
    System.out.println("Received dequeue all request");
  }

}
