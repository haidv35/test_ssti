package com.vmware.ph.phservice.collector.internal.manifest.xml.mapping;

import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.exceptions.Bug;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;
import com.vmware.ph.phservice.collector.internal.data.NamedQueryResultSet;
import com.vmware.ph.phservice.collector.internal.manifest.InvalidManifestException;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data.IndependentResultsMapping;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data.Mappings;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data.ResourceItemToFreeFormDataMapping;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data.ResourceItemToJsonLdMapping;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data.ResourceItemToJsonLdWithAttributesPatternMapping;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data.ResourceItemToPhResourceMapping;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data.ResultSetToPayloadMapping;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.data.VsanMassCollectorToJsonLdMapping;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JaxbMappingParser implements MappingParser {
  private static JAXBContext _instance;
  
  static JAXBContext getInstance() throws JAXBException {
    if (_instance == null)
      _instance = JAXBContext.newInstance(new Class[] { IndependentResultsMapping.class, ResultSetToPayloadMapping.class, ResourceItemToPhResourceMapping.class, ResourceItemToJsonLdMapping.class, ResourceItemToFreeFormDataMapping.class, ResourceItemToJsonLdWithAttributesPatternMapping.class, VsanMassCollectorToJsonLdMapping.class, Mappings.Wrapper.class }); 
    return _instance;
  }
  
  public JaxbMappingParser() {
    try {
      getInstance();
    } catch (JAXBException e) {
      throw new Bug("Bad hardcoded JAXB configuration.", e);
    } 
  }
  
  public Mapping parse(Node xmlNode) {
    try {
      Node element = getFirstChildElement(xmlNode);
      if (element == null) {
        Mapping emptyMapping = IndependentResultsMapping.EMPTY_INDEPENDENT_RESULTS_MAPPING;
        return emptyMapping;
      } 
      Unmarshaller unmarshaller = getInstance().createUnmarshaller();
      Object parsedObj = unmarshaller.unmarshal(element);
      if (!(parsedObj instanceof IndependentResultsMapping))
        throw new InvalidManifestException("Unexpected cdfMapping first child element in XML manifest."); 
      IndependentResultsMapping result = (IndependentResultsMapping)parsedObj;
      Mapping<NamedQueryResultSet, Payload> mapping = result.build();
      return mapping;
    } catch (JAXBException e) {
      throw (InvalidManifestException)ExceptionsContextManager.store(new InvalidManifestException("Failed parsing mapping from XML.", e));
    } 
  }
  
  private Node getFirstChildElement(Node xmlNode) {
    NodeList children = xmlNode.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof org.w3c.dom.Element)
        return children.item(i); 
    } 
    return null;
  }
}
