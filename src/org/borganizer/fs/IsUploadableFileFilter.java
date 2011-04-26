package org.borganizer.fs;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author Utkarsh Srivastava
 */
public class IsUploadableFileFilter implements FileFilter {
  private static Collection<String> allowedExtensions =
      ImmutableList.of("jpg", "mpg", "mpeg", "jpeg");

  public boolean accept(File pathname) {
    final String fileName = pathname.getName().toLowerCase();
    return pathname.isFile() && Iterables.any(allowedExtensions, new Predicate<String>() {
      public boolean apply(String s) {
        return fileName.endsWith("." + s);
      }
    });
  }
}
