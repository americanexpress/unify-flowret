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

import com.americanexpress.unify.base.BaseUtils;
import com.americanexpress.unify.jdocs.Document;
import com.americanexpress.unify.jdocs.JDocument;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/*
 * @author Deepak Arora
 */
public class FileDao implements FlowretDao {

  private String filePath = null;
  private Map<String, Long> counters = new HashMap<>();

  public FileDao(String filePath) {
    this.filePath = filePath;
  }

  @Override
  public void write(String key, Document d) {
    FileWriter fw = null;
    try {
      fw = new FileWriter(filePath + key + ".json");
      fw.write(d.getPrettyPrintJson());
      fw.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Document read(String key) {
    InputStream is = null;
    try {
      is = new BufferedInputStream(new FileInputStream(filePath + key + ".json"));
    }
    catch (FileNotFoundException e) {
    }

    Document d = null;
    if (is != null) {
      String json = BaseUtils.getStringFromStream(is);
      d = new JDocument(json);
      try {
        is.close();
      }
      catch (IOException e) {
      }
    }

    return d;
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
    try {
      Files.deleteIfExists(Paths.get(filePath + CONSTS_FLOWRET.DAO.JOURNEY + CONSTS_FLOWRET.DAO.SEP + key + ".json"));
      Files.deleteIfExists(Paths.get(filePath + CONSTS_FLOWRET.DAO.PROCESS_INFO + CONSTS_FLOWRET.DAO.SEP + key + ".json"));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

}
