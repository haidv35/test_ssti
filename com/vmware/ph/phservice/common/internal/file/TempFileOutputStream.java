package com.vmware.ph.phservice.common.internal.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class TempFileOutputStream extends OutputStream {
  private static final int DEFAULT_BUFFER_SIZE = 4096;
  
  private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
  
  private final File _destinationFile;
  
  private final OutputStream _outputStream;
  
  private final AtomicBoolean _closed = new AtomicBoolean(false);
  
  private final AtomicBoolean _exceptionThrown = new AtomicBoolean(false);
  
  public TempFileOutputStream(File destinationFile) throws FileNotFoundException {
    this(destinationFile, 4096);
  }
  
  public TempFileOutputStream(File destinationFile, int bufferSize) throws FileNotFoundException {
    this._destinationFile = destinationFile;
    this._outputStream = createOutputStream(getTempFile(), bufferSize);
  }
  
  OutputStream createOutputStream(File file, int bufferSize) throws FileNotFoundException {
    FileOutputStream fileOutputStream = new FileOutputStream(getTempFile());
    if (bufferSize > 0)
      return new BufferedOutputStream(fileOutputStream, bufferSize); 
    return fileOutputStream;
  }
  
  public void write(int b) throws IOException {
    try {
      this._outputStream.write(b);
    } catch (IOException e) {
      this._exceptionThrown.set(true);
      throw e;
    } 
  }
  
  public void write(byte[] b) throws IOException {
    try {
      this._outputStream.write(b);
    } catch (IOException e) {
      this._exceptionThrown.set(true);
      throw e;
    } 
  }
  
  public void flush() throws IOException {
    try {
      this._outputStream.flush();
    } catch (IOException e) {
      this._exceptionThrown.set(true);
      throw e;
    } 
  }
  
  public void close() throws IOException {
    if (!this._closed.compareAndSet(false, true))
      return; 
    this._outputStream.close();
    if (this._exceptionThrown.get())
      return; 
    if (IS_WINDOWS && this._destinationFile.exists())
      this._destinationFile.delete(); 
    boolean renamed = getTempFile().renameTo(this._destinationFile);
    if (!renamed)
      throw new IOException("Failed move/rename from temp file " + 
          getTempFile() + " to destination file " + this._destinationFile); 
  }
  
  File getTempFile() {
    File destinationParent = this._destinationFile.getParentFile();
    String destinationName = this._destinationFile.getName();
    String tempFileName = destinationName + "~";
    return new File(destinationParent, tempFileName);
  }
}
