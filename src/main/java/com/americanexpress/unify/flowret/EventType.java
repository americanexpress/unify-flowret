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
public enum EventType {

  ON_PROCESS_START,
  ON_PROCESS_RESUME,
  ON_PROCESS_PEND,
  ON_PROCESS_COMPLETE,
  ON_TICKET_RAISED,
  ON_PERSIST,
  ON_PROCESS_REOPEN

}
