package com.aexp.acq.unify.flowret;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtils {

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

}
