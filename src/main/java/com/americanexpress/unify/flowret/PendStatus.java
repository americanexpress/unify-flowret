/*
 * Copyright 2025 American Express Travel Related Services Company, Inc.
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

public class PendStatus {

  private String workBasket = null;
  private String execPath = null;

  public PendStatus(String workBasket, String execPath) {
    this.workBasket = workBasket;
    this.execPath = execPath;
  }

  public String getWorkBasket() {
    return workBasket;
  }

  public String getExecPath() {
    return execPath;
  }

}
