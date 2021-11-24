package com.vmware.ph.phservice.cloud.health.dataapp;

import com.vmware.ph.phservice.cloud.health.AlarmsHandler;
import com.vmware.ph.phservice.cloud.health.HealthSystem;
import com.vmware.ph.phservice.cloud.health.ObjectHealthGroupProvider;
import com.vmware.ph.phservice.common.internal.i18n.LocalizedMessageProvider;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.VimContextProvider;
import com.vmware.ph.phservice.common.vim.VimContextVcClientProviderImpl;
import com.vmware.ph.phservice.common.vim.internal.vc.pc.VcPropertyCollectorReader;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.VcClientProvider;
import com.vmware.vim.binding.vim.ManagedEntity;
import com.vmware.vim.binding.vim.ServiceInstanceContent;
import com.vmware.vim.binding.vim.alarm.AlarmManager;
import com.vmware.vim.binding.vim.alarm.AlarmState;
import com.vmware.vim.binding.vim.event.EventManager;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthGroup;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthTest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VsphereHealthAlarmsHandler implements AlarmsHandler {
  private static final Log _log = LogFactory.getLog(VsphereHealthAlarmsHandler.class);
  
  private static final String FOLDER_MOREF_TYPE = "folder";
  
  private static final String VCENTER_HEALTH_GROUP_NAME = "vcenter";
  
  private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
  
  private final VimContextProvider _vimContextProvider;
  
  private final ObjectHealthGroupProvider _objectHealthGroupProvider;
  
  private final CloudInfrastructureHealthValidator _cloudInfrastructureHealthValidator;
  
  private final LocalizedMessageProvider _localizedMessageProvider;
  
  private VcClientProvider _vcClientProvider;
  
  private VsphereHealthAlarmsHelper _alarmsHelper;
  
  private ManagedObjectReference _registeredAlarmMoref = null;
  
  private AlarmState _registeredAlarmState = null;
  
  private VsanClusterHealthGroup _previousHealthGroup = null;
  
  public VsphereHealthAlarmsHandler(VimContextProvider vimContextProvider, ObjectHealthGroupProvider objectHealthGroupProvider, CloudInfrastructureHealthValidator cloudInfrastructureHealthValidator, LocalizedMessageProvider localizedMessageProvider) {
    this._vimContextProvider = vimContextProvider;
    this._objectHealthGroupProvider = objectHealthGroupProvider;
    this._cloudInfrastructureHealthValidator = cloudInfrastructureHealthValidator;
    this._localizedMessageProvider = localizedMessageProvider;
  }
  
  VsphereHealthAlarmsHandler(VimContextProvider vimContextProvider, ObjectHealthGroupProvider objectHealthGroupProvider, VsphereHealthAlarmsHelper alarmsHelper, CloudInfrastructureHealthValidator cloudInfrastructureHealthValidator, LocalizedMessageProvider localizedMessageProvider, ManagedObjectReference registeredAlarmMoref, VsanClusterHealthGroup previousHealthGroup) {
    this._vimContextProvider = vimContextProvider;
    this._objectHealthGroupProvider = objectHealthGroupProvider;
    this._alarmsHelper = alarmsHelper;
    this._cloudInfrastructureHealthValidator = cloudInfrastructureHealthValidator;
    this._localizedMessageProvider = localizedMessageProvider;
    this._registeredAlarmMoref = registeredAlarmMoref;
    this._previousHealthGroup = previousHealthGroup;
  }
  
  public void checkDataAppAgentCacheUpdates() {
    List<VsanClusterHealthGroup> currentResultsInCache = this._objectHealthGroupProvider.getCachedHealthGroups("folder");
    VsanClusterHealthGroup currentVcHealthGroup = null;
    if (!currentResultsInCache.isEmpty() && currentResultsInCache.size() == 1)
      currentVcHealthGroup = currentResultsInCache.get(0); 
    List<VsanClusterHealthTest> cloudInfrastructureHealthTests = this._cloudInfrastructureHealthValidator.conductCloudInfrastructureHealthTests(DEFAULT_LOCALE);
    VsanClusterHealthGroup healthGroupWithInfrastructureTests = CloudInfrastructureHealthValidator.mergeHealthGroupWithInfrastructureHealthTests(currentVcHealthGroup, cloudInfrastructureHealthTests);
    if (healthGroupWithInfrastructureTests != null)
      handleAlarms(healthGroupWithInfrastructureTests, DEFAULT_LOCALE); 
  }
  
  public synchronized void handleAlarms(VsanClusterHealthGroup currentHealthGroup, Locale locale) {
    if (currentHealthGroup != null) {
      boolean isVcHealthGroup = currentHealthGroup.getGroupName().toLowerCase().contains("vcenter");
      boolean isInfrastructureHealthGroup = currentHealthGroup.getGroupName().equals("infrastructure.health.group.name");
      if (isInfrastructureHealthGroup)
        removeCeipDisabledHealthTest(currentHealthGroup); 
      if (isVcHealthGroup || isInfrastructureHealthGroup)
        try {
          initVcConnection();
          if (this._alarmsHelper != null) {
            if (this._registeredAlarmMoref == null)
              this
                ._registeredAlarmMoref = this._alarmsHelper.getRegisteredHealthAlarmMoref(locale); 
            if (!this._alarmsHelper.isAlarmRegistered(this._registeredAlarmMoref))
              this._registeredAlarmMoref = this._alarmsHelper.registerHealthAlarm(locale); 
            if (!this._alarmsHelper.isHealthAlarmEnabled(this._registeredAlarmMoref))
              return; 
            updateAlarmIfStateChanged(this._previousHealthGroup, currentHealthGroup, locale);
            this._previousHealthGroup = currentHealthGroup;
          } 
        } catch (AlarmStateNotFoundException alarmStateNotFoundException) {
          _log.warn("AlarmState was not found for registered alarm: " + this._registeredAlarmMoref, alarmStateNotFoundException);
        } catch (Exception e) {
          _log.warn("An error occured while trying to trigger the vSphere Health alarm", e);
        } finally {
          closeVcConnection();
        }  
    } 
  }
  
  private void initVcConnection() {
    VimContext vimContext = this._vimContextProvider.getVimContext();
    if (vimContext == null)
      return; 
    this._vcClientProvider = (VcClientProvider)new VimContextVcClientProviderImpl(vimContext);
    VcClient vcClient = this._vcClientProvider.getVcClient();
    ServiceInstanceContent serviceInstanceContent = vcClient.getServiceInstanceContent();
    AlarmManager alarmManager = (AlarmManager)vcClient.createMo(serviceInstanceContent.alarmManager);
    EventManager eventManager = (EventManager)vcClient.createMo(serviceInstanceContent.eventManager);
    ManagedObjectReference rootFolderMoref = serviceInstanceContent.getRootFolder();
    VcPropertyCollectorReader pcReader = new VcPropertyCollectorReader(vcClient);
    this._alarmsHelper = new VsphereHealthAlarmsHelper(vcClient, alarmManager, eventManager, rootFolderMoref, pcReader, this._localizedMessageProvider);
  }
  
  private void closeVcConnection() {
    if (this._vcClientProvider != null)
      this._vcClientProvider.close(); 
    this._registeredAlarmState = null;
    this._alarmsHelper = null;
  }
  
  private void removeCeipDisabledHealthTest(VsanClusterHealthGroup currentHealthGroup) {
    List<HealthSystem.HealthStatus> groupHealth = new ArrayList<>();
    VsanClusterHealthTest ceipHealthCheck = null;
    for (VsanClusterHealthTest infrastructureHealthTest : currentHealthGroup.getGroupTests()) {
      String testId = infrastructureHealthTest.getTestId();
      if (testId.endsWith(CloudInfrastructureHealthValidator.CEIP_INFRASTRUCTURE_HEALTH_CHECK_ID)) {
        ceipHealthCheck = infrastructureHealthTest;
      } else if (infrastructureHealthTest.getTestHealth() != null) {
        groupHealth.add(
            HealthSystem.HealthStatus.getValue(infrastructureHealthTest.getTestHealth()));
      } 
    } 
    if (ceipHealthCheck != null) {
      VsanClusterHealthTest[] healthTestsWithoutCeipDisabledTest = (VsanClusterHealthTest[])ArrayUtils.removeElement((Object[])currentHealthGroup
          .getGroupTests(), ceipHealthCheck);
      currentHealthGroup.setGroupTests(healthTestsWithoutCeipDisabledTest);
    } 
    currentHealthGroup.setGroupHealth(
        HealthSystem.getAggregatedHealth(groupHealth).toString());
  }
  
  void updateAlarmIfStateChanged(VsanClusterHealthGroup previousHealthGroup, VsanClusterHealthGroup currentHealthGroup, Locale locale) throws AlarmStateNotFoundException {
    HealthSystem.HealthStatus previousHealthStatus = getPreviousHealthStatus(previousHealthGroup);
    boolean isPreviousHealthGood = previousHealthStatus.isGood();
    HealthSystem.HealthStatus currentHealthStatus = HealthSystem.HealthStatus.getValue(currentHealthGroup.getGroupHealth());
    boolean isCurrentHealthGood = currentHealthStatus.isGood();
    if (isPreviousHealthGood && isCurrentHealthGood)
      return; 
    if (isPreviousHealthGood && !isCurrentHealthGood) {
      this._alarmsHelper.triggerHealthAlarm(currentHealthStatus, locale);
    } else if (!isPreviousHealthGood && isCurrentHealthGood) {
      if (this._registeredAlarmState == null)
        this
          ._registeredAlarmState = this._alarmsHelper.getAlarmStateFromAlarmMoref(this._registeredAlarmMoref); 
      if (this._alarmsHelper.isHealthAlarmTriggered(this._registeredAlarmState))
        this._alarmsHelper.triggerHealthAlarm(currentHealthStatus, locale); 
    } else if (!isPreviousHealthGood && !isCurrentHealthGood) {
      handleBadToBadHealthStatus(previousHealthGroup, currentHealthGroup, previousHealthStatus, currentHealthStatus, locale);
    } 
  }
  
  private HealthSystem.HealthStatus getPreviousHealthStatus(VsanClusterHealthGroup previousHealthGroup) throws AlarmStateNotFoundException {
    HealthSystem.HealthStatus previousHealthStatus = null;
    if (previousHealthGroup != null) {
      previousHealthStatus = HealthSystem.HealthStatus.getValue(previousHealthGroup.getGroupHealth());
    } else {
      if (this._registeredAlarmState == null)
        this
          ._registeredAlarmState = this._alarmsHelper.getAlarmStateFromAlarmMoref(this._registeredAlarmMoref); 
      ManagedEntity.Status currentAlarmStatus = this._registeredAlarmState.getOverallStatus();
      previousHealthStatus = convertAlarmStatusToHealthStatus(currentAlarmStatus);
    } 
    return previousHealthStatus;
  }
  
  private void handleBadToBadHealthStatus(VsanClusterHealthGroup previousHealthGroup, VsanClusterHealthGroup currentHealthGroup, HealthSystem.HealthStatus previousHealthStatus, HealthSystem.HealthStatus currentHealthStatus, Locale locale) throws AlarmStateNotFoundException {
    if (this._registeredAlarmState == null)
      this
        ._registeredAlarmState = this._alarmsHelper.getAlarmStateFromAlarmMoref(this._registeredAlarmMoref); 
    boolean isAlarmResetToGray = false;
    if (this._registeredAlarmState.getAcknowledged().booleanValue() && 
      isNewIssueDetected(previousHealthGroup, currentHealthGroup)) {
      resetAlarmToGrayState(locale);
      isAlarmResetToGray = true;
    } 
    if (isAlarmResetToGray || previousHealthStatus != currentHealthStatus || 
      
      !this._alarmsHelper.isHealthAlarmTriggered(this._registeredAlarmState))
      this._alarmsHelper.triggerHealthAlarm(currentHealthStatus, locale); 
  }
  
  private void resetAlarmToGrayState(Locale locale) {
    this._alarmsHelper.triggerHealthAlarm(HealthSystem.HealthStatus.UNKNOWN, locale);
  }
  
  boolean isNewIssueDetected(VsanClusterHealthGroup previousHealthGroup, VsanClusterHealthGroup currentHealthGroup) {
    if (previousHealthGroup == null || currentHealthGroup == null)
      return false; 
    Map<String, HealthSystem.HealthStatus> previousTestResults = buildTestIdToTestHealthMap(previousHealthGroup);
    Map<String, HealthSystem.HealthStatus> currentTestResults = buildTestIdToTestHealthMap(currentHealthGroup);
    for (Map.Entry<String, HealthSystem.HealthStatus> currentTestIdToHealthStatus : currentTestResults.entrySet()) {
      if (((HealthSystem.HealthStatus)currentTestIdToHealthStatus.getValue()).isBad()) {
        HealthSystem.HealthStatus previousTestResultHealthStatus = previousTestResults.get(currentTestIdToHealthStatus.getKey());
        if (((HealthSystem.HealthStatus)currentTestIdToHealthStatus
          .getValue())
          .isWorseThan(previousTestResultHealthStatus) || previousTestResultHealthStatus == null)
          return true; 
      } 
    } 
    return false;
  }
  
  private Map<String, HealthSystem.HealthStatus> buildTestIdToTestHealthMap(VsanClusterHealthGroup healthGroup) {
    Map<String, HealthSystem.HealthStatus> testIdToTestHealth = new HashMap<>();
    for (VsanClusterHealthTest test : healthGroup.getGroupTests())
      testIdToTestHealth.put(test
          .getTestId(), 
          HealthSystem.HealthStatus.getValue(test.getTestHealth())); 
    return testIdToTestHealth;
  }
  
  private HealthSystem.HealthStatus convertAlarmStatusToHealthStatus(ManagedEntity.Status alarmStatus) {
    HealthSystem.HealthStatus healthStatus = null;
    switch (alarmStatus) {
      case gray:
        healthStatus = HealthSystem.HealthStatus.UNKNOWN;
        return healthStatus;
    } 
    healthStatus = HealthSystem.HealthStatus.getValue(alarmStatus.toString());
    return healthStatus;
  }
  
  public void close() throws Exception {
    closeVcConnection();
  }
}
