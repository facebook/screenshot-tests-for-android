package com.facebook.testing.screenshot.internal;

import android.util.Log;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

class TarBundleRecorder {

  private static final int TAR_EOF_RECORD_SIZE = 1024;
  private static final int BUFFER_SIZE = 1 << 16; // 64k

  private final String mBundleFileName;
  private final File mDir;
  private TarArchiveOutputStream mTarOutputStream;

  public TarBundleRecorder(String bundleFileName, File dir) {
    mBundleFileName = bundleFileName;
    mDir = dir;
  }

  public void recordFile(String fileName, byte[] content) throws IOException {
    TarArchiveOutputStream tarOutputStream = getOrCreateTarOutputStream();
    TarArchiveEntry entry = new TarArchiveEntry(fileName);
    entry.setSize(content.length);
    tarOutputStream.putArchiveEntry(entry);
    tarOutputStream.write(content);
    tarOutputStream.closeArchiveEntry();
  }

  public void flush() {
    try {
      if (mTarOutputStream != null) {
        mTarOutputStream.close();
      }
    } catch (IOException e) {
      Log.d(AlbumImpl.class.getName(), "Couldn't close zip file.", e);
    }
  }

  private TarArchiveOutputStream getOrCreateTarOutputStream() throws IOException {
    if (mTarOutputStream == null) {
      File file = new File(mDir, mBundleFileName);
      file.createNewFile();
      RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
      long length = randomAccessFile.length();
      if (length > TAR_EOF_RECORD_SIZE) {
        randomAccessFile.seek(length - TAR_EOF_RECORD_SIZE);
      }
      mTarOutputStream = new TarArchiveOutputStream(
          new BufferedOutputStream(
              new RandomAccessFileOutputStream(randomAccessFile),
              BUFFER_SIZE
          )
      );
    }
    return mTarOutputStream;
  }
}
