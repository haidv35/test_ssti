package com.vmware.ph.phservice.push.telemetry.internal.xml;

import com.vmware.ph.phservice.common.internal.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class TelemetryXmlManifestUtils {
  private static final String TELEMETRY_LEVEL_ELEMENT_NAME = "telemetryLevel";
  
  public static String getTelemetryLevel(String manifest) {
    Document doc = XmlUtils.parseManifestToDoc(manifest);
    Element telemetryLevelParentNode = XmlUtils.findFirstDirectChildElement(doc, "telemetryLevel");
    String telemetryLevel = null;
    if (telemetryLevelParentNode != null) {
      Node telemetryLevelTextNode = telemetryLevelParentNode.getFirstChild();
      if (telemetryLevelTextNode != null && telemetryLevelTextNode
        .getNodeType() == 3)
        telemetryLevel = telemetryLevelTextNode.getTextContent(); 
    } 
    return telemetryLevel;
  }
}
