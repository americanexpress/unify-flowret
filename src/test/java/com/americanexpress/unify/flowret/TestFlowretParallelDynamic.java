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

import com.americanexpress.unify.jdocs.BaseUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;

/*
 * @author Deepak Arora
 */
public class TestFlowretParallelDynamic {

  private static FileDao dao = null;
  private static ProcessComponentFactory factory = null;
  private static EventHandler handler = null;

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Please specify directory path as the first and only argument e.g. C:/Temp/");
      System.out.println("Do not forget to supply the trailing /");
      System.exit(1);
    }

    String dirPath = args[0];
    String journey = "parallel_dyn_test";
    String json = BaseUtils.getResourceAsString(TestFlowretParallelDynamic.class, "/flowret/" + journey + ".json");

    init(dirPath);
    Rts rts = Flowret.instance().getRunTimeService(dao, factory, handler, null);

    if (new File(dirPath + "flowret_journey-3.json ").exists() == false) {
      rts.startCase("3", json, null, null);
    }
    else {
      rts.resumeCase("3");
    }

    close();
  }

  @BeforeAll
  protected static void init(String dirPath) {
    ERRORS_FLOWRET.load();
    dao = new FileDao(dirPath);
    factory = new TestComponentFactoryParallelSupps();
    handler = new TestHandler();
    Flowret.init(10, 30000, "-");
  }

  @AfterAll
  protected static void close() {
    Flowret.instance().close();
  }

}
