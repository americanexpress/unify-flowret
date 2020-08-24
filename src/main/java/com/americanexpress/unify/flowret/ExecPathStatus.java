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

/*
 * @author Deepak Arora
 */
public enum ExecPathStatus {
  // A thread will have this status if
  // it is running or
  // it has pended on a step and terminated
  STARTED,

  // A thread will have this status if
  // if has terminated without a pend on a step
  // this means that either the thread has successfully reached a join condition
  // or a thread which was waiting on child threads has terminated because one of the child threads pended
  COMPLETED
}
