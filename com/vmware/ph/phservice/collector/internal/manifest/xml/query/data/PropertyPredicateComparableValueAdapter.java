package com.vmware.ph.phservice.collector.internal.manifest.xml.query.data;

import com.vmware.ph.phservice.collector.internal.manifest.InvalidManifestException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PropertyPredicateComparableValueAdapter extends XmlAdapter<Object, Object> {
  private static final String LIST_NODE = "list";
  
  private static final String LIST_ITEM_NODE = "item";
  
  public Object unmarshal(Object value) {
    Object unamrshalledValue;
    if (!(value instanceof Node))
      throw new InvalidManifestException("Expected XML Node. Received object of class " + value
          .getClass()); 
    Node comparableValueNode = (Node)value;
    Node childNode = comparableValueNode.getFirstChild();
    if (childNode == null)
      throw new InvalidManifestException("The <comparableValue> node must not be empty."); 
    String childNodeName = childNode.getNodeName();
    if ("list".equalsIgnoreCase(childNodeName)) {
      unamrshalledValue = parseListOfNodes(childNode);
    } else if (childNode.getNodeType() == 3) {
      unamrshalledValue = childNode.getTextContent();
    } else {
      throw new InvalidManifestException(
          String.format("Element with name '%s' is not regcognized.", new Object[] { childNodeName }));
    } 
    return unamrshalledValue;
  }
  
  public Node marshal(Object value) throws Exception {
    return (Node)value;
  }
  
  private static List<String> parseListOfNodes(Node listNode) {
    NodeList itemNodes = listNode.getChildNodes();
    if (itemNodes.getLength() == 0)
      throw new InvalidManifestException(
          String.format("A <%s> node must not be empty.", new Object[] { "list" })); 
    List<String> itemValues = new ArrayList<>();
    for (int i = 0; i < itemNodes.getLength(); i++) {
      Node itemNode = itemNodes.item(i);
      if (1 == itemNode.getNodeType()) {
        validateListItemNode(itemNode);
        itemValues.add(itemNode.getTextContent());
      } 
    } 
    return itemValues;
  }
  
  private static void validateListItemNode(Node node) {
    if (!node.getNodeName().equals("item"))
      throw new InvalidManifestException(
          String.format("'%s' element is not reconginzed! Only <%s> element can be nested inside a <%s>", new Object[] { node.getNodeName(), "item", "list" })); 
  }
}
