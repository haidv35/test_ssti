package com.vmware.cis.data.internal.adapters.is;

import com.vmware.cis.data.api.ResultSet;
import com.vmware.cis.data.internal.util.PropertyUtil;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final class ResponseConverter {
  private static final Logger _logger = LoggerFactory.getLogger(ResponseConverter.class);
  
  private static PropertyValueUnmarshaller _propertyUnmarshaller = new PropertyValueUnmarshaller();
  
  private static DocumentBuilder _dBuilder;
  
  static {
    try {
      _dBuilder = getDocumentBuilder();
    } catch (Exception e) {
      _logger.error("Failed to create document builder");
    } 
  }
  
  static ResultSet parse(InputStream isStream, String model, List<String> properties, boolean withTotalCount) throws Exception {
    Document response = _dBuilder.parse(isStream);
    Element root = response.getDocumentElement();
    root.normalize();
    NodeList resultNodeList = root.getElementsByTagName("query:result");
    if (resultNodeList.getLength() == 0)
      throw new IllegalArgumentException("Missing node 'query:result' in the response from IS"); 
    Element resultElement = (Element)resultNodeList.item(0);
    resultNodeList = resultElement.getElementsByTagName("query:item");
    ResultSet.Builder resultBuilder = ResultSet.Builder.properties(properties);
    for (int i = 0; i < resultNodeList.getLength(); i++) {
      Element element = (Element)resultNodeList.item(i);
      List<Object> values = new ArrayList(properties.size());
      NodeList nodeListProperties = element.getElementsByTagName("query:properties");
      Element propertiesElement = (Element)nodeListProperties.item(0);
      String mor = extractMorAttribute(element);
      Object key = _propertyUnmarshaller.unmarshal(model, "@modelKey", mor);
      Node crtNode = propertiesElement.getFirstChild();
      for (String property : properties) {
        Object value;
        if (PropertyUtil.isModelKey(property)) {
          value = key;
        } else {
          String nodeName = XQueryUtil.getNodeName(property);
          if (crtNode != null && crtNode.getNodeName().equals(nodeName)) {
            if (_propertyUnmarshaller.hasMultipleCardinality(model, property)) {
              List<Object> list = new ArrayList();
              while (crtNode != null && crtNode.getNodeName().equals(nodeName)) {
                String content = extractMorAttributeOrText((Element)crtNode);
                Object crtValue = _propertyUnmarshaller.unmarshal(model, property, content);
                list.add(crtValue);
                crtNode = crtNode.getNextSibling();
              } 
              value = list;
            } else {
              String content = extractMorAttributeOrText((Element)crtNode);
              value = _propertyUnmarshaller.unmarshal(model, property, content);
              crtNode = crtNode.getNextSibling();
            } 
          } else {
            value = null;
          } 
        } 
        values.add(value);
      } 
      resultBuilder.item(key, values);
    } 
    if (withTotalCount)
      resultBuilder.totalCount(getTotalCount(resultElement)); 
    ResultSet resultSet = resultBuilder.build();
    return resultSet;
  }
  
  private static DocumentBuilder getDocumentBuilder() throws Exception {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    return dBuilder;
  }
  
  private static Integer getTotalCount(Element resultElement) {
    NodeList nodeList = resultElement.getElementsByTagName("query:itemCount");
    if (nodeList.getLength() != 1)
      return Integer.valueOf(0); 
    Element countElement = (Element)nodeList.item(0);
    return Integer.valueOf(Integer.parseInt(countElement.getTextContent()));
  }
  
  private static String extractMorAttribute(Element element) {
    String attribute = element.getAttribute("query:resource");
    if (attribute.isEmpty())
      throw new IllegalArgumentException("No MoR identified in item"); 
    return attribute;
  }
  
  private static String extractMorAttributeOrText(Element element) {
    String attributeValue = element.getAttribute("xlink:href");
    if (!attributeValue.isEmpty())
      return attributeValue; 
    return element.getTextContent();
  }
}
