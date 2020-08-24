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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @author Deepak Arora
 */
public class ProcessDefinition {

  private String name = null;
  private Map<String, Ticket> tickets = new HashMap<>();
  private List<ProcessVariable> processVariables = new ArrayList<>();
  private Map<String, Unit> units = null;

  protected ProcessDefinition() {
    this.units = new HashMap<>();
  }

  protected String getName() {
    return name;
  }

  protected void setName(String name) {
    this.name = name;
  }

  protected void addUnit(Unit unit) {
    units.put(unit.getName(), unit);
  }

  protected Unit getUnit(String name) {
    return units.get(name);
  }

  protected Ticket getTicket(String name) {
    return tickets.get(name);
  }

  protected void setTicket(Ticket ticket) {
    tickets.put(ticket.getName(), ticket);
  }

  protected void setProcessVariables(List<ProcessVariable> processVariables) {
    this.processVariables = processVariables;
  }

  protected List<ProcessVariable> getProcessVariables() {
    return processVariables;
  }

}
