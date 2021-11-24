package com.vmware.ph.phservice.cloud.health.dataapp;

import com.vmware.ph.phservice.cloud.health.HealthSystem;
import com.vmware.ph.phservice.common.internal.i18n.LocalizedMessageProvider;
import com.vmware.vim.binding.impl.vmodl.DynamicPropertyImpl;
import com.vmware.vim.binding.impl.vmodl.LocalizableMessageImpl;
import com.vmware.vim.binding.vmodl.DynamicProperty;
import com.vmware.vim.binding.vmodl.LocalizableMessage;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthAction;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthTest;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

public class CloudInfrastructureHealthTestBuilder {
  private final String _testKey;
  
  private final HealthSystem.HealthStatus _testHealthStatus;
  
  private final String _testCategoryName;
  
  private final VsanClusterHealthAction.VsanClusterHealthActionIdEnum _actionId;
  
  private final LocalizedMessageProvider _localizedMessageProvider;
  
  private final String _healthTestKey;
  
  private final String _cloudHealthGroupId;
  
  private final Locale _locale;
  
  public CloudInfrastructureHealthTestBuilder(String testKey, HealthSystem.HealthStatus testHealthStatus, String testCategoryName, VsanClusterHealthAction.VsanClusterHealthActionIdEnum actionId, LocalizedMessageProvider localizedMessageProvider, String healthTestKey, String cloudHealthGroupId, Locale locale) {
    this._testKey = testKey;
    this._testHealthStatus = testHealthStatus;
    this._testCategoryName = testCategoryName;
    this._actionId = actionId;
    this._localizedMessageProvider = localizedMessageProvider;
    this._healthTestKey = healthTestKey;
    this._cloudHealthGroupId = cloudHealthGroupId;
    this._locale = locale;
  }
  
  public VsanClusterHealthTest build() {
    String testKey = String.format("%s.%s", new Object[] { this._healthTestKey, this._testKey });
    String testNameKey = String.format("%s.%s.testName", new Object[] { this._cloudHealthGroupId, this._testKey });
    String testDescKey = "";
    String testShortKey = "";
    if (this._testHealthStatus == HealthSystem.HealthStatus.GREEN) {
      testDescKey = String.format("%s.desc.enabled", new Object[] { testKey });
      testShortKey = String.format("%s.short.enabled", new Object[] { testKey });
    } else {
      testDescKey = String.format("%s.desc.disabled", new Object[] { testKey });
      testShortKey = String.format("%s.short.disabled", new Object[] { testKey });
    } 
    VsanClusterHealthAction healthAction = null;
    if (this._actionId != null) {
      String label = this._actionId.toString().toLowerCase();
      boolean isEnabled = true;
      healthAction = createHealthAction(this._actionId, label, isEnabled, this._localizedMessageProvider, this._locale);
    } 
    VsanClusterHealthTest healthTest = new VsanClusterHealthTest();
    healthTest.setTestId(testKey);
    healthTest.setTestName(this._localizedMessageProvider
        .getMessage(testNameKey, this._locale));
    healthTest.setTestDescription(this._localizedMessageProvider
        .getMessage(testDescKey, this._locale));
    healthTest.setTestShortDescription(this._localizedMessageProvider
        .getMessage(testShortKey, this._locale));
    healthTest.setTestHealth(this._testHealthStatus.toString());
    if (healthAction != null)
      healthTest.setTestActions(new VsanClusterHealthAction[] { healthAction }); 
    if (!StringUtils.isBlank(this._testCategoryName)) {
      DynamicPropertyImpl dynamicPropertyImpl = new DynamicPropertyImpl();
      dynamicPropertyImpl.setName("category");
      dynamicPropertyImpl.setVal(this._testCategoryName);
      healthTest.setDynamicProperty(new DynamicProperty[] { (DynamicProperty)dynamicPropertyImpl });
    } 
    return healthTest;
  }
  
  private static VsanClusterHealthAction createHealthAction(VsanClusterHealthAction.VsanClusterHealthActionIdEnum actionId, String label, boolean isEnabled, LocalizedMessageProvider localizedMessageProvider, Locale locale) {
    String actionLabelKey = String.format("com.vmware.vsan.health.action.label.%s", new Object[] { label });
    String actionDescKey = String.format("com.vmware.vsan.health.action.%s.short", new Object[] { label });
    LocalizableMessageImpl localizableMessageImpl1 = new LocalizableMessageImpl();
    localizableMessageImpl1.setKey(actionLabelKey);
    localizableMessageImpl1.setMessage(localizedMessageProvider
        .getMessage(actionLabelKey, locale));
    LocalizableMessageImpl localizableMessageImpl2 = new LocalizableMessageImpl();
    localizableMessageImpl2.setKey(actionDescKey);
    localizableMessageImpl2.setMessage(localizedMessageProvider
        .getMessage(actionDescKey, locale));
    VsanClusterHealthAction healthAction = new VsanClusterHealthAction();
    healthAction.setActionId(actionId.name());
    healthAction.setActionLabel((LocalizableMessage)localizableMessageImpl1);
    healthAction.setActionDescription((LocalizableMessage)localizableMessageImpl2);
    healthAction.setEnabled(isEnabled);
    return healthAction;
  }
}
