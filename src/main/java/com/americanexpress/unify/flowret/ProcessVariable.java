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

import com.americanexpress.unify.jdocs.UnifyException;

/*
 * @author Deepak Arora
 */
public class ProcessVariable {

  private String name = null;
  private volatile Object value = null;
  private ProcessVariableType type = null;

  public ProcessVariable(String name, ProcessVariableType type, Object value) {
    validate(type, value);
    this.name = name;
    this.value = value;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public boolean getBoolean() {
    return (Boolean)value;
  }

  public int getInteger() {
    return (Integer)value;
  }

  public Long getLong() {
    return (Long)value;
  }

  protected Object getValue() {
    return value;
  }

  public String getString() {
    return (String)value;
  }

  public String getValueAsString() {
    String s = null;

    switch (type) {
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

  public ProcessVariableType getType() {
    return type;
  }

  public synchronized void setValue(Object value) {
    validate(type, value);
    this.value = value;
  }

  private void validate(ProcessVariableType type, Object value) {
    switch (type) {
      case BOOLEAN: {
        if ((value instanceof Boolean) == false) {
          throw new UnifyException("flowret_err_4", "BOOLEAN");
        }
        break;
      }

      case LONG: {
        if ((value instanceof Long) == false) {
          throw new UnifyException("flowret_err_4", "LONG");
        }
        break;
      }

      case INTEGER: {
        if ((value instanceof Integer) == false) {
          throw new UnifyException("flowret_err_4", "INTEGER");
        }
        break;
      }

      case STRING: {
        if ((value instanceof String) == false) {
          throw new UnifyException("flowret_err_4", "STRING");
        }
        break;
      }
    }
  }

}
