package org.borganizer.flickr;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.borganizer.conf.PropertiesReader;
import org.borganizer.fs.FolderUploader;
import org.borganizer.fs.IsUploadableFileFilter;
import org.borganizer.remote.ImageStore;

/**
 * @author Utkarsh Srivastava
 */
public class Main {
  public static void main(String[] args) throws Exception {
    System.out.println("Hello World");
    File confFile = new File(System.getProperty("user.home"), ".borganizer");
    Properties properties = PropertiesReader.getPropertiesFromFile(confFile);
    AuthedFlickrClient authedFlickrClient = new AuthedFlickrClient(
        properties.getProperty("apiKey"),
        properties.getProperty("secret"),
        properties.getProperty("token"));

    ExecutorService executorService = Executors.newFixedThreadPool(10);
    ImageStore imageStore = new FlickrImageStore(executorService, authedFlickrClient);
    FolderUploader folderUploader = new FolderUploader(imageStore, new IsUploadableFileFilter());

    folderUploader.upload(new File("/Users/utkarsh/Public/Drop Box"));
  }
}
