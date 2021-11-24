package com.vmware.ph.phservice.collector.internal.manifest.xml;

import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;
import com.vmware.ph.phservice.collector.internal.manifest.InvalidManifestException;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.MappingParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.obfuscation.ObfuscationRulesParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.RequestSpecParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.data.RequestSpec;
import com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling.RequestScheduleSpecParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling.data.RequestScheduleSpec;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationRule;
import com.vmware.ph.phservice.common.internal.xml.XmlUtils;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class XmlManifestUtils {
  private static final String REQUEST_ELEMENT_NAME = "request";
  
  private static final String REQUEST_SCHEDULE_ELEMENT_NAME = "requestSchedules";
  
  private static final String MANIFEST_RECOMMENDED_PAGE_SIZE_ATTRIBUTE = "recommendedPageSize";
  
  private static final String MAPPING_ELEMENT_NAME = "cdfMapping";
  
  private static final String OBFUSCATION_ELEMENT_NAME = "obfuscation";
  
  public static int getRecommendedPageSize(Document document, int defaultPageSize) {
    int pageSize;
    String pageSizeStr = getRecommendedPageSizeRawValue(document);
    if (!StringUtils.isEmpty(pageSizeStr)) {
      try {
        pageSize = Integer.parseInt(pageSizeStr);
      } catch (NumberFormatException e) {
        pageSize = defaultPageSize;
      } 
    } else {
      pageSize = defaultPageSize;
    } 
    return pageSize;
  }
  
  public static Document getDailySection(String manifest) {
    Document doc = XmlUtils.parseManifestToDoc(manifest);
    Element dailyNode = XmlUtils.findFirstDirectChildElement(doc, "daily");
    if (dailyNode != null) {
      String pageSizeRawValue = getRecommendedPageSizeRawValue(doc);
      if (StringUtils.isNotEmpty(pageSizeRawValue))
        dailyNode.setAttribute("recommendedPageSize", pageSizeRawValue); 
      return XmlUtils.makeDocumentFromNode(dailyNode);
    } 
    return null;
  }
  
  public static Document getNonDailySection(String manifest) {
    Document doc = XmlUtils.parseManifestToDoc(manifest);
    XmlUtils.removeAllLevelOneElements(doc, "daily");
    return doc;
  }
  
  public static <I, O> Mapping<I, O> parseMapping(Document manifestDoc, MappingParser mappingParser) {
    Element mappingElement = getElement(manifestDoc, "cdfMapping", true);
    Mapping<I, O> mapping = null;
    if (mappingElement != null)
      mapping = mappingParser.parse(mappingElement); 
    return mapping;
  }
  
  public static RequestSpec parseRequestSpec(Document manifestDoc, RequestSpecParser requestParser) {
    Element requestElement = getElement(manifestDoc, "request", true);
    RequestSpec requestSpec = requestParser.parse(requestElement);
    return requestSpec;
  }
  
  public static boolean containsRequestScheduleSection(String manifest) {
    Document doc = XmlUtils.parseManifestToDoc(manifest);
    Element requestSchedulingNode = XmlUtils.findFirstDirectChildElement(doc, "requestSchedules");
    return (requestSchedulingNode != null);
  }
  
  public static RequestScheduleSpec parseRequestScheduleSpec(Document manifestDoc, RequestScheduleSpecParser requestScheduleSpecParser) {
    Element schedulingElement = getElement(manifestDoc, "requestSchedules", false);
    RequestScheduleSpec requestScheduleSpec = null;
    if (schedulingElement != null)
      requestScheduleSpec = requestScheduleSpecParser.parse(schedulingElement); 
    return requestScheduleSpec;
  }
  
  public static List<ObfuscationRule> parseObfuscationRules(Document manifestDoc, ObfuscationRulesParser obfuscationRulesParser) {
    Element obfuscationElement = getElement(manifestDoc, "obfuscation", false);
    List<ObfuscationRule> obfuscationRules = null;
    if (obfuscationElement != null)
      obfuscationRules = obfuscationRulesParser.parse(obfuscationElement); 
    return obfuscationRules;
  }
  
  private static Element getElement(Document manifestDoc, String elementName, boolean isElementRequired) {
    NodeList elements = manifestDoc.getElementsByTagNameNS("", elementName);
    if (elements.getLength() < 1 || elements.item(0).getNodeType() != 1) {
      if (isElementRequired)
        throw (InvalidManifestException)ExceptionsContextManager.store(new InvalidManifestException("Collection manifest is missing required element: " + elementName)); 
      return null;
    } 
    return (Element)elements.item(0);
  }
  
  private static String getRecommendedPageSizeRawValue(Document document) {
    return document.getDocumentElement().getAttributeNS(null, "recommendedPageSize");
  }
}
