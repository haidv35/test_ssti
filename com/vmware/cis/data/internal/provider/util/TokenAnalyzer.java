package com.vmware.cis.data.internal.provider.util;

import java.util.ArrayList;
import java.util.List;

public class TokenAnalyzer {
  private static final class EscapeAwareIterator {
    private final String _str;
    
    private final int _lastIndex;
    
    private int _currentIndex;
    
    private char _currentChar;
    
    private boolean _isEscaped;
    
    EscapeAwareIterator(String str) {
      this._str = str;
      this._lastIndex = str.length() - 1;
      this._currentIndex = -1;
    }
    
    boolean isEscaped() {
      return this._isEscaped;
    }
    
    char current() {
      if (this._currentIndex < 0)
        throw new IllegalArgumentException("Iterator has not been initialised"); 
      return this._currentChar;
    }
    
    int index() {
      return this._currentIndex;
    }
    
    void advance() {
      if (!hasMore())
        throw new IllegalArgumentException("Iterator out of bounds"); 
      this._currentIndex++;
      this._currentChar = this._str.charAt(this._currentIndex);
      if (this._currentChar == '\\') {
        if (this._currentIndex == this._lastIndex)
          throw new IllegalArgumentException("Invalid escape at index" + this._currentIndex + " in string: " + this._str); 
        this._currentIndex++;
        this._currentChar = this._str.charAt(this._currentIndex);
        this._isEscaped = true;
      } else {
        this._isEscaped = false;
      } 
    }
    
    boolean hasMore() {
      return (this._currentIndex < this._lastIndex);
    }
  }
  
  enum FtsPhraseParseState {
    InsideQuotedFtsToken, InsideNonQuotedFtsToken, OutsideFtsToken;
  }
  
  public static List<String> getFTSTokens(String text) {
    FtsPhraseParseState state = FtsPhraseParseState.OutsideFtsToken;
    List<String> ftsTokens = new ArrayList<>();
    StringBuilder tokenBuilder = new StringBuilder();
    EscapeAwareIterator iterator = new EscapeAwareIterator(text);
    while (iterator.hasMore()) {
      iterator.advance();
      char crtChar = iterator.current();
      switch (state) {
        case OutsideFtsToken:
          if (iterator.isEscaped()) {
            if (!isValidEscapedFTSCharacter(crtChar))
              throw new IllegalArgumentException("Illegal escaped character " + crtChar); 
            if (crtChar != '"')
              tokenBuilder.append('\\'); 
            tokenBuilder.append(crtChar);
            state = FtsPhraseParseState.InsideNonQuotedFtsToken;
            continue;
          } 
          if (crtChar == '"') {
            state = FtsPhraseParseState.InsideQuotedFtsToken;
            continue;
          } 
          if (crtChar != ' ') {
            tokenBuilder.append(crtChar);
            state = FtsPhraseParseState.InsideNonQuotedFtsToken;
          } 
        case InsideNonQuotedFtsToken:
          if (iterator.isEscaped()) {
            if (!isValidEscapedFTSCharacter(crtChar))
              throw new IllegalArgumentException("Illegal escaped character " + crtChar); 
            if (crtChar != '"')
              tokenBuilder.append('\\'); 
            tokenBuilder.append(crtChar);
            continue;
          } 
          if (crtChar == '"') {
            ftsTokens.add(tokenBuilder.toString());
            tokenBuilder = new StringBuilder();
            state = FtsPhraseParseState.InsideQuotedFtsToken;
            continue;
          } 
          if (crtChar == ' ') {
            ftsTokens.add(tokenBuilder.toString());
            tokenBuilder = new StringBuilder();
            state = FtsPhraseParseState.OutsideFtsToken;
            continue;
          } 
          tokenBuilder.append(crtChar);
        case InsideQuotedFtsToken:
          if (iterator.isEscaped()) {
            if (!isValidEscapedFTSCharacter(crtChar))
              throw new IllegalArgumentException("Illegal escaped character " + crtChar); 
            if (crtChar != '"')
              tokenBuilder.append('\\'); 
            tokenBuilder.append(crtChar);
            continue;
          } 
          if (crtChar == '"') {
            ftsTokens.add(tokenBuilder.toString());
            tokenBuilder = new StringBuilder();
            state = FtsPhraseParseState.OutsideFtsToken;
            continue;
          } 
          tokenBuilder.append(crtChar);
      } 
    } 
    if (state == FtsPhraseParseState.InsideQuotedFtsToken)
      throw new IllegalArgumentException("Invalid end state"); 
    if (state == FtsPhraseParseState.InsideNonQuotedFtsToken)
      ftsTokens.add(tokenBuilder.toString()); 
    return ftsTokens;
  }
  
  private static boolean isValidEscapedFTSCharacter(char c) {
    return (c == '*' || c == '\\' || c == '"');
  }
  
  public static List<String> createLikeTokens(String originalToken) {
    List<String> tokens = new ArrayList<>();
    List<Integer> indexes = getWildcardIndexes(originalToken);
    if (indexes.isEmpty()) {
      if (originalToken.isEmpty())
        throw new IllegalArgumentException("Empty quoted token is not supported"); 
      tokens.add("*" + originalToken + "*");
    } else if (indexes.size() == 1) {
      int index = ((Integer)indexes.get(0)).intValue();
      if (index == 0) {
        tokens.add(originalToken);
      } else if (index == originalToken.length() - 1) {
        tokens.add(originalToken);
      } else {
        tokens.add(originalToken.substring(0, index + 1));
        tokens.add(originalToken.substring(index));
      } 
    } else if (indexes.size() == 2) {
      int firstIndex = ((Integer)indexes.get(0)).intValue();
      if (firstIndex != 0)
        throw new IllegalArgumentException("Double wildcards are not supported unless they are first and last position"); 
      int secondIndex = ((Integer)indexes.get(1)).intValue();
      if (secondIndex != originalToken.length() - 1)
        throw new IllegalArgumentException("Double wildcards are not supported unless they are first and last position"); 
      if (secondIndex == 1) {
        tokens.add("*");
      } else {
        tokens.add(originalToken);
      } 
    } else {
      throw new IllegalArgumentException("No more than 2 wildcards are accepted in " + originalToken);
    } 
    return tokens;
  }
  
  private static List<Integer> getWildcardIndexes(String token) {
    List<Integer> indexes = new ArrayList<>();
    EscapeAwareIterator iterator = new EscapeAwareIterator(token);
    while (iterator.hasMore()) {
      iterator.advance();
      if (!iterator.isEscaped() && iterator.current() == '*')
        indexes.add(Integer.valueOf(iterator.index())); 
    } 
    return indexes;
  }
}
