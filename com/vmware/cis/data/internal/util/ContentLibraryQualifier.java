package com.vmware.cis.data.internal.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.lang.Validate;

public final class ContentLibraryQualifier {
  public static final char RESOURCE_MODEL_SEPARATOR = '/';
  
  private static final String LIBRARY_TYPE = "com.vmware.content.Library";
  
  private static final String LIBRARY_MODEL = "com.vmware.content.LibraryModel";
  
  private static final String ITEM_TYPE = "com.vmware.content.library.Item";
  
  private static final String ITEM_MODEL = "com.vmware.content.library.ItemModel";
  
  private static final String OVF_TEMPLATE_MODEL = "com.vmware.content.type.ovf.OvfTemplate";
  
  private static final String VM_TEMPLATE_ITEM_MODEL = "com.vmware.vcenter.vm_template.LibraryItems.Info";
  
  private static final Collection<String> ALL_LIBRARY_MODELS = Arrays.asList(new String[] { "com.vmware.content.Library", "com.vmware.content.LibraryModel" });
  
  private static final Collection<String> ALL_ITEM_MODELS = Arrays.asList(new String[] { "com.vmware.content.library.Item", "com.vmware.content.library.ItemModel", "com.vmware.content.type.ovf.OvfTemplate", "com.vmware.vcenter.vm_template.LibraryItems.Info" });
  
  private static final Collection<String> GENERIC_PROPERTIES = (Collection<String>)ImmutableList.builder()
    .add("grantedPrivileges")
    .add("systemPrivileges")
    .add("entityPermission")
    .add("rolesInfo")
    .add("hasPrivileges")
    .add("primaryIconId")
    .add("labelIds")
    .build();
  
  private static final Map<String, Collection<String>> NATIVE_MODEL_PROPERTIES_MAP = getNativeModelPropertiesMap();
  
  public static String qualifyClProperty(String property, Collection<String> resourceModels) {
    Validate.isTrue(isClFromClause(resourceModels), "The resource models collection should be valid from clause.");
    if (PropertyUtil.isSpecialProperty(property))
      return property; 
    for (String str : resourceModels) {
      if (isQualified(property, str))
        return property; 
    } 
    String model = classifyClProperty(property, resourceModels);
    return QualifiedProperty.forModelAndSimpleProperty(model, property).toString();
  }
  
  public static String getClModel(String property, Collection<String> resourceModels) {
    Validate.isTrue(isClFromClause(resourceModels), "The resource models collection should be valid from clause.");
    if (PropertyUtil.isSpecialProperty(property))
      return null; 
    for (String str : resourceModels) {
      if (isQualified(property, str))
        return str; 
    } 
    String model = classifyClProperty(property, resourceModels);
    return model;
  }
  
  public static boolean isClModel(String resourceModel) {
    Validate.notNull(resourceModel);
    return (ALL_LIBRARY_MODELS.contains(resourceModel) || ALL_ITEM_MODELS
      .contains(resourceModel));
  }
  
  public static boolean isClType(String resourceModel) {
    Validate.notNull(resourceModel);
    return ("com.vmware.content.Library".equals(resourceModel) || "com.vmware.content.library.Item".equals(resourceModel));
  }
  
  public static boolean isClFromClause(Collection<String> resourceModels) {
    Validate.notEmpty(resourceModels);
    return (ALL_LIBRARY_MODELS.containsAll(resourceModels) || ALL_ITEM_MODELS
      .containsAll(resourceModels));
  }
  
  private static String classifyClProperty(String propertyPath, Collection<String> resourceModels) {
    String primaryModel = null;
    String secondaryModel = null;
    for (String resourceModel : resourceModels) {
      if ("com.vmware.content.LibraryModel".equals(resourceModel) || "com.vmware.content.library.ItemModel".equals(resourceModel)) {
        primaryModel = resourceModel;
        continue;
      } 
      if (!isSupportedProperty(propertyPath, resourceModel))
        continue; 
      secondaryModel = resourceModel;
    } 
    return (secondaryModel == null) ? primaryModel : secondaryModel;
  }
  
  private static boolean isSupportedProperty(String propertyPath, String model) {
    assert propertyPath != null;
    assert model != null;
    Collection<String> rootProperties = NATIVE_MODEL_PROPERTIES_MAP.get(model);
    if (rootProperties == null)
      return false; 
    String rootProperty = UnqualifiedProperty.getRootProperty(propertyPath);
    return rootProperties.contains(rootProperty);
  }
  
  private static boolean isQualified(String property, String model) {
    assert property != null;
    assert model != null;
    return (property.startsWith(model) && property.length() > model.length() && property
      .charAt(model.length()) == '/');
  }
  
  private static Map<String, Collection<String>> getNativeModelPropertiesMap() {
    return (Map<String, Collection<String>>)ImmutableMap.builder()
      .put("com.vmware.content.Library", getNativeLibraryTypeProperties())
      .put("com.vmware.content.library.Item", getNativeItemTypeProperties())
      .put("com.vmware.content.type.ovf.OvfTemplate", getNativeOvfTemplateProperties())
      .put("com.vmware.vcenter.vm_template.LibraryItems.Info", getNativeVmTemplateItemProperties())
      .build();
  }
  
  private static Collection<String> getNativeLibraryTypeProperties() {
    return (Collection<String>)ImmutableList.builder()
      .add("hasDeletePrivileges")
      .addAll(GENERIC_PROPERTIES)
      .build();
  }
  
  private static Collection<String> getNativeItemTypeProperties() {
    return (Collection<String>)ImmutableList.builder()
      .add("files")
      .add("contentLibraryIsoPath")
      .add("contentLibraryFriendlyIsoPath")
      .addAll(GENERIC_PROPERTIES)
      .build();
  }
  
  private static Collection<String> getNativeOvfTemplateProperties() {
    return (Collection<String>)ImmutableList.builder()
      .add("id")
      .add("isVappTemplate")
      .add("libraryIdParent")
      .add("libraryItemGuestOs")
      .add("networks")
      .add("ovfTemplate")
      .add("storagePolicyGroups")
      .add("vappTemplate")
      .add("version")
      .add("vmCount")
      .add("vmTemplate")
      .build();
  }
  
  private static Collection<String> getNativeVmTemplateItemProperties() {
    return (Collection<String>)ImmutableList.builder()
      .add("guestOS")
      .add("guestOsDescription")
      .add("cpu")
      .add("memory")
      .add("vmHomeStorage")
      .add("disks")
      .add("nics")
      .add("vmTemplate")
      .build();
  }
}
