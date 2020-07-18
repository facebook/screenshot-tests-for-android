package com.facebook.testing.screenshot.internal;

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

class MetadataRecorder {

  private static final int BUFFER_SIZE = 1 << 16; // 64k
  private static final String FILE_ENCODING = "utf-8";
  private static final String ROOT_TAG_OPEN = "<screenshots>";
  private static final String ROOT_TAG_CLOSE = "</screenshots>";

  private final File mDir;
  private XmlSerializer mXmlSerializer;
  private OutputStream mOutputStream;


  MetadataRecorder(File reportDirectory) {
    mDir = reportDirectory;
  }

  void flush() {
    if (mOutputStream != null) {
      endXml();
    }
  }

  ScreenshotPropertiesRecorder startProperty(String name) throws IOException {
    initXml();
    mXmlSerializer.startTag(null, name);
    return new ScreenshotPropertiesRecorder(name);
  }

  class ScreenshotPropertiesRecorder {

    private final String mRootName;

    ScreenshotPropertiesRecorder(String rootName) {
      mRootName = rootName;
    }

    ScreenshotPropertiesRecorder addProperty(String name, String value) throws IOException {
      addTextNode(name, value);
      return this;
    }

    ScreenshotPropertiesRecorder startProperty(String name) throws IOException {
      mXmlSerializer.startTag(null, name);
      return new ScreenshotPropertiesRecorder(name);
    }

    void endProperty() throws IOException {
      mXmlSerializer.endTag(null, mRootName);
    }
  }

  private void initXml() {
    if (mOutputStream != null) {
      return;
    }

    try {
      RandomAccessFile previousFile = new RandomAccessFile(getMetadataFile(), "rw");
      mOutputStream = new BufferedOutputStream(
          new RandomAccessFileOutputStream(previousFile),
          BUFFER_SIZE
      );
      prepareMetadataFileToAppend(previousFile);
      mXmlSerializer = Xml.newSerializer();
      mXmlSerializer.setOutput(mOutputStream, FILE_ENCODING);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void prepareMetadataFileToAppend(RandomAccessFile file) throws IOException {
    long fileLength = file.length();
    boolean isEmptyFile = fileLength == 0;
    if (isEmptyFile) {
      mOutputStream.write(ROOT_TAG_OPEN.getBytes(FILE_ENCODING));
    } else {
      int closingTagBytesSize = ROOT_TAG_CLOSE.getBytes(FILE_ENCODING).length;
      file.seek(fileLength - closingTagBytesSize);
    }
  }

  private void endXml() {
    try {
      mXmlSerializer.flush();
      mOutputStream.write(ROOT_TAG_CLOSE.getBytes(FILE_ENCODING));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      mOutputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void addTextNode(String name, String value) throws IOException {
    mXmlSerializer.startTag(null, name);
    if (value != null) {
      mXmlSerializer.text(value);
    }
    mXmlSerializer.endTag(null, name);
  }

  File getMetadataFile() {
    return new File(mDir, "metadata.xml");
  }

  private static class RandomAccessFileOutputStream extends OutputStream {

    private RandomAccessFile mFile;

    RandomAccessFileOutputStream(RandomAccessFile file) {
      mFile = file;
    }

    @Override
    public void write(int b) throws IOException {
      mFile.write(b);
    }
  }
}
