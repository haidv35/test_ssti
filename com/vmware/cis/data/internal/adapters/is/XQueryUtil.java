package com.vmware.cis.data.internal.adapters.is;

import com.vmware.vim.binding.vmodl.ManagedObjectReference;

final class XQueryUtil {
  static final String XML_TAG_RESULT = "query:result";
  
  static final String XML_TAG_PROPERTIES = "query:properties";
  
  static final String XML_TAG_ITEM = "query:item";
  
  static final String XML_TAG_ITEM_COUNT = "query:itemCount";
  
  static final String XML_ATTR_MODEL_KEY = "query:resource";
  
  static final String XML_ATTR_MOID = "xlink:href";
  
  static final String XQUERY_FOREIGN_KEY = "@qs:resource";
  
  static final String XQUERY_KEY = "@qs:id";
  
  static final String XQUERY_CONSTANT_FALSE = "false()";
  
  static final String XQUERY_CONSTANT_TRUE = "true()";
  
  static final String XQUERY_LET = "let ";
  
  static final String XQUERY_ASSIGN = " := ";
  
  static final String XQUERY_TARGETSET = "$targetSet";
  
  static final String XQUERY_TARGETSET_ROOT = "/";
  
  static final String XQUERY_TARGETSET_ALL = "let $targetSet := /";
  
  static final String XQUERY_TARGETSET_OP_UNION = " union ";
  
  static final String XQUERY_TARGETSET_OP_INTERSECT = " intersect ";
  
  static final String XQUERY_OP_OR = " or ";
  
  static final String XQUERY_OP_AND = " and ";
  
  static final String XQUERY_SELECT_ROOT = "local:product('vpx')";
  
  static final String STATIC_NAMESPACE_DECLS = "declare namespace vim25=\"urn:vim25\";\ndeclare namespace qs=\"urn:vmware:queryservice\";\ndeclare namespace query=\"query\";\ndeclare default element namespace \"urn:vim25\";\ndeclare namespace vapi=\"urn:vim25\";\ndeclare namespace xlink=\"http://www.w3.org/1999/xlink\";\ndeclare option xhive:fts-analyzer-class \"com.vmware.vim.query.server.store.impl.CaseInsensitiveWhitespaceAnalyzer\";\n";
  
  static final String EMPTY_SORT = "let $resultSortedFlag := false()\nlet $resultSorted := <query:resultSorted>{$resultSortedFlag}</query:resultSorted>\n";
  
  static final String SORT_BY_KEY_TEMPLATE = "let $orderedTargetSet := for $target in $targetSet\n\n  order by fn:string($target[1]/@qs:resource) %s\n  return $target\n\nlet $resultSortedFlag := true()\nlet $resultSorted := <query:resultSorted>{$resultSortedFlag}</query:resultSorted>\n\nlet $targetSet := $orderedTargetSet\n";
  
  static final String NO_SELECT_PROPERTIES = "let $items := ()\nlet $itemCount := <query:itemCount>{count(($targetSet))}</query:itemCount>\n";
  
  static final String EXTRACT_PROPERTIES_START = "let $items := for $target in if ($resultSortedFlag) then $targetSet[fn:position()>=%d and fn:position()<=%d] else $targetSet\nlet $resourceId := $target/@qs:resource\nlet $targetDocId := $target/@qs:id\nreturn <query:item query:provider=\"{$targetDocId}\" query:resource=\"{$resourceId}\">\n<query:properties>\n";
  
  static final String EXTRACT_PROPERTIES_END = "</query:properties>\n</query:item>\nlet $itemCount := <query:itemCount>{count(($targetSet))}</query:itemCount>\n";
  
  static String RETURN_CLAUSE = "return <query:result xmlns:qs=\"urn:vmware:queryservice\" xmlns:query=\"query\" xmlns:vapi=\"urn:vim25\" xmlns=\"urn:vim25\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">{$itemCount,$items,$resultSorted}</query:result>";
  
  static final String RETURN_NODE_START = "  {local:returnnode($target,";
  
  static final String RETURN_NODE_END = ")}\n";
  
  static final String RETURN_NODE_PROPERTY_START_COMPOSITE = "$target/vim25:";
  
  static final String RETURN_NODE_PROPERTY_START_SIMPLE = "$target/";
  
  static final String PROPERTY_SLASH_REPLACER = "/vim25:";
  
  private static final String SEPARATOR = ":";
  
  private static final String URN_VMOMI = "urn:vmomi";
  
  private static final String SEARCH_AT = "@";
  
  private static final String REPLACE_AT = "_at_";
  
  private static final String SEARCH_SLASH = "/";
  
  private static final String REPLACE_SLASH = "_sl_";
  
  static String getNodeName(String property) {
    return property.replaceAll("@", "_at_")
      .replaceAll("/", "_sl_");
  }
  
  static String fromMoR(Object value) {
    if (!(value instanceof ManagedObjectReference)) {
      String message = String.format("Value '%s' of type '%s' not an instance of ManagedObjectReference", new Object[] { value
            .toString(), value
            .getClass().getName() });
      throw new IllegalArgumentException(message);
    } 
    ManagedObjectReference mor = (ManagedObjectReference)value;
    StringBuilder builder = new StringBuilder();
    builder.append("urn:vmomi");
    builder.append(":");
    builder.append(mor.getType());
    builder.append(":");
    builder.append(mor.getValue());
    builder.append(":");
    builder.append(mor.getServerGuid());
    return builder.toString();
  }
  
  static ManagedObjectReference toMoR(String value) {
    if (!value.startsWith("urn:vmomi"))
      throw new IllegalArgumentException("Unexpected prefix for " + value); 
    String subString = value.substring("urn:vmomi".length() + 1);
    String[] split = subString.split(":");
    if (split.length != 3)
      throw new IllegalArgumentException("Could not extract mor object from " + value); 
    return new ManagedObjectReference(split[0], split[1], split[2]);
  }
}
