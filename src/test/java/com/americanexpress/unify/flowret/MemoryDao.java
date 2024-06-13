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

import com.americanexpress.unify.jdocs.Document;

import java.util.HashMap;
import java.util.Map;

/*
 * @author Deepak Arora
 */
public class MemoryDao implements FlowretDao {

  private Map<String, Long> counters = new HashMap<>();
  private Map<String, Document> documents = new HashMap<>();

  @Override
  public void write(String key, Document d) {
    documents.put(key, d);
  }

  @Override
  public Document read(String key) {
    return documents.get(key);
  }

  @Override
  public synchronized long incrCounter(String key) {
    Long val = counters.get(key);
    if (val == null) {
      val = 0L;
      counters.put(key, val);
    }
    else {
      val = val + 1;
      counters.put(key, val);
    }
    return val;
  }

  public void delete(String key) {
    documents.remove(key);
  }

  public Map<String, Document> getDocumentMap() {
    return documents;
  }

}
