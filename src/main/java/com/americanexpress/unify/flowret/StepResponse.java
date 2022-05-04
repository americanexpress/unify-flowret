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

import com.americanexpress.unify.base.ErrorTuple;

/*
 * @author Deepak Arora
 */
public class StepResponse {

  private UnitResponseType unitResponseType = null;
  private String ticket = "";
  private String workBasket = "";
  private ErrorTuple errorTuple = new ErrorTuple();

  public StepResponse(UnitResponseType unitResponseType, String ticket, String workBasket) {
    init(unitResponseType, ticket, workBasket, new ErrorTuple());
  }

  public StepResponse(UnitResponseType unitResponseType, String ticket, String workBasket, ErrorTuple errorTuple) {
    init(unitResponseType, ticket, workBasket, errorTuple);
  }

  private void init(UnitResponseType unitResponseType, String ticket, String workBasket, ErrorTuple errorTuple) {
    this.unitResponseType = unitResponseType;
    if (ticket != null) {
      this.ticket = ticket;
    }
    if (workBasket != null) {
      this.workBasket = workBasket;
    }
    this.errorTuple = errorTuple;
  }

  public UnitResponseType getUnitResponseType() {
    return unitResponseType;
  }

  public String getWorkBasket() {
    return workBasket;
  }

  public String getTicket() {
    return ticket;
  }

  public ErrorTuple getErrorTuple() {
    return errorTuple;
  }

}
