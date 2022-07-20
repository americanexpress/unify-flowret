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

import com.americanexpress.unify.base.ErrorMap;

import java.util.Map;

/*
 * @author Deepak Arora
 */

/**
 * @author daror20
 */
public class ERRORS_FLOWRET extends ErrorMap {

  public static void load() {
    Map<String, String> map = errors;
    map.put("flowret_err_1", "Cannot start a case which is already started. Case Id -> {0}");
    map.put("flowret_err_2", "Could not resume case. No process definition found. Case id -> {0}");
    map.put("flowret_err_3", "Cannot start a case which has already completed. Case id -> {0}");
    map.put("flowret_err_4", "Value object does not conform to process variable data type -> {0}");
    map.put("flowret_err_5", "Unexpected condition encountered. Case id -> {0}");
    map.put("flowret_err_6", "Cannot resume a case that has already completed. Case id -> {0}");
    map.put("flowret_err_7", "Unexpected exception encountered");
    map.put("flowret_err_8", "Route cannot pend upon successful execution");
    map.put("flowret_err_9", "A parallel route cannot have next specified");
    map.put("flowret_err_10", "A dynamic parallel route cannot have branches specified");
    map.put("flowret_err_11", "Journey file for case id {0} does not exist");
    map.put("flowret_err_12", "Case id {0} -> could not find an exec path to pend to. This application cannot be repaired");
    map.put("flowret_err_13", "Case id {0} -> cannot reopen a case which has not yet completed");
    map.put("flowret_err_14", "Case id {0} -> pend work basket cannot be null or empty");
    map.put("flowret_err_15", "Case id {0} -> ticket cannot be null or empty");
    map.put("flowret_err_16", "More than one occurrence(s) of a step name found. Journey name -> {0}, step name -> {1}");
    map.put("flowret_err_17", "More than one occurrence(s) of a name in a branch found. Journey name -> {0}, step name -> {1}, branch name -> {2}");
    map.put("flowret_err_18", "Next step name specified in branch does not exist. Journey name -> {0}, step name -> {1}, branch name -> {2}, next name -> {3}");
    map.put("flowret_err_19", "Step name cannot be end. Journey name -> {0}");
    map.put("flowret_err_20", "Next step name specified in a step is not defined. Journey name -> {0}, step name -> {1}, next name -> {2}");
  }

}
