package com.vmware.ph.phservice.cloud.health.dataapp;

import com.vmware.ph.client.common.extensions.ps.ProxySettingsValidator;
import com.vmware.ph.common.net.HttpConnectionConfig;
import com.vmware.ph.common.net.ProxySettings;
import com.vmware.ph.common.net.ProxySettingsProvider;
import com.vmware.ph.config.ceip.CeipConfigProvider;
import com.vmware.ph.phservice.cloud.health.HealthSystem;
import com.vmware.ph.phservice.common.internal.i18n.LocalizedMessageProvider;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthAction;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthGroup;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CloudInfrastructureHealthValidator {
  public static final String INFRASTRUCTURE_HEALTH_GROUP_NAME = "infrastructure.health.group.name";
  
  public static final String CEIP_INFRASTRUCTURE_HEALTH_CHECK_ID = "vSphereCloudHealthCEIPException"
    .toLowerCase();
  
  public static final String CONNECTIVITY_INFRASTRUCTURE_HEALTH_CHECK_ID = "vSphereCloudHealthConnectionException"
    .toLowerCase();
  
  private final CeipConfigProvider _ceipConfigProvider;
  
  private final ProxySettingsProvider _proxySettingsProvider;
  
  private final ProxySettingsValidator _proxySettingsValidator;
  
  private final LocalizedMessageProvider _localizedMessageProvider;
  
  public CloudInfrastructureHealthValidator(CeipConfigProvider ceipConfigProvider, ProxySettingsProvider proxySettingsProvider, ProxySettingsValidator proxySettingsValidator, LocalizedMessageProvider localizedMessageProvider) {
    this._ceipConfigProvider = ceipConfigProvider;
    this._localizedMessageProvider = localizedMessageProvider;
    this._proxySettingsValidator = proxySettingsValidator;
    this._proxySettingsProvider = proxySettingsProvider;
  }
  
  public List<VsanClusterHealthTest> conductCloudInfrastructureHealthTests(Locale locale) {
    List<VsanClusterHealthTest> infrastructureHealthTests = new ArrayList<>();
    String cloudInfrastructureHealthTestsCategoryName = this._localizedMessageProvider.getMessage("com.vmware.adc.health.category.online.health.availability.name", locale);
    boolean isCeipEnabled = this._ceipConfigProvider.isCeipEnabled();
    HealthSystem.HealthStatus ceipStatus = isCeipEnabled ? HealthSystem.HealthStatus.GREEN : HealthSystem.HealthStatus.YELLOW;
    VsanClusterHealthTest ceipHealthTest = (new CloudInfrastructureHealthTestBuilder(CEIP_INFRASTRUCTURE_HEALTH_CHECK_ID, ceipStatus, cloudInfrastructureHealthTestsCategoryName, VsanClusterHealthAction.VsanClusterHealthActionIdEnum.EnableCeip, this._localizedMessageProvider, "com.vmware.vsan.health.test", HealthSystem.CLOUD_HEALTH_GROUP_ID, locale)).build();
    infrastructureHealthTests.add(ceipHealthTest);
    if (isCeipEnabled) {
      boolean hasCloudConnectivity = checkCloudConnectivity();
      HealthSystem.HealthStatus cloudConnectivityStatus = hasCloudConnectivity ? HealthSystem.HealthStatus.GREEN : HealthSystem.HealthStatus.YELLOW;
      VsanClusterHealthTest cloudConnectivityHealthTest = (new CloudInfrastructureHealthTestBuilder(CONNECTIVITY_INFRASTRUCTURE_HEALTH_CHECK_ID, cloudConnectivityStatus, cloudInfrastructureHealthTestsCategoryName, null, this._localizedMessageProvider, "com.vmware.vsan.health.test", HealthSystem.CLOUD_HEALTH_GROUP_ID, locale)).build();
      infrastructureHealthTests.add(cloudConnectivityHealthTest);
    } 
    return infrastructureHealthTests;
  }
  
  public static VsanClusterHealthGroup mergeHealthGroupWithInfrastructureHealthTests(VsanClusterHealthGroup healthGroup, List<VsanClusterHealthTest> additionalHealthTests) {
    if (additionalHealthTests == null || additionalHealthTests.isEmpty())
      return healthGroup; 
    VsanClusterHealthGroup cloudHealthGroupWithInfrastructureTests = new VsanClusterHealthGroup();
    List<HealthSystem.HealthStatus> aggregatedHealth = new ArrayList<>();
    List<VsanClusterHealthTest> aggregatedTests = new ArrayList<>();
    addTestsAndTestHealths(additionalHealthTests, aggregatedHealth, aggregatedTests);
    if (healthGroup != null) {
      VsanClusterHealthTest[] healthGroupTests = healthGroup.getGroupTests();
      if (healthGroupTests != null)
        addTestsAndTestHealths(Arrays.asList(healthGroupTests), aggregatedHealth, aggregatedTests); 
      cloudHealthGroupWithInfrastructureTests.setGroupName(healthGroup.getGroupName());
    } else {
      cloudHealthGroupWithInfrastructureTests.setGroupName("infrastructure.health.group.name");
    } 
    cloudHealthGroupWithInfrastructureTests.setGroupTests(aggregatedTests
        .<VsanClusterHealthTest>toArray(new VsanClusterHealthTest[aggregatedTests.size()]));
    cloudHealthGroupWithInfrastructureTests.setGroupHealth(
        HealthSystem.getAggregatedHealth(aggregatedHealth).toString().toLowerCase());
    return cloudHealthGroupWithInfrastructureTests;
  }
  
  private static void addTestsAndTestHealths(List<VsanClusterHealthTest> testsToAdd, List<HealthSystem.HealthStatus> aggregatedHealthStatuses, List<VsanClusterHealthTest> aggregatedTests) {
    for (VsanClusterHealthTest test : testsToAdd) {
      if (test != null) {
        String testHealthString = test.getTestHealth();
        if (testHealthString != null) {
          aggregatedHealthStatuses.add(HealthSystem.HealthStatus.getValue(test.getTestHealth()));
          aggregatedTests.add(test);
        } 
      } 
    } 
  }
  
  public static boolean getConnectivityFromInfrastrcutureTests(List<VsanClusterHealthTest> infrastructureHealthTests) {
    if (infrastructureHealthTests == null)
      return true; 
    boolean hasCheckedCeipState = false;
    boolean hasCheckedCloudConnectivity = false;
    boolean hasConnectivity = true;
    for (VsanClusterHealthTest infrastructureHealthTest : infrastructureHealthTests) {
      String testId = infrastructureHealthTest.getTestId();
      HealthSystem.HealthStatus healthStatus = HealthSystem.HealthStatus.getValue(infrastructureHealthTest.getTestHealth());
      if (!hasCheckedCeipState && testId
        .endsWith(CEIP_INFRASTRUCTURE_HEALTH_CHECK_ID)) {
        hasConnectivity = (hasConnectivity && healthStatus.equals(HealthSystem.HealthStatus.GREEN));
        hasCheckedCeipState = true;
      } 
      if (!hasCheckedCloudConnectivity && testId
        .endsWith(CONNECTIVITY_INFRASTRUCTURE_HEALTH_CHECK_ID)) {
        hasConnectivity = (hasConnectivity && healthStatus.equals(HealthSystem.HealthStatus.GREEN));
        hasCheckedCloudConnectivity = true;
      } 
      if (hasCheckedCeipState && hasCheckedCloudConnectivity)
        break; 
    } 
    return hasConnectivity;
  }
  
  private boolean checkCloudConnectivity() {
    HttpConnectionConfig httpConnectionConfig = new HttpConnectionConfig();
    ProxySettings proxySettings = this._proxySettingsProvider.getProxySettings(httpConnectionConfig);
    return this._proxySettingsValidator.isValid(proxySettings, httpConnectionConfig);
  }
}
