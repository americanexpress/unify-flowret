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
public class Step extends Unit {

  private String next = null;
  private String componentName = null;
  private String userData = null;

  protected Step(String name, String componentName, String next, String userData) {
    super(name, UnitType.STEP);
    this.next = next;
    this.componentName = componentName;
    this.userData = userData;
  }

  protected String getNext() {
    return next;
  }

  @Override
  protected String getComponentName() {
    return componentName;
  }

  @Override
  protected String getUserData() {
    return userData;
  }

}
