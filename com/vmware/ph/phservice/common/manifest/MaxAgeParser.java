package com.vmware.ph.phservice.common.manifest;

import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MaxAgeParser {
  private static final String MAX_AGE_REGEX = "max-age=\"(\\d+)\"";
  
  private static final Pattern MAX_AGE_PATTERN = Pattern.compile("max-age=\"(\\d+)\"");
  
  private static final Log _log = LogFactory.getLog(MaxAgeParser.class);
  
  public Optional<Duration> parseDuration(String content) {
    return parse(content).map(Duration::ofSeconds);
  }
  
  private Optional<Long> parse(String content) {
    if (content == null)
      return Optional.empty(); 
    Matcher matcher = MAX_AGE_PATTERN.matcher(content);
    if (matcher.find()) {
      long maxAge = Long.parseLong(matcher.group(1));
      if (_log.isDebugEnabled())
        _log.debug("Parsed maxAge from manifest " + maxAge); 
      return Optional.of(Long.valueOf(maxAge));
    } 
    if (_log.isDebugEnabled())
      _log.debug("Manifest does not contain a max-age attribute or it is not a positive number"); 
    return Optional.empty();
  }
}
