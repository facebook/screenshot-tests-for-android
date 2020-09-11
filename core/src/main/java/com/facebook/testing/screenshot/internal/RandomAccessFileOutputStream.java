package com.facebook.testing.screenshot.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

class RandomAccessFileOutputStream extends OutputStream {

  private RandomAccessFile mFile;

  RandomAccessFileOutputStream(RandomAccessFile file) {
    mFile = file;
  }

  @Override
  public void write(int b) throws IOException {
    mFile.write(b);
  }

  @Override
  public void close() throws IOException {
    mFile.close();
  }
}
