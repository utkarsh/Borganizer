package org.borganizer.remote;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author Utkarsh Srivastava
 */
public interface ImageStore {
  /**
   * Does a folder with the given name exist on the store
   */
  public boolean folderExists(String name);

  /**
   * Get the list of files in a folder. It assumes a case-insensitive file system, so returns
   * all names in lower case.
   *
   * @param folder name of folder
   * @return set of file names in folder
   * @throws IOException          if the folder does not exist
   */
  public Set<String> getFiles(String folder) throws IOException;

  /**
   * Upload the given files to a folder of the given name
   */
  public void upload(List<File> files, String folder);

}
