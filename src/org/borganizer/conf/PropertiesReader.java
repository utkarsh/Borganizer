package org.borganizer.conf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Utkarsh Srivastava
 */
public class PropertiesReader {
  public static Properties getPropertiesFromFile(File file) throws IOException {
    Properties properties = new Properties();
    try {
      properties.load(new BufferedInputStream(
          new FileInputStream(file)));
    } catch (IOException e) {
      System.err.println("Could not parse file " + file);
      throw e;
    }
    return properties;
  }
}
