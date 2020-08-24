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
public class Ticket {

  private String name = null;
  private String step = null;

  protected Ticket(String name, String step) {
    this.name = name;
    this.step = step;
  }

  protected String getName() {
    return name;
  }

  protected String getStep() {
    return step;
  }

}
