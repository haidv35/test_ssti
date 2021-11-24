package com.vmware.ph.phservice.common.ph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class CompressionUtil {
  public static byte[] getGzippedBytes(byte[] inputData) throws IOException {
    try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); 
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
      gzipOutputStream.write(inputData);
      gzipOutputStream.finish();
      return byteArrayOutputStream.toByteArray();
    } 
  }
}
