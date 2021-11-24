package com.vmware.ph.phservice.common.internal;

import javax.xml.bind.DatatypeConverter;

public class CodecUtil {
  public static byte[] decodeBase64(String base64String) {
    return DatatypeConverter.parseBase64Binary(base64String);
  }
  
  public static String encodeBase64(byte[] base64Binary) {
    return DatatypeConverter.printBase64Binary(base64Binary);
  }
  
  public static byte[] decodeHex(String hexString) {
    return DatatypeConverter.parseHexBinary(hexString);
  }
}
