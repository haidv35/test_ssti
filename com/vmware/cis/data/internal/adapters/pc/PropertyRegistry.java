package com.vmware.cis.data.internal.adapters.pc;

import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.vim.binding.impl.vmodl.TypeNameImpl;
import com.vmware.vim.binding.vmodl.TypeName;
import com.vmware.vim.binding.vmodl.query.PropertyCollector;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.Validate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

final class PropertyRegistry {
  private static final String TRAVERSALS_FILE = "/metadata/property_collector/traversals.xml";
  
  private static final String XML_TAG_PROPERTY_DEFINITION = "definition";
  
  private static final String XML_TAG_MODEL = "model";
  
  private static final String XML_TAG_FOREIGN_MODELS = "foreignModels";
  
  private static final String XML_TAG_PROPERTY = "property";
  
  private static final String XML_TAG_FILTER_TRAVERSALS = "filterTraversals";
  
  private static final String XML_TAG_SELECT_TRAVERSALS = "selectTraversals";
  
  private static final String XML_TAG_START = "start";
  
  private static final String XML_TAG_NAME = "name";
  
  private static final String XML_TAG_NEXT_TRAVERSAL = "next";
  
  private static final String XML_TAG_TRAVERSAL_GROUP = "traversal";
  
  private static Map<String, ModelDefinition> _propertiesByModel;
  
  private static final PropertyDefinition SIMPLE_PROPERTY = new PropertyDefinition(null, null, null, null);
  
  static {
    populateTraversals();
  }
  
  public static PropertyDefinition getPropertyDefinition(String model, String property) {
    if (PropertyUtil.isModelKey(property))
      return SIMPLE_PROPERTY; 
    ModelDefinition definition = _propertiesByModel.get(model);
    if (definition == null)
      throw new IllegalArgumentException("Model " + model + " not supported"); 
    PropertyDefinition propDef = definition.getPropertyDefinition(property);
    if (propDef == null)
      return SIMPLE_PROPERTY; 
    return propDef;
  }
  
  static final class ModelDefinition {
    final Map<String, PropertyRegistry.PropertyDefinition> _propertiesByName;
    
    ModelDefinition(Map<String, PropertyRegistry.PropertyDefinition> propertiesByName) {
      assert propertiesByName != null;
      this._propertiesByName = Collections.unmodifiableMap(propertiesByName);
    }
    
    PropertyRegistry.PropertyDefinition getPropertyDefinition(String property) {
      Validate.notNull(property);
      return this._propertiesByName.get(property);
    }
  }
  
  static final class PropertyDefinition {
    private final PropertyCollector.SelectionSpec[] _traversalSpecFilter;
    
    private final PropertyCollector.SelectionSpec[] _traversalSpecSelect;
    
    private final PropertyCollector.PropertySpec[] _propertySpec;
    
    private final Collection<String> _foreignKeyModels;
    
    PropertyDefinition(PropertyCollector.SelectionSpec[] traversalSpecFilter, PropertyCollector.SelectionSpec[] traversalSpecSelect, PropertyCollector.PropertySpec[] propertySpec, Collection<String> foreignKeyModels) {
      this._traversalSpecFilter = traversalSpecFilter;
      this._traversalSpecSelect = traversalSpecSelect;
      this._foreignKeyModels = foreignKeyModels;
      this._propertySpec = propertySpec;
    }
    
    public PropertyCollector.SelectionSpec[] getFilterSpecForPredicate() {
      return this._traversalSpecFilter;
    }
    
    public PropertyCollector.SelectionSpec[] getFilterSpecForSelect() {
      return this._traversalSpecSelect;
    }
    
    public PropertyCollector.PropertySpec[] getPropertySpecForSelect() {
      return this._propertySpec;
    }
    
    public Collection<String> getForeignKeyModels() {
      return this._foreignKeyModels;
    }
  }
  
  private static void populateTraversals() {
    _propertiesByModel = new HashMap<>();
    Map<String, Map<String, PropertyDefinition>> modelsMap = new HashMap<>();
    Element root = load();
    NodeList propertyNodes = root.getElementsByTagName("definition");
    for (int i = 0; i < propertyNodes.getLength(); i++) {
      Element element = (Element)propertyNodes.item(i);
      String model = extractNodeContent(element, "model", true);
      String property = extractNodeContent(element, "property", true);
      Collection<String> foreignKeyModels = null;
      String foreignKeyModelsString = extractNodeContent(element, "foreignModels", false);
      if (foreignKeyModelsString != null)
        foreignKeyModels = Arrays.asList(foreignKeyModelsString.split(",")); 
      NodeList traversals = element.getElementsByTagName("filterTraversals");
      PropertyCollector.SelectionSpec[] traversalsFilter = handleTraversal(traversals, null);
      Map<String, PropertyCollector.PropertySpec> propertySpecs = new LinkedHashMap<>();
      traversals = element.getElementsByTagName("selectTraversals");
      PropertyCollector.SelectionSpec[] traversalsSelect = handleTraversal(traversals, propertySpecs);
      if (traversalsSelect != null) {
        if (foreignKeyModels == null) {
          String message = String.format("No foreign key models specified for property %s of model %s", new Object[] { property, model });
          throw new RuntimeException(message);
        } 
        fillPropertySpecs(foreignKeyModels, propertySpecs);
      } 
      PropertyDefinition definition = new PropertyDefinition(traversalsFilter, traversalsSelect, (PropertyCollector.PropertySpec[])propertySpecs.values().toArray((Object[])new PropertyCollector.PropertySpec[0]), foreignKeyModels);
      Map<String, PropertyDefinition> propertyByName = modelsMap.get(model);
      if (propertyByName == null) {
        propertyByName = new HashMap<>();
        modelsMap.put(model, propertyByName);
      } 
      PropertyDefinition oldDefinition = propertyByName.put(property, definition);
      if (oldDefinition != null) {
        String message = String.format("Multiple definitions for property %s of model %s", new Object[] { property, model });
        throw new RuntimeException(message);
      } 
    } 
    for (Map.Entry<String, Map<String, PropertyDefinition>> entry : modelsMap.entrySet())
      _propertiesByModel.put(entry.getKey(), new ModelDefinition(entry.getValue())); 
  }
  
  private static void fillPropertySpecs(Collection<String> foreignKeyModels, Map<String, PropertyCollector.PropertySpec> propertySpecsByModel) {
    for (String model : foreignKeyModels) {
      if (!propertySpecsByModel.containsKey(model)) {
        PropertyCollector.PropertySpec pSpec = new PropertyCollector.PropertySpec();
        pSpec.setType((TypeName)new TypeNameImpl(model));
        propertySpecsByModel.put(model, pSpec);
      } 
    } 
  }
  
  private static PropertyCollector.SelectionSpec[] handleTraversal(NodeList traversalsRoot, Map<String, PropertyCollector.PropertySpec> propertySpecs) {
    NodeList traversals = extractTraversals(traversalsRoot);
    if (traversals == null)
      return null; 
    if (traversals.getLength() == 0)
      return null; 
    Map<String, List<String>> nextTraversalByName = new HashMap<>();
    Map<String, PropertyCollector.TraversalSpec> traversalsByName = new HashMap<>();
    Queue<String> unprocessedSpecs = new LinkedList<>();
    List<PropertyCollector.TraversalSpec> traversalSpecsRoot = new ArrayList<>(unprocessedSpecs.size());
    Set<String> referencedTraversals = new HashSet<>();
    for (int i = 0; i < traversals.getLength(); i++) {
      Element traversalElement = (Element)traversals.item(i);
      String model = extractNodeContent(traversalElement, "model", true);
      String property = extractNodeContent(traversalElement, "property", true);
      String name = extractNodeContent(traversalElement, "name", true);
      String startString = extractNodeContent(traversalElement, "start", false);
      boolean start = (startString == null) ? false : Boolean.parseBoolean(startString);
      PropertyCollector.TraversalSpec traversalSpec = createTraversalSpec(name, model, property);
      traversalsByName.put(name, traversalSpec);
      if (propertySpecs != null) {
        PropertyCollector.PropertySpec propertySpec = new PropertyCollector.PropertySpec();
        propertySpec.setType((TypeName)new TypeNameImpl(model));
        propertySpec.setPathSet(new String[] { property });
        propertySpecs.put(model, propertySpec);
        traversalSpec.setSkip(Boolean.valueOf(false));
      } 
      if (start) {
        unprocessedSpecs.add(name);
        traversalSpecsRoot.add(traversalSpec);
        referencedTraversals.add(name);
      } 
      String next = extractNodeContent(traversalElement, "next", false);
      if (next == null) {
        nextTraversalByName.put(name, Collections.emptyList());
      } else {
        String[] nextTraversalName = next.split(",");
        nextTraversalByName.put(name, Arrays.asList(nextTraversalName));
      } 
    } 
    while (!unprocessedSpecs.isEmpty()) {
      String specName = unprocessedSpecs.poll();
      PropertyCollector.TraversalSpec spec = traversalsByName.get(specName);
      if (spec == null) {
        String message = String.format("No traversal named %s could be found", new Object[] { specName });
        throw new RuntimeException(message);
      } 
      List<String> nextList = nextTraversalByName.get(specName);
      List<PropertyCollector.SelectionSpec> selectionSpecs = new ArrayList<>(nextList.size());
      for (String next : nextList) {
        if (referencedTraversals.contains(next)) {
          selectionSpecs.add(new PropertyCollector.SelectionSpec(next));
          continue;
        } 
        selectionSpecs.add((PropertyCollector.SelectionSpec)traversalsByName.get(next));
        unprocessedSpecs.add(next);
        referencedTraversals.add(next);
      } 
      spec.setSelectSet(selectionSpecs.<PropertyCollector.SelectionSpec>toArray(new PropertyCollector.SelectionSpec[0]));
    } 
    return traversalSpecsRoot.<PropertyCollector.SelectionSpec>toArray(new PropertyCollector.SelectionSpec[0]);
  }
  
  private static NodeList extractTraversals(NodeList traversalsRoot) {
    if (traversalsRoot.getLength() == 0)
      return null; 
    if (traversalsRoot.getLength() > 2)
      throw new RuntimeException("Found too many traversal structures"); 
    Element root = (Element)traversalsRoot.item(0);
    return root.getElementsByTagName("traversal");
  }
  
  private static PropertyCollector.TraversalSpec createTraversalSpec(String name, String model, String property) {
    PropertyCollector.TraversalSpec traversalSepc = new PropertyCollector.TraversalSpec();
    traversalSepc.setName(name);
    traversalSepc.setType((TypeName)new TypeNameImpl(model));
    traversalSepc.setPath(property);
    traversalSepc.setSkip(new Boolean(false));
    return traversalSepc;
  }
  
  private static String extractNodeContent(Element element, String nodeName, boolean mandatory) {
    Element childNode = null;
    NodeList nodeList = element.getElementsByTagName(nodeName);
    for (int i = 0; i < nodeList.getLength(); i++) {
      if (nodeList.item(i).getParentNode().equals(element)) {
        if (childNode != null) {
          String message = String.format("Element %s cannot have more than 1 child nodes named %s", new Object[] { element
                .getNodeName(), nodeName });
          throw new RuntimeException(message);
        } 
        childNode = (Element)nodeList.item(i);
      } 
    } 
    if (mandatory && childNode == null) {
      String message = String.format("Element %s must have a child node named %s", new Object[] { element
            .getNodeName(), nodeName });
      throw new RuntimeException(message);
    } 
    if (childNode == null)
      return null; 
    return childNode.getTextContent();
  }
  
  private static Element load() {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    InputStream inputStream = PropertyRegistry.class.getResourceAsStream("/metadata/property_collector/traversals.xml");
    try {
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(inputStream);
      Element root = document.getDocumentElement();
      root.normalize();
      return root;
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    } finally {
      try {
        inputStream.close();
      } catch (Exception exception) {}
    } 
  }
}
