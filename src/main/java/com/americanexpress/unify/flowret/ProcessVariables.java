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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * @author Deepak Arora
 */
public class ProcessVariables {

  private Map<String, ProcessVariable> pvMap = new ConcurrentHashMap<>();

  public ProcessVariables() {
  }

  protected ProcessVariables(Map<String, ProcessVariable> pvMap) {
    this.pvMap = pvMap;
  }

  public Boolean getBoolean(String name) {
    ProcessVariable pv = pvMap.get(name);
    if (pv == null) {
      return null;
    }
    else {
      return pv.getBoolean();
    }
  }

  public Integer getInteger(String name) {
    ProcessVariable pv = pvMap.get(name);
    if (pv == null) {
      return null;
    }
    else {
      return pv.getInteger();
    }
  }

  public Long getLong(String name) {
    ProcessVariable pv = pvMap.get(name);
    if (pv == null) {
      return null;
    }
    else {
      return pv.getLong();
    }
  }

  public String getString(String name) {
    ProcessVariable pv = pvMap.get(name);
    if (pv == null) {
      return null;
    }
    else {
      return pv.getString();
    }
  }

  public String getValueAsString(String name) {
    String s = null;
    ProcessVariable pv = pvMap.get(name);

    if (pv == null) {
      return null;
    }
    else {
      Object value = pv.getValue();
      switch (pv.getType()) {
        case BOOLEAN: {
          Boolean b = (Boolean)value;
          s = b.toString();
          break;
        }

        case LONG: {
          Long l = (Long)value;
          s = String.valueOf(l);
          break;
        }

        case INTEGER: {
          Integer i = (Integer)value;
          s = String.valueOf(i);
          break;
        }

        case STRING: {
          s = (String)value;
          break;
        }
      }

      return s;
    }
  }

  public ProcessVariableType getType(String name) {
    ProcessVariable pv = pvMap.get(name);
    if (pv == null) {
      return null;
    }
    else {
      return pv.getType();
    }
  }

  public void setValue(String name, ProcessVariableType type, Object value) {
    ProcessVariable pv = pvMap.get(name);
    if (pv == null) {
      pv = new ProcessVariable(name, type, value);
    }
    else {
      pv.setValue(value);
    }
    pvMap.put(name, pv);
  }

  protected List<ProcessVariable> getListOfProcessVariables() {
    return new ArrayList<>(pvMap.values());
  }

}
