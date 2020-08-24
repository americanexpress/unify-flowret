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

/*
 * @author Deepak Arora
 */
public interface FlowretDao {

  /**
   * Method invoked to write the document to the data store
   *
   * @param key the key used to identify the document
   * @param d   the JDocs document
   */
  public void write(String key, Document d);

  /**
   * The method used to read a document from the data store
   *
   * @param key the key of the document to read
   * @return
   */
  public Document read(String key);

  /**
   * The method used to increment the value of a counter
   *
   * @param key the key for the counter
   * @return the incremented value of the counter
   */
  public long incrCounter(String key);

}
