package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.internal.file.FileUtil;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileSystemMnemonicWordsBuilder implements Builder<List<String>> {
  private static final Log _log = LogFactory.getLog(FileSystemMnemonicWordsBuilder.class);
  
  private final File _mnemonicsFile;
  
  public FileSystemMnemonicWordsBuilder(File mnemonicsFile) {
    this._mnemonicsFile = mnemonicsFile;
  }
  
  public List<String> build() {
    List<String> mnemonicWords = new LinkedList<>();
    try {
      mnemonicWords = FileUtil.readLinesSafe(this._mnemonicsFile);
    } catch (Exception e) {
      _log.warn("Failed to load the mnemonic words from file.");
    } 
    return mnemonicWords;
  }
}
