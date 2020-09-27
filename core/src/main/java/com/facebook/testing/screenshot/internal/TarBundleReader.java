package com.facebook.testing.screenshot.internal;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

class TarBundleReader {

  private String mBundleFileName;
  private final File mDir;
  private static final int BUFFER_SIZE = 1 << 16; // 64k

  TarBundleReader(String bundleFileName, File dir) {
    mBundleFileName = bundleFileName;
    mDir = dir;
  }

  File readFileFromBundle(String fileName) throws IOException {
    File bundle = new File(mDir, mBundleFileName);
    if (!bundle.isFile()) {
      return null;
    }

    TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new FileInputStream(bundle));
    try {
      byte[] buffer = new byte[BUFFER_SIZE];

      TarArchiveEntry entry;
      while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
        if (!fileName.equals(entry.getName())) {
          continue;
        }

        File file = File.createTempFile(fileName, ".tmp");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        try {
          int len;
          while ((len = tarArchiveInputStream.read(buffer)) > 0) {
            fileOutputStream.write(buffer, 0, len);
          }
        } finally {
          fileOutputStream.close();
        }
        return file;
      }
    } finally {
      tarArchiveInputStream.close();
    }
    return null;
  }
}
