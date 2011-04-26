package org.borganizer.flickr;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photosets.Photoset;
import com.aetrion.flickr.uploader.UploadMetaData;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.borganizer.remote.ImageStore;
import org.xml.sax.SAXException;

/**
 * @author Utkarsh Srivastava
 */
public class FlickrImageStore implements ImageStore {
  private static final int PAGE_SIZE = 100;

  private ExecutorService executorService;
  private AuthedFlickrClient authedFlickrClient;
  private Map<String, String> cachedSetNamesToIds;

  public FlickrImageStore(ExecutorService executorService, AuthedFlickrClient authedFlickrClient)
      throws IOException, SAXException, FlickrException {
    this.executorService = executorService;
    this.authedFlickrClient = authedFlickrClient;
    cachedSetNamesToIds = cacheSetNames();
  }

  private Map<String, String> cacheSetNames() throws IOException, SAXException, FlickrException {
    Map<String, String> setNamesToIds = new ConcurrentHashMap<String, String>();

    String currentUserId = authedFlickrClient.getCurrentUser().getId();
    Collection<Photoset> photosets = authedFlickrClient.getFlickr()
        .getPhotosetsInterface().getList(currentUserId).getPhotosets();

    for (Photoset photoset : photosets) {
      setNamesToIds.put(photoset.getTitle().toLowerCase(), photoset.getId());
    }

    return setNamesToIds;
  }

  public boolean folderExists(String name) {
    return cachedSetNamesToIds.containsKey(name.toLowerCase());
  }

  public Set<String> getFiles(String folder) throws IOException {
    int pageNum = 0;
    String context = "retrieving photo list for folder " + folder;
    Set<String> files = new HashSet<String>();
    while (true) {
      try {
        List<Photo> photos = authedFlickrClient.getFlickr().getPhotosetsInterface()
            .getPhotos(cachedSetNamesToIds.get(folder.toLowerCase()), PAGE_SIZE, pageNum);
        for (Photo photo : photos) {
          files.add(photo.getTitle().toLowerCase());
        }
        pageNum++;
        if (photos.size() < PAGE_SIZE) {
          return files;
        }
      } catch (SAXException e) {
        logException(e, context);
      } catch (FlickrException e) {
        logException(e, context);
      }
    }
  }

  public void upload(List<File> files, String folder) {
    if (files.isEmpty()) {
      return;
    }

    Collection<Future<String>> futures = Lists.newArrayList();

    String context = "uploading files to set " + folder;


    if (folderExists(folder)) {
      String photoSetId = cachedSetNamesToIds.get(folder.toLowerCase());
      for (File file : files) {
        futures.add(uploadPhoto(file, photoSetId));
      }
    } else {
      String firstPhotoId;
      try {
        System.out.println("Waiting for " + files.get(0) + " to upload as the first photo"
            + "of set " + folder);
        firstPhotoId = uploadPhoto(files.get(0), null).get();
      } catch (InterruptedException e) {
        return;
      } catch (ExecutionException e) {
        throw new RuntimeException("Unhandled exception: " + e);
      }
      Photoset photoSet;
      while (true) {
        try {
          photoSet = authedFlickrClient.getFlickr().getPhotosetsInterface()
              .create(folder, folder, firstPhotoId);
          break;
        } catch (IOException e) {
          logException(e, context);
        } catch (SAXException e) {
          logException(e, context);
        } catch (FlickrException e) {
          logException(e, context);
        }
      }
      cachedSetNamesToIds.put(folder.toLowerCase(), photoSet.getId());
      for (File file : files.subList(1, files.size())) {
        futures.add(uploadPhoto(file, photoSet.getId()));
      }
    }

    System.out.println("Uploading " + futures.size() + " images to set " + folder);
    int i = 0;
    for (Future<String> future : futures) {
      try {
        future.get();
      } catch (InterruptedException e) {
        return;
      } catch (ExecutionException e) {
        throw new RuntimeException("Unhandled exception: " + e);
      }
      i++;
      System.out.println("Finished uploading " + i + " of " + futures.size() + " images to set "
          + folder);
    }
  }

  private void logException(Exception e, String context) {
    System.err.println(e.getClass().getName() + " while " + context + ", retrying");
  }

  private Future<String> uploadPhoto(final File file, final String photoSetId) {
    Preconditions.checkState(file.exists() && file.isFile());
    return executorService.submit(new Callable<String>() {
      public String call() {
        authedFlickrClient.setCurrentThreadAuth();
        String context = "uploading photo: " + file;
        String photoId = null;
        while (true) {
          try {
            if (photoId == null) {
              photoId = authedFlickrClient.getFlickr().getUploader()
                  .upload(new BufferedInputStream(new FileInputStream(file)),
                      getUploadMetaData(file));
            }
            if (photoSetId != null) {
              authedFlickrClient.getFlickr().getPhotosetsInterface().addPhoto(photoSetId, photoId);
            }

            return photoId;
          } catch (IOException e) {
            logException(e, context);
          } catch (SAXException e) {
            logException(e, context);
          } catch (FlickrException e) {
            logException(e, context);
          }
        }
      }
    });
  }

  private UploadMetaData getUploadMetaData(File file) {
    UploadMetaData uploadMetaData = new UploadMetaData();
    uploadMetaData.setFamilyFlag(false);
    uploadMetaData.setFriendFlag(false);
    uploadMetaData.setPublicFlag(false);
    uploadMetaData.setTitle(file.getName());
    return uploadMetaData;
  }
}
