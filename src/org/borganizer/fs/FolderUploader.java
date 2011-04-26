package org.borganizer.fs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.borganizer.remote.ImageStore;

/**
 * @author Utkarsh Srivastava
 */
public class FolderUploader {
  private static FileFilter IS_DIR = new FileFilter() {
    public boolean accept(File pathname) {
      return pathname.isDirectory();
    }
  };

  private ImageStore imageStore;
  private FileFilter isUploadableFileFilter;

  public FolderUploader(ImageStore imageStore, FileFilter uploadableFileFilter) {
    this.imageStore = imageStore;
    isUploadableFileFilter = uploadableFileFilter;
  }

  public int getNumItemsToUpload(File folder) throws IOException {
    checkIsDir(folder);


    Collection<File> filesToUploadAtBaseLevel = getFilesToUploadAtBaseLevel(folder);

    int numToUpload = filesToUploadAtBaseLevel.size();

    for (File file : folder.listFiles(IS_DIR)) {
      numToUpload += getNumItemsToUpload(file);
    }
    return numToUpload;

  }

  private List<File> getFilesToUploadAtBaseLevel(File folder) throws
      IOException {

    File[] uploadableFiles = folder.listFiles(isUploadableFileFilter);
    List<File> filesToUpload = new ArrayList<File>();

    if (imageStore.folderExists(folder.getName())) {
      Set<String> remoteFiles = imageStore.getFiles(folder.getName());
      for (File uploadableFile : uploadableFiles) {
        if (!remoteFiles.contains(uploadableFile.getName().toLowerCase())) {
          filesToUpload.add(uploadableFile);
        }
      }
    } else {
      for (File uploadableFile : uploadableFiles) {
        filesToUpload.add(uploadableFile);
      }
    }

    return filesToUpload;
  }

  public void upload(File folder) throws IOException {
    checkIsDir(folder);

    imageStore.upload(getFilesToUploadAtBaseLevel(folder), folder.getName());

    for (File file : folder.listFiles(IS_DIR)) {
      upload(file);
    }
  }

  private void checkIsDir(File folder) throws IOException {
    if (!folder.isDirectory()) {
      throw new IOException("Folder " + folder.getAbsolutePath() + " is not a directory");
    }
  }
}
