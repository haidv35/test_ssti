package com.vmware.ph.phservice.common.internal.xml;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlUtils {
  private static final String FEATURE_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
  
  private static final String FEATURE_EXTERNAL_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
  
  private static final String FEATURE_NO_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
  
  public static Element findFirstDirectChildElement(Document doc, String elementName) {
    Element docEl = doc.getDocumentElement();
    for (Node node = docEl.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (node.getNodeType() == 1 && 
        Objects.equals(node.getNodeName(), elementName))
        return (Element)node; 
    } 
    return null;
  }
  
  public static void removeAllLevelOneElements(Document doc, String elementName) {
    Element root = doc.getDocumentElement();
    List<Element> toBeRemoved = new ArrayList<>();
    Element docEl = doc.getDocumentElement();
    for (Node node = docEl.getFirstChild(); node != null; node = node.getNextSibling()) {
      if (node.getNodeType() == 1 && 
        Objects.equals(node.getNodeName(), elementName))
        toBeRemoved.add((Element)node); 
    } 
    for (int i = toBeRemoved.size() - 1; i >= 0; i--) {
      Node remove = toBeRemoved.get(i);
      root.removeChild(remove);
    } 
  }
  
  public static Document parseManifestToDoc(String manifest) {
    Reader manifestReader = new StringReader(manifest);
    try {
      DocumentBuilder docBuilder = getDocumentBuilder();
      Document doc = docBuilder.parse(new InputSource(manifestReader));
      return doc;
    } catch (SAXException|java.io.IOException e) {
      throw new RuntimeException("Failed to parse string XML data to XML document: " + e
          .getMessage(), e);
    } 
  }
  
  public static Document makeDocumentFromNode(Node node) {
    DocumentBuilder docBuilder = getDocumentBuilder();
    Document newDocument = docBuilder.newDocument();
    Node importedNode = newDocument.importNode(node, true);
    newDocument.appendChild(importedNode);
    return newDocument;
  }
  
  private static DocumentBuilder getDocumentBuilder() {
    try {
      DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
      xmlFactory.setNamespaceAware(true);
      xmlFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
      xmlFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      xmlFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      xmlFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      DocumentBuilder docBuilder = xmlFactory.newDocumentBuilder();
      return docBuilder;
    } catch (ParserConfigurationException e) {
      throw new RuntimeException("Failed to create XML parser with hardcoded configuration.", e);
    } 
  }
}
