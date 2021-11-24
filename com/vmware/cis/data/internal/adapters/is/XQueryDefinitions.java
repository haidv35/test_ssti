package com.vmware.cis.data.internal.adapters.is;

import com.vmware.cis.data.api.QuerySchema;
import com.vmware.cis.data.internal.util.PropertyUtil;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.Validate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

final class XQueryDefinitions {
  private static final String XML_TAG_PROPERTY_DEFINITION = "definition";
  
  private static final String XML_TAG_MODEL = "model";
  
  private static final String XML_TAG_PROPERTY = "property";
  
  private static final String XML_TAG_TYPE = "type";
  
  private static final String XML_TAG_SELECT_EXPRESSION = "selectExpression";
  
  private static final String XML_TAG_FILTER_EXPRESSION = "filterExpression";
  
  private static final String XML_TAG_FILTER_TEMPLATE = "filterTemplate";
  
  private static final String XML_TAG_MULTIPLE_CARDINALITY = "multipleCardinality";
  
  private static final String XML_TAG_FUNCTION_DEFINITION = "definition";
  
  private static final String XML_TAG_NAME = "name";
  
  private static final String XML_TAG_EXPRESSION = "expression";
  
  private static final String FILE_NAME_PROPERTY_DEFINITIONS = "properties";
  
  private static final String FILE_NAME_FUNCTION_DEFINITIONS = "functions";
  
  private static final String FILE_PATH_TEMPLATE = "/metadata/inventory_service/%s.xml";
  
  private static final Pattern XQUERY_FUNCTION_PATTERN = Pattern.compile("local:[A-Za-z_]([A-Za-z_0-9]|-)*\\(");
  
  private static final PropertyDefinition DEFAULT_STRING_DEFINITION = new PropertyDefinition(QuerySchema.PropertyType.STRING, null, null, null, 
      Boolean.valueOf(false));
  
  private static final PropertyDefinition DEFAULT_KEY_DEFINITION = new PropertyDefinition(QuerySchema.PropertyType.ID, null, null, null, 
      Boolean.valueOf(false));
  
  private static final String KEY_LOCAL_RETURNNODE = "local:returnnode";
  
  private static final String KEY_LOCAL_WHICH_ONE_IS_ANCESTOR = "local:whichOneIsAncestor";
  
  private static final String KEY_LOCAL_PRODUCT = "local:product";
  
  private static Map<String, FunctionDefinition> _functionsByName;
  
  private static Map<String, ModelDefinition> _modelsByName;
  
  static {
    loadFunctions();
    loadProperties();
  }
  
  static Set<String> getRequiredFunctions() {
    Set<String> functionSet = new HashSet<>();
    functionSet.add("local:whichOneIsAncestor");
    functionSet.add("local:returnnode");
    functionSet.add("local:product");
    return functionSet;
  }
  
  static String getFunctionExpression(String functionName) {
    FunctionDefinition functionDefinition = getFunctionDefinition(functionName);
    return functionDefinition.getBody();
  }
  
  static Set<String> getAllDependentFunctions(String functionName) {
    Set<String> checkedFunctions = new HashSet<>();
    Set<String> uncheckedFunctions = new HashSet<>();
    uncheckedFunctions.add(functionName);
    while (!uncheckedFunctions.isEmpty()) {
      Set<String> newFunctions = new HashSet<>();
      for (String crtFunctionName : uncheckedFunctions) {
        checkedFunctions.add(crtFunctionName);
        FunctionDefinition definition = getFunctionDefinition(crtFunctionName);
        for (String newFunction : definition.getDependencies()) {
          if (checkedFunctions.contains(newFunction) || uncheckedFunctions
            .contains(newFunction))
            continue; 
          newFunctions.add(newFunction);
        } 
      } 
      uncheckedFunctions = newFunctions;
    } 
    return checkedFunctions;
  }
  
  static PropertyDefinition getPropertyDefinition(String model, String property) {
    if (PropertyUtil.isModelKey(property))
      return DEFAULT_KEY_DEFINITION; 
    ModelDefinition modelDefinition = _modelsByName.get(model);
    if (modelDefinition == null)
      return DEFAULT_STRING_DEFINITION; 
    PropertyDefinition propertyDefinition = modelDefinition.getPropertyDefinition(property);
    return (propertyDefinition == null) ? DEFAULT_STRING_DEFINITION : propertyDefinition;
  }
  
  private static FunctionDefinition getFunctionDefinition(String functionName) {
    FunctionDefinition functionDefinition = _functionsByName.get(functionName);
    if (functionDefinition == null)
      throw new IllegalArgumentException("xQuery function definition not found for '" + functionName + "'"); 
    return functionDefinition;
  }
  
  private static void loadProperties() {
    _modelsByName = new HashMap<>();
    Element root = load("properties");
    Map<String, Map<String, PropertyDefinition>> modelsMap = new HashMap<>();
    NodeList propertyNodes = root.getElementsByTagName("definition");
    for (int i = 0; i < propertyNodes.getLength(); i++) {
      Element element = (Element)propertyNodes.item(i);
      String model = extractNodeContent(element, "model", true);
      String property = extractNodeContent(element, "property", true);
      String type = extractNodeContent(element, "type", true);
      QuerySchema.PropertyType propertyType = QuerySchema.PropertyType.valueOf(type.toUpperCase());
      String selectExpression = extractNodeContent(element, "selectExpression", false);
      String filterExpression = extractNodeContent(element, "filterExpression", false);
      String filterTemplate = extractNodeContent(element, "filterTemplate", false);
      String multipleCardinalityString = extractNodeContent(element, "multipleCardinality", false);
      boolean multipleCardinality = (multipleCardinalityString == null) ? false : Boolean.parseBoolean(multipleCardinalityString);
      PropertyDefinition propertyDefinition = new PropertyDefinition(propertyType, selectExpression, filterExpression, filterTemplate, Boolean.valueOf(multipleCardinality));
      Map<String, PropertyDefinition> propertyMap = modelsMap.get(model);
      if (propertyMap == null) {
        propertyMap = new HashMap<>();
        modelsMap.put(model, propertyMap);
      } 
      PropertyDefinition oldDefinition = propertyMap.put(property, propertyDefinition);
      if (oldDefinition != null) {
        String message = String.format("Multiple definitions for property %s of model %s", new Object[] { property, model });
        throw new IllegalArgumentException(message);
      } 
    } 
    for (Map.Entry<String, Map<String, PropertyDefinition>> e : modelsMap.entrySet())
      _modelsByName.put(e.getKey(), new ModelDefinition(e.getValue())); 
  }
  
  private static void loadFunctions() {
    _functionsByName = new HashMap<>();
    Element root = load("functions");
    NodeList propertyNodes = root.getElementsByTagName("definition");
    for (int i = 0; i < propertyNodes.getLength(); i++) {
      Element element = (Element)propertyNodes.item(i);
      String name = extractNodeContent(element, "name", true);
      String expression = extractNodeContent(element, "expression", true);
      _functionsByName.put(name, new FunctionDefinition(expression));
    } 
  }
  
  private static String extractNodeContent(Element element, String nodeName, boolean mandatory) {
    NodeList nodeList = element.getElementsByTagName(nodeName);
    if (nodeList.getLength() > 1) {
      String message = String.format("Element %s cannot have more than 1 child nodes named %s", new Object[] { element
            .getNodeName(), nodeName });
      throw new IllegalArgumentException(message);
    } 
    if (mandatory && nodeList.getLength() != 1) {
      String message = String.format("Element %s must have a child child node named %s", new Object[] { element
            .getNodeName(), nodeName });
      throw new IllegalArgumentException(message);
    } 
    if (nodeList.getLength() == 0)
      return null; 
    Element childNode = (Element)nodeList.item(0);
    return childNode.getTextContent();
  }
  
  static final class FunctionDefinition {
    private final String _body;
    
    private final Set<String> _dependencies;
    
    FunctionDefinition(String body) {
      this._body = body;
      this._dependencies = XQueryDefinitions.extractFunctions(body);
    }
    
    String getBody() {
      return this._body;
    }
    
    Collection<String> getDependencies() {
      return this._dependencies;
    }
  }
  
  static final class ModelDefinition {
    Map<String, XQueryDefinitions.PropertyDefinition> _propertiesByName;
    
    ModelDefinition(Map<String, XQueryDefinitions.PropertyDefinition> propertiesByName) {
      assert propertiesByName != null;
      this._propertiesByName = Collections.unmodifiableMap(propertiesByName);
    }
    
    XQueryDefinitions.PropertyDefinition getPropertyDefinition(String property) {
      Validate.notNull(property);
      return this._propertiesByName.get(property);
    }
  }
  
  static final class PropertyDefinition {
    private final QuerySchema.PropertyType _type;
    
    private final String _selectExpression;
    
    private final String _filterExpression;
    
    private final String _filterTemplate;
    
    private final Set<String> _selectFunctions;
    
    private final Set<String> _filterFunctions;
    
    private final boolean _multipleCardinality;
    
    PropertyDefinition(QuerySchema.PropertyType type, String selectExpression, String filterExpression, String filterTemplate, Boolean multipleCardinality) {
      Validate.notNull(type);
      this._type = type;
      this._selectExpression = selectExpression;
      this._filterExpression = filterExpression;
      this._filterTemplate = filterTemplate;
      this._selectFunctions = XQueryDefinitions.extractFunctions(selectExpression);
      this
        ._filterFunctions = (filterTemplate != null) ? XQueryDefinitions.extractFunctions(filterTemplate) : XQueryDefinitions.extractFunctions(filterExpression);
      this._multipleCardinality = multipleCardinality.booleanValue();
    }
    
    QuerySchema.PropertyType getType() {
      return this._type;
    }
    
    String getSelectExpression() {
      return this._selectExpression;
    }
    
    String getFilterExpression() {
      return this._filterExpression;
    }
    
    String getFilterTemplate() {
      return this._filterTemplate;
    }
    
    Set<String> getSelectFunctions() {
      return this._selectFunctions;
    }
    
    Set<String> getFilterFunctions() {
      return this._filterFunctions;
    }
    
    boolean hasMultipleCardinality() {
      return this._multipleCardinality;
    }
  }
  
  private static Set<String> extractFunctions(String expression) {
    if (expression == null)
      return Collections.emptySet(); 
    Set<String> set = new HashSet<>();
    Matcher match = XQUERY_FUNCTION_PATTERN.matcher(expression);
    while (match.find()) {
      String functionName = getFunctionName(match.group());
      set.add(functionName);
    } 
    return Collections.unmodifiableSet(set);
  }
  
  private static String getFunctionName(String match) {
    assert match != null;
    String openBracket = "(";
    int index = match.indexOf("(");
    if (index < 0)
      throw new IllegalArgumentException(
          String.format("Invalid query. XQuery function with name {0} is not recognized.", new Object[] { match })); 
    return match.substring(0, index).trim();
  }
  
  private static Element load(String resourceFileName) {
    String resourceId = String.format("/metadata/inventory_service/%s.xml", new Object[] { resourceFileName });
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    InputStream inputStream = XQueryDefinitions.class.getResourceAsStream(resourceId);
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
