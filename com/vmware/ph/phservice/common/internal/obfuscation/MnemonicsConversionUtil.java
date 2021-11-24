package com.vmware.ph.phservice.common.internal.obfuscation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MnemonicsConversionUtil {
  private static final int HASH_CHARACTER_SET_NUMBER = 3;
  
  public static Map<String, String> convertToMnemonics(Map<String, String> map, List<String> mnemonicWords, String delimiter) {
    Map<String, String> mnemonics = new HashMap<>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      List<String> mnemonic = getHashToMnemonic(entry
          .getKey(), mnemonicWords, delimiter);
      String mnemonicKey = mnemonic.isEmpty() ? entry.getKey() : buildMnemonicKey(mnemonic);
      mnemonics.put(mnemonicKey, entry.getValue());
    } 
    return mnemonics;
  }
  
  public static List<String> getHashToMnemonic(String hash, List<String> mnemonicWords, String delimiter) {
    List<String> mnemonic = new LinkedList<>();
    try {
      List<List<String>> wordLists = new LinkedList<>();
      for (String hashPart : hash.split(delimiter)) {
        List<String> hashPartWordList = getWordListForHashPart(hashPart, mnemonicWords);
        wordLists.add(hashPartWordList);
      } 
      int wordListMaxSize = 0;
      for (List<String> wordList : wordLists)
        wordListMaxSize = Math.max(wordListMaxSize, wordList.size()); 
      for (int i = 0; i < wordListMaxSize; i++) {
        for (List<String> wordList : wordLists) {
          if (wordList.size() > i)
            mnemonic.add(wordList.get(i)); 
        } 
      } 
    } catch (Exception exception) {}
    return mnemonic;
  }
  
  public static String buildMnemonicKey(List<String> mnemonic) {
    int minNumberOfWords = Math.min(mnemonic.size(), 3);
    StringBuilder mnemonicKeyBuilder = new StringBuilder();
    for (int i = 0; i < minNumberOfWords; i++) {
      mnemonicKeyBuilder.append(mnemonic.get(i));
      if (i < minNumberOfWords - 1)
        mnemonicKeyBuilder.append("-"); 
    } 
    return mnemonicKeyBuilder.toString();
  }
  
  private static List<String> getWordListForHashPart(String hashPart, List<String> mnemonicWords) {
    List<String> wordListForHashPart = new LinkedList<>();
    int maxIndex = Math.max(3, hashPart.length());
    int i;
    for (i = 0; i < maxIndex; i += 3) {
      int startIndex = i;
      int endIndex = Math.min(maxIndex, i + 3);
      int singleWordIndex = Integer.parseInt(hashPart.substring(startIndex, endIndex), 16);
      if (!mnemonicWords.isEmpty()) {
        String singleWord = mnemonicWords.get(singleWordIndex);
        wordListForHashPart.add(singleWord);
      } 
    } 
    return wordListForHashPart;
  }
}
