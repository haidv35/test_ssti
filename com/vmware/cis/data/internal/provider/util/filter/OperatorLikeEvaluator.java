package com.vmware.cis.data.internal.provider.util.filter;

import javax.annotation.Nonnull;
import org.apache.commons.lang.Validate;

public final class OperatorLikeEvaluator {
  private static final char WILDCARD_CHAR = '*';
  
  private static final char ESCAPE_CHAR = '\\';
  
  public enum StringMatchingMode {
    StartsWith, EndsWith, Contains, Exact;
  }
  
  public static final class StringMatchingInfo {
    private final OperatorLikeEvaluator.StringMatchingMode _matchingMode;
    
    private final String _searchText;
    
    StringMatchingInfo(@Nonnull OperatorLikeEvaluator.StringMatchingMode matchingMode, @Nonnull String searchText) {
      this._matchingMode = matchingMode;
      this._searchText = searchText;
    }
    
    public OperatorLikeEvaluator.StringMatchingMode getMode() {
      return this._matchingMode;
    }
    
    public String getSearchText() {
      return this._searchText;
    }
  }
  
  public static boolean evalLike(String propertyValue, String comparableTemplate) {
    Validate.notNull(propertyValue);
    Validate.notNull(comparableTemplate);
    StringMatchingInfo info = analyzeTemplate(comparableTemplate);
    StringMatchingMode mode = info.getMode();
    String searchText = info.getSearchText();
    switch (mode) {
      case StartsWith:
        return propertyValue.startsWith(searchText);
      case EndsWith:
        return propertyValue.endsWith(searchText);
      case Contains:
        return propertyValue.contains(searchText);
      case Exact:
        return propertyValue.equals(searchText);
    } 
    throw new IllegalStateException("Unimplemented string matching mode: " + mode);
  }
  
  public static String toSearchTemplate(String rawText, StringMatchingMode mode) {
    Validate.notEmpty(rawText, "The raw text must not be empty.");
    Validate.notNull(mode, "The matching mode must not be null.");
    StringBuilder escaped = new StringBuilder(rawText.length());
    for (int i = 0; i < rawText.length(); i++) {
      char ch = rawText.charAt(i);
      if (ch == '\\') {
        escaped.append('\\');
        escaped.append(ch);
      } else if (ch == '*') {
        escaped.append('\\');
        escaped.append(ch);
      } else {
        escaped.append(ch);
      } 
    } 
    switch (mode) {
      case StartsWith:
        return escaped.toString() + '*';
      case EndsWith:
        return '*' + escaped.toString();
      case Contains:
        return '*' + escaped.toString() + '*';
      case Exact:
        return escaped.toString();
    } 
    throw new IllegalStateException("Unimplemented string matching mode: " + mode);
  }
  
  public static StringMatchingInfo analyzeTemplate(String template) {
    Validate.notNull(template, "Comparable text template is required");
    if (template.equals("*") || template.equals("**"))
      throw new IllegalArgumentException("Comparable text template must contain non-wildcard characters: " + template); 
    boolean startsWithWildCard = false;
    boolean endsWithWildCard = false;
    StringBuilder unescaped = new StringBuilder(template.length());
    boolean isEscaped = false;
    int lastIndex = template.length() - 1;
    for (int i = 0; i <= lastIndex; i++) {
      char ch = template.charAt(i);
      if (isEscaped) {
        unescaped.append(ch);
        isEscaped = false;
      } else if (ch == '\\') {
        isEscaped = true;
      } else if (ch == '*') {
        if (i == 0) {
          startsWithWildCard = true;
        } else if (i == lastIndex) {
          endsWithWildCard = true;
        } else {
          throw new IllegalArgumentException("Wildcard used in the middle of comparable value");
        } 
      } else {
        unescaped.append(ch);
      } 
    } 
    StringMatchingMode matchingMode = getStringMatchingMode(startsWithWildCard, endsWithWildCard);
    return new StringMatchingInfo(matchingMode, unescaped.toString());
  }
  
  private static StringMatchingMode getStringMatchingMode(boolean startsWithWildCard, boolean endsWithWildCard) {
    if (startsWithWildCard) {
      if (endsWithWildCard)
        return StringMatchingMode.Contains; 
      return StringMatchingMode.EndsWith;
    } 
    if (endsWithWildCard)
      return StringMatchingMode.StartsWith; 
    return StringMatchingMode.Exact;
  }
}
