package com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling;

import com.vmware.ph.exceptions.Bug;
import com.vmware.ph.phservice.collector.internal.manifest.InvalidManifestException;
import com.vmware.ph.phservice.collector.internal.manifest.scheduling.ScheduleIntervalParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling.data.RequestScheduleSpec;
import com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling.data.ScheduleSpec;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

public class JaxbRequestScheduleSpecParser implements RequestScheduleSpecParser {
  private final JAXBContext _jaxbContext;
  
  public JaxbRequestScheduleSpecParser() {
    try {
      this._jaxbContext = JAXBContext.newInstance(new Class[] { RequestScheduleSpec.class, ScheduleSpec.class });
    } catch (JAXBException e) {
      throw new Bug("Bad hardcoded JAXB configuration.", e);
    } 
  }
  
  public RequestScheduleSpec parse(Node xmlNode) throws InvalidManifestException {
    try {
      Unmarshaller unmarshaller = this._jaxbContext.createUnmarshaller();
      RequestScheduleSpec requestScheduleSpec = unmarshaller.<RequestScheduleSpec>unmarshal(xmlNode, RequestScheduleSpec.class).getValue();
      for (ScheduleSpec scheduleSpec : requestScheduleSpec.getSchedules())
        validateRequestScheduleSpec(scheduleSpec); 
      return requestScheduleSpec;
    } catch (JAXBException e) {
      throw (InvalidManifestException)ExceptionsContextManager.store(new InvalidManifestException("Failed when parsing scheduling section from XML.", e));
    } 
  }
  
  static void validateRequestScheduleSpec(ScheduleSpec scheduleSpec) throws InvalidManifestException {
    String scheduleInterval = scheduleSpec.getInterval();
    String retryInterval = scheduleSpec.getRetryInterval();
    int maxRetryCount = scheduleSpec.getMaxRetriesCount();
    validateIntervalFormat(scheduleInterval);
    if (!StringUtils.isBlank(retryInterval)) {
      validateIntervalFormat(retryInterval);
      validateNonNegativeMaxRetryCount(maxRetryCount);
      validateTotalRetriesIntervalDoesntExceedScheduleInterval(scheduleInterval, retryInterval, maxRetryCount);
    } 
  }
  
  private static void validateIntervalFormat(String intervalStr) {
    if (ScheduleIntervalParser.parseIntervalMillis(intervalStr) == 0L)
      throw new InvalidManifestException("The schedule interval string is invalid. Correct format is <schedule interval><time unit specifier>" + intervalStr); 
  }
  
  private static void validateNonNegativeMaxRetryCount(int maxRetriesCount) {
    if (maxRetriesCount < 0)
      throw new InvalidManifestException("The maximum retries count can not be a negative value. Max Retries count is " + maxRetriesCount); 
  }
  
  private static void validateTotalRetriesIntervalDoesntExceedScheduleInterval(String scheduleInterval, String retryInterval, int maxRetriesCount) {
    long totalRetryScheduleIntervalMillis = ScheduleIntervalParser.parseIntervalMillis(retryInterval) * maxRetriesCount;
    long scheduleIntervalMillis = ScheduleIntervalParser.parseIntervalMillis(scheduleInterval);
    if (totalRetryScheduleIntervalMillis >= scheduleIntervalMillis)
      throw new InvalidManifestException(" Total interval of retries exceeds the scheduled interval"); 
  }
}
