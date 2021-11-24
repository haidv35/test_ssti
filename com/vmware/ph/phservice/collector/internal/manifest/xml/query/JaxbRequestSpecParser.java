package com.vmware.ph.phservice.collector.internal.manifest.xml.query;

import com.vmware.ph.exceptions.Bug;
import com.vmware.ph.phservice.collector.internal.manifest.InvalidManifestException;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.data.Constraint;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.data.QuerySpec;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.data.RequestSpec;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.w3c.dom.Node;

public final class JaxbRequestSpecParser implements RequestSpecParser {
  private static JAXBContext _instance;
  
  private static JAXBContext getInstance() throws JAXBException {
    if (_instance == null)
      _instance = JAXBContext.newInstance(new Class[] { RequestSpec.class }); 
    return _instance;
  }
  
  public RequestSpec parse(Node xmlNode) throws InvalidManifestException {
    Unmarshaller parser;
    JAXBElement<RequestSpec> parsedManifest;
    Validate.notNull(xmlNode, "An xmlNode is required.");
    try {
      parser = getInstance().createUnmarshaller();
    } catch (JAXBException e) {
      throw new Bug("Internal error: failed to create JAXB Unmarshaller for manifest parsing.", e);
    } 
    try {
      parsedManifest = parser.unmarshal(xmlNode, RequestSpec.class);
    } catch (JAXBException e) {
      throw (InvalidManifestException)ExceptionsContextManager.store(new InvalidManifestException("Failed to parse the request manifest: " + e
            
            .getMessage(), e));
    } 
    RequestSpec parsedRequestSpec = parsedManifest.getValue();
    for (QuerySpec querySpec : parsedRequestSpec.getQueries())
      validateManifestConstraint(querySpec.getConstraint()); 
    return parsedRequestSpec;
  }
  
  private void validateManifestConstraint(Constraint constraint) {
    if (constraint == null)
      throw (InvalidManifestException)ExceptionsContextManager.store(new InvalidManifestException("Invalid query: no constraint defined.")); 
    if (constraint.getClass().equals(Constraint.class))
      requireTargetType(constraint, "constraint"); 
  }
  
  private void requireTargetType(Constraint constraint, String friendlyConstraintType) {
    if (StringUtils.isEmpty(constraint.targetType))
      throw new InvalidManifestException("Invalid query: targetType is required for " + friendlyConstraintType); 
  }
}
