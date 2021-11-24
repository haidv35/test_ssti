package com.vmware.ph.phservice.cloud.health.dataapp;

import com.vmware.ph.phservice.cloud.health.HealthSystem;
import com.vmware.ph.phservice.common.internal.i18n.LocalizedMessageProvider;
import com.vmware.vim.binding.vmodl.DynamicProperty;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthGroup;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthTest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class HealthGroupsCategoryHelper {
  public static final String ONLINE_HEALTH_AVAILABILITY_CATEGORY_NAME_KEY = "com.vmware.adc.health.category.online.health.availability.name";
  
  public static final String CATEGORY_PROPERTY_NAME = "category";
  
  static final String GENERAL_HEALTH_CATEGORY_NAME_KEY = "com.vmware.adc.health.category.general.health.checks.name";
  
  static final String ADC_HEALTH_PREFIX = "com.vmware.adc.";
  
  private final LocalizedMessageProvider _localizedMessageProvider;
  
  public HealthGroupsCategoryHelper(LocalizedMessageProvider localizedMessageProvider) {
    this._localizedMessageProvider = localizedMessageProvider;
  }
  
  public VsanClusterHealthGroup[] buildCategoryHealthGroupsFromHealthTests(List<VsanClusterHealthTest> healthTests, Locale locale) {
    Map<String, List<VsanClusterHealthTest>> categoryNameToHealthTest = groupHealthTestsByCategory(healthTests, locale);
    VsanClusterHealthGroup[] healthGroups = createHealthGroupsByCategory(categoryNameToHealthTest, locale);
    return healthGroups;
  }
  
  private Map<String, List<VsanClusterHealthTest>> groupHealthTestsByCategory(List<VsanClusterHealthTest> healthTests, Locale locale) {
    Map<String, List<VsanClusterHealthTest>> categoryNameToHealthTests = new HashMap<>();
    for (VsanClusterHealthTest healthTest : healthTests) {
      String healthTestCategoryName = getHealthTestCategoryName(healthTest, locale);
      List<VsanClusterHealthTest> healthTestsInCategory = categoryNameToHealthTests.get(healthTestCategoryName);
      if (healthTestsInCategory == null) {
        healthTestsInCategory = new ArrayList<>();
        categoryNameToHealthTests.put(healthTestCategoryName, healthTestsInCategory);
      } 
      healthTestsInCategory.add(healthTest);
    } 
    return categoryNameToHealthTests;
  }
  
  private VsanClusterHealthGroup[] createHealthGroupsByCategory(Map<String, List<VsanClusterHealthTest>> categoryNameToHealthTest, Locale locale) {
    List<String> orderedCategoryNames = orderCategoryNames(categoryNameToHealthTest
        .keySet(), locale);
    VsanClusterHealthGroup[] healthGroups = new VsanClusterHealthGroup[orderedCategoryNames.size()];
    for (int i = 0; i < orderedCategoryNames.size(); i++) {
      String categoryName = orderedCategoryNames.get(i);
      List<VsanClusterHealthTest> healthTestsForCategory = categoryNameToHealthTest.get(categoryName);
      healthGroups[i] = 
        createHealthGroupFromGroupNameAndHealthTests(categoryName, healthTestsForCategory);
    } 
    return healthGroups;
  }
  
  private VsanClusterHealthGroup createHealthGroupFromGroupNameAndHealthTests(String groupName, List<VsanClusterHealthTest> groupTests) {
    VsanClusterHealthGroup cloudHealthGroup = new VsanClusterHealthGroup();
    cloudHealthGroup.setGroupName(groupName);
    String groupIdSuffix = groupName.toLowerCase().replace(" ", ".");
    cloudHealthGroup.setGroupId("com.vmware.adc." + groupIdSuffix);
    cloudHealthGroup.setGroupTests(groupTests
        .<VsanClusterHealthTest>toArray(new VsanClusterHealthTest[groupTests.size()]));
    List<HealthSystem.HealthStatus> groupTestsHealthStatuses = new ArrayList<>();
    for (VsanClusterHealthTest healthTest : groupTests)
      groupTestsHealthStatuses.add(HealthSystem.HealthStatus.getValue(healthTest.getTestHealth())); 
    HealthSystem.HealthStatus aggregatedHealthStatus = HealthSystem.getAggregatedHealth(groupTestsHealthStatuses);
    cloudHealthGroup.setGroupHealth(aggregatedHealthStatus.toString().toLowerCase());
    return cloudHealthGroup;
  }
  
  private List<String> orderCategoryNames(Collection<String> categoryNames, Locale locale) {
    Set<String> categoryNamesSet = new HashSet<>(categoryNames);
    String onlineHealthAvailabilityCategoryName = this._localizedMessageProvider.getMessage("com.vmware.adc.health.category.online.health.availability.name", locale);
    String generalCategoryName = this._localizedMessageProvider.getMessage("com.vmware.adc.health.category.general.health.checks.name", locale);
    categoryNamesSet.remove(onlineHealthAvailabilityCategoryName);
    boolean hasGeneralCategory = categoryNamesSet.contains(generalCategoryName);
    if (hasGeneralCategory)
      categoryNamesSet.remove(generalCategoryName); 
    List<String> orderedCategoryNames = new ArrayList<>();
    orderedCategoryNames.add(onlineHealthAvailabilityCategoryName);
    List<String> remainingCategoryNames = new ArrayList<>(categoryNamesSet);
    Collections.sort(remainingCategoryNames);
    orderedCategoryNames.addAll(remainingCategoryNames);
    if (hasGeneralCategory)
      orderedCategoryNames.add(generalCategoryName); 
    return orderedCategoryNames;
  }
  
  private String getHealthTestCategoryName(VsanClusterHealthTest healthTest, Locale locale) {
    DynamicProperty[] dynamicProperties = healthTest.getDynamicProperty();
    String categoryName = null;
    if (dynamicProperties != null && dynamicProperties.length != 0)
      for (DynamicProperty dynamicProperty : dynamicProperties) {
        if (dynamicProperty != null) {
          String dynamicPropertyName = dynamicProperty.getName();
          if ("category".equals(dynamicPropertyName)) {
            categoryName = (String)dynamicProperty.getVal();
            break;
          } 
        } 
      }  
    if (StringUtils.isBlank(categoryName))
      categoryName = this._localizedMessageProvider.getMessage("com.vmware.adc.health.category.general.health.checks.name", locale); 
    return categoryName;
  }
}
