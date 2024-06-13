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
import com.americanexpress.unify.flowret.test_singular.TestFlowret;
import com.americanexpress.unify.jdocs.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestManager {

  private static PrintStream previousConsole = null;
  private static ByteArrayOutputStream newConsole = null;
  private static boolean isLoaded = false;

  public static void init(PrintStream previousConsole, ByteArrayOutputStream newConsole, int maxThreads, int idleTimeout) {
    if (TestManager.previousConsole == null) {
      TestManager.previousConsole = previousConsole;
    }

    if (TestManager.newConsole == null) {
      TestManager.newConsole = newConsole;
      System.setOut(new PrintStream(newConsole));
    }

    if (isLoaded == false) {
      isLoaded = true;
      ERRORS_FLOWRET.load();
      Flowret.init(maxThreads, idleTimeout, "-");
      //    Flowret.instance().setWriteAuditLog(false);
      //    Flowret.instance().setWriteProcessInfoAfterEachStep(false);
    }
    else {
      Flowret.close();
      Flowret.init(maxThreads, idleTimeout, "-");
      //    Flowret.instance().setWriteAuditLog(false);
      //    Flowret.instance().setWriteProcessInfoAfterEachStep(false);
    }
  }

  public static void reset() {
    TestManager.newConsole.reset();
  }

  public static void myAssertEquals1(boolean writeToConsole, String testCase, String resourcePath) {
    String s = newConsole.toString();
    String output = trimLines(BaseUtils.getWithoutCarriageReturn(s));
    String expected = BaseUtils.getResourceAsString(TestFlowret.class, resourcePath);
    expected = trimLines(BaseUtils.getWithoutCarriageReturn(expected));
    assertEquals(expected, output);
    previousConsole.println("*********************** Test case successful, test case name -> " + testCase);
    if (writeToConsole == true) {
      previousConsole.println();
      previousConsole.println(s);
      previousConsole.println();
      previousConsole.println();
    }
    previousConsole.flush();
  }

  public static void myAssertEqualsTodo(boolean writeToConsole, String testCase, String resourcePath) {
    String s = newConsole.toString();
    previousConsole.println("*********************** Test case run with no assertion, test case name -> " + testCase);
    if (writeToConsole == true) {
      previousConsole.println();
      previousConsole.println(s);
      previousConsole.println();
      previousConsole.println();
    }
    previousConsole.flush();
  }

  public static void myAssertEquals2(boolean writeToConsole, String testCase, String resourcePath) {
    String s = newConsole.toString();
    String output = getSortedWithoutCrLf(s);
    String expected = BaseUtils.getResourceAsString(TestFlowret.class, resourcePath);
    expected = getSortedWithoutCrLf(expected);
    assertEquals(expected, output);
    previousConsole.println("*********************** Test case successful, test case name -> " + testCase);
    if (writeToConsole == true) {
      previousConsole.println();
      previousConsole.println(s);
      previousConsole.println();
      previousConsole.println();
    }
    previousConsole.flush();
  }

  public static void deleteFiles(String dirPath) {
    try {
      Files.walk(Paths.get(dirPath))
              .filter(Files::isRegularFile)
              .map(Path::toFile)
              .forEach(File::delete);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void writeFiles(boolean writeFiles, String dirPath, Map<String, Document> documents) {
    if (writeFiles == false) {
      return;
    }

    File directory = new File(dirPath);
    if (directory.exists() == false) {
      directory.mkdirs();
    }
    else {
      try {
        Files.walk(Paths.get(dirPath))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .forEach(File::delete);
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }

    for (String key : documents.keySet()) {
      Document d = documents.get(key);
      FileWriter fw = null;
      try {
        String fileName = directory.getCanonicalPath() + "/" + key + ".json";
        fw = new FileWriter(fileName);
        fw.write(d.getPrettyPrintJson());
        fw.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static String getSortedWithoutCrLf(String s) {
    s = s.replaceAll("\r\n", "\n");
    String[] lines = s.split("\n");
    Arrays.sort(lines);
    s = "";
    for (String line : lines) {
      line = line.trim();
      s = s + line;
    }
    return s;
  }

  public static String trimLines(String s) {
    String[] lines = s.split("\n");
    s = "";
    for (String line : lines) {
      line = line.trim();
      s = s + line + "\n";
    }
    return s;
  }

}
