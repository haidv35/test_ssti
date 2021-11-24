package com.vmware.ph.phservice.common.vsan.filtering;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.vim.vsan.binding.vim.VsanComparator;
import com.vmware.vim.vsan.binding.vim.VsanJsonComparator;
import com.vmware.vim.vsan.binding.vim.VsanJsonFilterRule;
import com.vmware.vim.vsan.binding.vim.VsanNestJsonComparator;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class FilterRule {
  private static final Collection IMPLICIT_PATH_NODES = Collections.unmodifiableList(Arrays.asList((Object[])new String[] { "data", "propSet" }));
  
  private final List<VsanJsonFilterRule> _filteringRules;
  
  private Stack<String> _currentPath = new Stack<>();
  
  private String _parentNode = null;
  
  public FilterRule(List<VsanJsonFilterRule> filteringRules) {
    this._filteringRules = filteringRules;
  }
  
  public void apply(JsonLd jsonLd) {
    if (this._filteringRules.isEmpty())
      return; 
    String filteredJsonString = filterJsonString(jsonLd.toString());
    jsonLd.setJsonString(filteredJsonString);
  }
  
  private String filterJsonString(String jsonString) {
    String result;
    JsonFactory jsonFactory = new JsonFactory((ObjectCodec)new ObjectMapper());
    try(JsonParser jsonParser = jsonFactory.createParser(jsonString); 
        Writer stringWriter = new StringWriter(jsonString.length()); 
        JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter)) {
      while (!jsonParser.isClosed()) {
        JsonToken jsonToken = jsonParser.nextToken();
        if (jsonToken != null)
          handleCurrentJsonToken(jsonParser, jsonGenerator); 
      } 
      jsonGenerator.flush();
      result = stringWriter.toString();
    } catch (Exception e) {
      throw new Error("Could not filter JSON input!", e);
    } 
    return result;
  }
  
  private void handleCurrentJsonToken(JsonParser jsonParser, JsonGenerator jsonGenerator) throws IOException {
    JsonToken currentToken = jsonParser.currentToken();
    switch (currentToken) {
      case START_OBJECT:
        handleStartObject(jsonParser, jsonGenerator);
        return;
      case END_OBJECT:
        handleEndObject(jsonGenerator);
        return;
      case START_ARRAY:
        handleStartArray(jsonGenerator);
        return;
      case END_ARRAY:
        handleEndArray(jsonGenerator);
        return;
      case FIELD_NAME:
        handleFieldName(jsonParser, jsonGenerator);
        return;
      case VALUE_NULL:
      case VALUE_TRUE:
      case VALUE_FALSE:
      case VALUE_EMBEDDED_OBJECT:
      case VALUE_NUMBER_FLOAT:
      case VALUE_NUMBER_INT:
      case VALUE_STRING:
        handleValue(jsonParser, jsonGenerator);
        return;
    } 
    handleDefaultCase(currentToken);
  }
  
  private void handleStartArray(JsonGenerator jsonGenerator) throws IOException {
    jsonGenerator.writeStartArray();
  }
  
  private void handleEndArray(JsonGenerator jsonGenerator) throws IOException {
    jsonGenerator.writeEndArray();
  }
  
  private void handleFieldName(JsonParser jsonParser, JsonGenerator jsonGenerator) throws IOException {
    String fieldName = jsonParser.getCurrentName();
    this._parentNode = fieldName;
    jsonGenerator.writeFieldName(fieldName);
  }
  
  private void handleEndObject(JsonGenerator jsonGenerator) throws IOException {
    removeLastNodeFromPath();
    jsonGenerator.writeEndObject();
  }
  
  private void handleValue(JsonParser jsonParser, JsonGenerator jsonGenerator) throws IOException {
    writeRawValue(jsonParser, jsonGenerator);
  }
  
  private void writeRawValue(JsonParser jsonParser, JsonGenerator jsonGenerator) throws IOException {
    String valueAsString;
    JsonToken valueToken = jsonParser.currentToken();
    switch (valueToken) {
      case VALUE_STRING:
        valueAsString = jsonParser.getValueAsString();
        jsonGenerator.writeString(valueAsString);
        break;
      case VALUE_NUMBER_FLOAT:
      case VALUE_NUMBER_INT:
        jsonGenerator.writeNumber(jsonParser.getValueAsString());
        break;
      case VALUE_TRUE:
      case VALUE_FALSE:
        jsonGenerator.writeBoolean(jsonParser.getBooleanValue());
        break;
      case VALUE_NULL:
        jsonGenerator.writeNull();
        break;
      case VALUE_EMBEDDED_OBJECT:
        jsonGenerator.writeEmbeddedObject(jsonParser.getEmbeddedObject());
        break;
    } 
  }
  
  private void handleDefaultCase(JsonToken currentToken) {
    throw new IllegalStateException("Cannot handle unknown token " + currentToken);
  }
  
  private void handleStartObject(JsonParser jsonParser, JsonGenerator jsonGenerator) throws IOException {
    if (this._parentNode != null)
      this._currentPath.push(this._parentNode); 
    List<VsanJsonFilterRule> currentObjectFilteringRules = getFilterRulesForCurrentObject();
    if (!currentObjectFilteringRules.isEmpty()) {
      filterCurrentObjectTree(currentObjectFilteringRules, jsonParser, jsonGenerator);
    } else {
      jsonGenerator.writeStartObject();
    } 
  }
  
  private List<VsanJsonFilterRule> getFilterRulesForCurrentObject() {
    List<VsanJsonFilterRule> matchingRules = new ArrayList<>();
    for (VsanJsonFilterRule filterRuleSpec : this._filteringRules) {
      String[] filterRulePath = filterRuleSpec.getComparablePath();
      boolean doPathsMatch = true;
      List<String> actualPath = Collections.emptyList();
      if (this._currentPath.size() >= 2)
        actualPath = this._currentPath.subList(IMPLICIT_PATH_NODES.size(), this._currentPath.size()); 
      if (filterRulePath.length == actualPath.size()) {
        for (int i = 0; i < filterRulePath.length; i++) {
          if (!filterRulePath[i].equals(actualPath.get(i)))
            doPathsMatch = false; 
        } 
      } else {
        doPathsMatch = false;
      } 
      if (doPathsMatch)
        matchingRules.add(filterRuleSpec); 
    } 
    return matchingRules;
  }
  
  private static void filterCurrentObjectTree(List<VsanJsonFilterRule> rulesForCurrentPath, JsonParser jsonParser, JsonGenerator jsonGenerator) throws IOException {
    TreeNode currentTreeNode = jsonParser.readValueAsTree();
    for (VsanJsonFilterRule filterRuleSpec : rulesForCurrentPath) {
      if (currentTreeNode.isObject())
        applyComparator(currentTreeNode, filterRuleSpec.getFilterComparator()); 
    } 
    jsonGenerator.writeTree(currentTreeNode);
  }
  
  private void removeLastNodeFromPath() {
    if (!this._currentPath.isEmpty()) {
      this._parentNode = this._currentPath.pop();
    } else {
      this._parentNode = null;
    } 
  }
  
  private static boolean applyComparator(TreeNode currentNode, VsanComparator filterComparator) {
    boolean isMatchFound = true;
    if (filterComparator instanceof VsanNestJsonComparator) {
      VsanNestJsonComparator nestedComparator = (VsanNestJsonComparator)filterComparator;
      String conjoiner = nestedComparator.getConjoiner();
      boolean isConjunction = Conjoiner.AND.toString().equals(conjoiner);
      isMatchFound = isConjunction;
      for (VsanJsonComparator vsanJsonComparator : nestedComparator.getNestedComparators()) {
        if (isConjunction) {
          if (isMatchFound) {
            isMatchFound = applyComparator(currentNode, (VsanComparator)vsanJsonComparator);
          } else {
            return false;
          } 
        } else {
          if (isMatchFound)
            return true; 
          isMatchFound = applyComparator(currentNode, (VsanComparator)vsanJsonComparator);
        } 
      } 
      return isMatchFound;
    } 
    if (filterComparator instanceof VsanJsonComparator)
      isMatchFound = applySimpleFilterComparator((ObjectNode)currentNode, (VsanJsonComparator)filterComparator); 
    return isMatchFound;
  }
  
  private static boolean applySimpleFilterComparator(ObjectNode objectNode, VsanJsonComparator simpleComparator) {
    boolean isMatchFound = false;
    String comparableKey = simpleComparator.getComparableValue().getKey();
    if (Comparator.POP.toString().equals(simpleComparator.getComparator()))
      isMatchFound = popElementFromNode(objectNode, comparableKey); 
    return isMatchFound;
  }
  
  private static boolean popElementFromNode(ObjectNode node, String elementKey) {
    boolean isMatchFound = node.has(elementKey);
    if (isMatchFound)
      node.remove(elementKey); 
    return isMatchFound;
  }
  
  enum Conjoiner {
    AND, OR;
  }
  
  enum Comparator {
    POP;
  }
}
