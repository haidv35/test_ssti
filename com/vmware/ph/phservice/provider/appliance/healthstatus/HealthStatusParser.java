package com.vmware.ph.phservice.provider.appliance.healthstatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HealthStatusParser {
  private static final Log _log = LogFactory.getLog(HealthStatusParser.class);
  
  public static <T> T parseHealthStatus(String healthStatusString, Class<T> healthStatusClass) {
    if (StringUtils.isBlank(healthStatusString) || healthStatusClass == null)
      return null; 
    if (healthStatusString == null)
      return null; 
    T healthStatus = parseXmlHealthStatus(healthStatusString, healthStatusClass);
    if (healthStatus == null)
      healthStatus = parseJsonHealthStatus(healthStatusString, healthStatusClass); 
    if (healthStatus == null && 
      _log.isDebugEnabled())
      _log.debug(
          String.format("Health status \n%s\n could not be parsed.", new Object[] { healthStatusString })); 
    return healthStatus;
  }
  
  private static <T> T parseXmlHealthStatus(String rawHealthStatus, Class<T> healthStatusClass) {
    T healthStatus = null;
    try {
      JAXBContext healthStatusJaxbContext = JAXBContext.newInstance(new Class[] { healthStatusClass });
      Unmarshaller healthStatusUnmarshaller = healthStatusJaxbContext.createUnmarshaller();
      healthStatus = healthStatusClass.cast(healthStatusUnmarshaller.unmarshal(new ByteArrayInputStream(rawHealthStatus
              .getBytes())));
    } catch (JAXBException e) {
      if (_log.isTraceEnabled())
        _log.trace("Failed to parse the XML health status.", e); 
    } 
    return healthStatus;
  }
  
  private static <T> T parseJsonHealthStatus(String rawHealthStatus, Class<T> healthStatusClass) {
    T healthStatus = null;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      healthStatus = (T)objectMapper.readValue(rawHealthStatus, healthStatusClass);
    } catch (IOException e) {
      if (_log.isTraceEnabled())
        _log.trace("Failed to parse the JSON health status.", e); 
    } 
    return healthStatus;
  }
}
