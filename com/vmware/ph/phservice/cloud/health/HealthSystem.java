package com.vmware.ph.phservice.cloud.health;

import com.vmware.ph.phservice.cloud.dataapp.internal.ProgressReporter;
import com.vmware.ph.phservice.cloud.health.dataapp.CloudInfrastructureHealthTestBuilder;
import com.vmware.ph.phservice.cloud.health.dataapp.CloudInfrastructureHealthValidator;
import com.vmware.ph.phservice.cloud.health.dataapp.HealthGroupsCategoryHelper;
import com.vmware.ph.phservice.cloud.health.dataapp.healthtask.VsphereHealthTaskReporter;
import com.vmware.ph.phservice.cloud.health.dataapp.healthtask.VsphereHealthTaskReporterFactory;
import com.vmware.ph.phservice.cloud.health.repository.SilencedTestsRepository;
import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.internal.DateUtil;
import com.vmware.ph.phservice.common.internal.i18n.LocalizedMessageProvider;
import com.vmware.ph.phservice.common.vmomi.internal.server.VmomiUtil;
import com.vmware.vim.binding.vim.fault.VsanFault;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.RuntimeFault;
import com.vmware.vim.binding.vmodl.fault.InvalidArgument;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthGroup;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthResultBase;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthResultColumnInfo;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthResultTable;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthSummary;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthTest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthSystem {
  private static final Logger _logger = LoggerFactory.getLogger(HealthSystem.class);
  
  private static final Pattern TEST_ID_PATTERN = Pattern.compile("^\\w+([.]\\w+)*$");
  
  private static final String FAULT_EXECUTION_FAULT_KEY = "com.vmware.adc.health.fault.executionFault";
  
  private static final int PERCENT_DONE_AFTER_CLOUDGROUPS_RETRIEVED = 94;
  
  private static final int PERCENT_DONE_AFTER_INFRASTRUCTURE_TESTS_DONE = 96;
  
  private static final int PERCENT_DONE_AFTER_ALARMS_HANDLED = 98;
  
  public static final String VSAN_HEALTH_KEY = "com.vmware.vsan.health.test";
  
  public enum HealthStatus {
    SKIPPED, GREEN, UNKNOWN, INFO, YELLOW, RED;
    
    public static List<HealthStatus> getWorstToBestPriorityLst() {
      return Arrays.asList(new HealthStatus[] { RED, YELLOW, INFO, UNKNOWN, GREEN, SKIPPED });
    }
    
    public static HealthStatus getValue(String enumString) {
      return valueOf(enumString.toUpperCase());
    }
    
    public boolean isBad() {
      return (compareTo(YELLOW) >= 0);
    }
    
    public boolean isGood() {
      return !isBad();
    }
    
    public boolean isWorseThan(HealthStatus other) {
      if (other == null)
        return false; 
      return (compareTo(other) > 0);
    }
    
    public boolean isBetterThan(HealthStatus other) {
      if (other == null)
        return false; 
      return (compareTo(other) < 0);
    }
    
    public String toString() {
      return super.toString().toLowerCase();
    }
  }
  
  public static final String CLOUD_HEALTH_GROUP_ID = String.format("%s.cloudhealth", new Object[] { "com.vmware.vsan.health.test" });
  
  static final String CLOUD_HEALTH_EXECUTION_TEST_NAME = "CloudHealthExecutionException";
  
  static final String GREEN_OVERALL_HEALTH_DESCRIPTION_KEY = String.format("%s.overallHealthDescription.green", new Object[] { CLOUD_HEALTH_GROUP_ID });
  
  static final String NON_GREEN_OVERALL_HEALTH_DESCRIPTION_APPENDIX_KEY = String.format("%s.overallHealthDescription.issueAppendix", new Object[] { CLOUD_HEALTH_GROUP_ID });
  
  private final CloudInfrastructureHealthValidator _cloudInfrastructureHealthValidator;
  
  private final HealthGroupsCategoryHelper _healthGroupsCategoryHelper;
  
  private final ObjectHealthGroupProvider _objectHealthGroupProvider;
  
  private final LocalizedMessageProvider _localizedMessageProvider;
  
  private final SilencedTestsRepository _silencedTestsRepository;
  
  private final VsphereHealthTaskReporterFactory _vsphereHealthTaskReporterFactory;
  
  private final ExecutorService _alarmsExecutor;
  
  private AlarmsHandler _alarmsHandler;
  
  public HealthSystem(CloudInfrastructureHealthValidator cloudInfrastructureHealthValidator, HealthGroupsCategoryHelper healthGroupsCategoryHelper, ObjectHealthGroupProvider objectHealthGroupProvider, LocalizedMessageProvider localizedMessageProvider, SilencedTestsRepository silencedTestsRepository, VsphereHealthTaskReporterFactory vsphereHealthTaskReporterFactory) {
    this._cloudInfrastructureHealthValidator = cloudInfrastructureHealthValidator;
    this._healthGroupsCategoryHelper = healthGroupsCategoryHelper;
    this._objectHealthGroupProvider = objectHealthGroupProvider;
    this._localizedMessageProvider = localizedMessageProvider;
    this._silencedTestsRepository = silencedTestsRepository;
    this._vsphereHealthTaskReporterFactory = vsphereHealthTaskReporterFactory;
    this._alarmsExecutor = Executors.newSingleThreadExecutor();
  }
  
  public void setAlarmsHandler(AlarmsHandler alarmsHandler) {
    this._alarmsHandler = alarmsHandler;
  }
  
  public VsanClusterHealthSummary getCloudHealthSummary(ManagedObjectReference moRef, Boolean fetchFromCache, String perspective, Locale locale, String sessionUser) {
    boolean shouldTriggerVsphereHealthTask = !Boolean.TRUE.equals(fetchFromCache);
    VsanClusterHealthSummary summary = new VsanClusterHealthSummary();
    VsphereHealthTaskReporter vsphereHealthTaskReporter = null;
    try {
      if (shouldTriggerVsphereHealthTask) {
        vsphereHealthTaskReporter = this._vsphereHealthTaskReporterFactory.createVsphereHealthTaskReporter(this._localizedMessageProvider);
        vsphereHealthTaskReporter.triggerTask(moRef, locale, sessionUser);
      } 
      Pair<VsanClusterHealthGroup[], Date> healthGroupsAndTimestamp = getHealthGroupsAndTimestamp(moRef, (fetchFromCache != null) ? fetchFromCache
          
          .booleanValue() : false, perspective, locale, vsphereHealthTaskReporter);
      VsanClusterHealthGroup[] healthGroups = (VsanClusterHealthGroup[])healthGroupsAndTimestamp.getFirst();
      Date healthGroupsTimestamp = (Date)healthGroupsAndTimestamp.getSecond();
      if (healthGroupsTimestamp == null)
        healthGroupsTimestamp = DateUtil.createUtcCalendar().getTime(); 
      summary.setGroups(healthGroups);
      Calendar timestamp = DateUtil.createUtcCalendar();
      timestamp.setTime(healthGroupsTimestamp);
      summary.setTimestamp(timestamp);
      populateOverallHealthInSummary(summary, healthGroups, locale);
      if (vsphereHealthTaskReporter != null)
        vsphereHealthTaskReporter.reportSuccess(); 
    } catch (Exception e) {
      RuntimeFault fault = VmomiUtil.generateRuntimeFault(this._localizedMessageProvider, "com.vmware.adc.health.fault.executionFault", e);
      try {
        if (vsphereHealthTaskReporter != null)
          vsphereHealthTaskReporter.reportFailure((Exception)fault); 
      } catch (Exception taskFault) {
        String message = "Failed to update error state of task: " + taskFault.getMessage();
        if (_logger.isDebugEnabled()) {
          _logger.debug(message, taskFault);
        } else {
          _logger.error(message);
        } 
      } 
      throw fault;
    } finally {
      if (vsphereHealthTaskReporter != null)
        vsphereHealthTaskReporter.close(); 
    } 
    return summary;
  }
  
  public Set<String> getSilencedTests(ManagedObjectReference moRef) {
    if (moRef == null) {
      _logger.debug("No silent checks for null moRef.");
      return new HashSet<>();
    } 
    Set<String> silencedTests = this._silencedTestsRepository.getSilencedTests(moRef);
    _logger.debug("Silenced tests: {}", silencedTests);
    if (!moRef.getType().equalsIgnoreCase("Folder")) {
      ManagedObjectReference vcMoRef = createVcMoRefForVcGuid(moRef.getServerGuid());
      Set<String> vcSilencedTests = this._silencedTestsRepository.getSilencedTests(vcMoRef);
      HashSet<String> hostSilencedTests = new HashSet<>(silencedTests);
      hostSilencedTests.addAll(vcSilencedTests);
      _logger.debug("Silenced tests for host: {}", hostSilencedTests);
      silencedTests = hostSilencedTests;
    } 
    return silencedTests;
  }
  
  public boolean updateSilencedTests(ManagedObjectReference moRef, Set<String> testsToAdd, Set<String> testsToRemove) throws VsanFault {
    if (moRef == null) {
      VsanFault vsanFault = new VsanFault();
      vsanFault.setMessage("Cannot set silent checks for null moRef.");
      throw vsanFault;
    } 
    try {
      validateTestIdsFormat(testsToAdd, testsToRemove);
    } catch (InvalidArgument e) {
      VsanFault vsanFault = new VsanFault();
      vsanFault.setFaultCause((Exception)e);
      vsanFault.setMessage("Invalid health check id.");
      throw vsanFault;
    } 
    try {
      this._silencedTestsRepository.updateSilencedTests(moRef, testsToAdd, testsToRemove);
    } catch (IOException e) {
      VsanFault fault = new VsanFault();
      fault.setFaultCause(e);
      fault.setMessage("Failed to update silenced tests.");
    } 
    return true;
  }
  
  private Pair<VsanClusterHealthGroup[], Date> getHealthGroupsAndTimestamp(ManagedObjectReference moRef, boolean useCache, String perspective, Locale locale, ProgressReporter progressReporter) {
    Pair<VsanClusterHealthGroup, Date> objectHealthGroupAndTimestamp = getObjectHealthGroupAndTimestamp(moRef, useCache, perspective, progressReporter);
    if (progressReporter != null)
      progressReporter.reportProgress(94); 
    List<VsanClusterHealthTest> cloudInfrastructureHealthTests = this._cloudInfrastructureHealthValidator.conductCloudInfrastructureHealthTests(locale);
    if (progressReporter != null)
      progressReporter.reportProgress(96); 
    VsanClusterHealthGroup objectHealthGroup = (objectHealthGroupAndTimestamp != null) ? (VsanClusterHealthGroup)objectHealthGroupAndTimestamp.getFirst() : null;
    List<VsanClusterHealthTest> healthTests = getConductedHealthTests(objectHealthGroup, cloudInfrastructureHealthTests, locale);
    updateStatusOfSilencedHealthChecks(moRef, healthTests);
    if (this._alarmsHandler != null)
      handleVsphereHealthAlarm(objectHealthGroup, cloudInfrastructureHealthTests, locale); 
    if (progressReporter != null)
      progressReporter.reportProgress(98); 
    localizeHealthTests(healthTests, locale);
    VsanClusterHealthGroup[] healthGroups = this._healthGroupsCategoryHelper.buildCategoryHealthGroupsFromHealthTests(healthTests, locale);
    Date objectHealthTimestamp = (objectHealthGroupAndTimestamp != null) ? (Date)objectHealthGroupAndTimestamp.getSecond() : null;
    return new Pair(healthGroups, objectHealthTimestamp);
  }
  
  private void updateStatusOfSilencedHealthChecks(ManagedObjectReference moRef, List<VsanClusterHealthTest> healthTests) {
    Set<String> silencedTests = getSilencedTests(moRef);
    for (VsanClusterHealthTest test : healthTests) {
      if (silencedTests.contains(test.getTestId()))
        test.setTestHealth(HealthStatus.SKIPPED.toString().toLowerCase()); 
    } 
  }
  
  private ManagedObjectReference createVcMoRefForVcGuid(String vcGuid) {
    return new ManagedObjectReference("Folder", "group-d1", vcGuid);
  }
  
  private void validateTestIdsFormat(Set<String> testsToAdd, Set<String> testsToRemove) throws InvalidArgument {
    Set<String> testIds = new HashSet<>(testsToAdd);
    testIds.addAll(testsToRemove);
    checkForBlankTestIds(testIds);
    checkForTestIdPattern(testIds);
  }
  
  private void checkForBlankTestIds(Set<String> testIds) {
    if (testIds.stream().anyMatch(StringUtils::isBlank)) {
      _logger.error("You cannot add/remove null or empty testId.");
      throw new InvalidArgument();
    } 
  }
  
  private void checkForTestIdPattern(Set<String> testIds) {
    Predicate<String> testIdFilter = TEST_ID_PATTERN.asPredicate();
    Set<String> validFormatTestIds = (Set<String>)testIds.stream().filter(testIdFilter).collect(Collectors.toSet());
    HashSet<String> invalidTestIds = new HashSet<>(testIds);
    invalidTestIds.removeAll(validFormatTestIds);
    if (!invalidTestIds.isEmpty()) {
      String errorMessage = String.format("Some of the testIds have invalid format %s.", new Object[] { invalidTestIds
            
            .toString() });
      _logger.error(errorMessage);
      InvalidArgument fault = new InvalidArgument();
      fault.setMessage(errorMessage);
      throw fault;
    } 
  }
  
  private void handleVsphereHealthAlarm(final VsanClusterHealthGroup vsanClusterHealthGroup, final List<VsanClusterHealthTest> cloudInfrastructureHealthTests, final Locale locale) {
    Runnable handleAlarmsTask = new Runnable() {
        public void run() {
          VsanClusterHealthGroup healthGroupWithInfrastructureTests = CloudInfrastructureHealthValidator.mergeHealthGroupWithInfrastructureHealthTests(vsanClusterHealthGroup, cloudInfrastructureHealthTests);
          if (healthGroupWithInfrastructureTests != null)
            HealthSystem.this._alarmsHandler.handleAlarms(healthGroupWithInfrastructureTests, locale); 
        }
      };
    this._alarmsExecutor.execute(handleAlarmsTask);
  }
  
  private Pair<VsanClusterHealthGroup, Date> getObjectHealthGroupAndTimestamp(ManagedObjectReference moRef, boolean useCache, String perspective, ProgressReporter progressReporter) {
    Pair<VsanClusterHealthGroup, Date> objectHealthGroupAndTimestamp = this._objectHealthGroupProvider.getObjectHealthGroupAndTimestamp(moRef, useCache, perspective, progressReporter);
    return objectHealthGroupAndTimestamp;
  }
  
  private List<VsanClusterHealthTest> getConductedHealthTests(VsanClusterHealthGroup objectHealthGroup, List<VsanClusterHealthTest> infrastructureHealthTests, Locale locale) {
    List<VsanClusterHealthTest> healthTests = new ArrayList<>();
    healthTests.addAll(infrastructureHealthTests);
    boolean hasCloudConnectivity = CloudInfrastructureHealthValidator.getConnectivityFromInfrastrcutureTests(infrastructureHealthTests);
    if (objectHealthGroup != null) {
      VsanClusterHealthTest[] groupTests = objectHealthGroup.getGroupTests();
      if (groupTests != null)
        healthTests.addAll(Arrays.asList(groupTests)); 
    } else if (hasCloudConnectivity) {
      HealthStatus cloudHealthExecutionWarning = HealthStatus.YELLOW;
      String cloudHealthExecutionTestKey = "CloudHealthExecutionException".toLowerCase();
      String onlineHealthAvailabilityCategoryName = this._localizedMessageProvider.getMessage("com.vmware.adc.health.category.online.health.availability.name", locale);
      VsanClusterHealthTest coudHealthExecutionExceptionHealthTest = (new CloudInfrastructureHealthTestBuilder(cloudHealthExecutionTestKey, cloudHealthExecutionWarning, onlineHealthAvailabilityCategoryName, null, this._localizedMessageProvider, "com.vmware.vsan.health.test", CLOUD_HEALTH_GROUP_ID, locale)).build();
      healthTests.add(coudHealthExecutionExceptionHealthTest);
    } 
    return healthTests;
  }
  
  public static HealthStatus getAggregatedHealth(List<HealthStatus> healths) {
    for (HealthStatus status : HealthStatus.getWorstToBestPriorityLst()) {
      if (healths.contains(status))
        return status; 
    } 
    return HealthStatus.GREEN;
  }
  
  private void populateOverallHealthInSummary(VsanClusterHealthSummary summary, VsanClusterHealthGroup[] healthGroups, Locale locale) {
    VsanClusterHealthGroup worstHealthGroup = findHealthGroupWithWorstHealth(healthGroups);
    String overallHealth = worstHealthGroup.getGroupHealth();
    String overallHealthDescription = getOverallHealthDescriptionWithIssue(worstHealthGroup, locale);
    if (HealthStatus.getValue(overallHealth).isGood())
      overallHealthDescription = this._localizedMessageProvider.getMessage(GREEN_OVERALL_HEALTH_DESCRIPTION_KEY, locale); 
    summary.setOverallHealth(overallHealth);
    summary.setOverallHealthDescription(overallHealthDescription);
  }
  
  private static VsanClusterHealthGroup findHealthGroupWithWorstHealth(VsanClusterHealthGroup[] healthGroups) {
    VsanClusterHealthGroup worstHealthGroup = healthGroups[0];
    for (HealthStatus status : HealthStatus.getWorstToBestPriorityLst()) {
      boolean worstHealthFound = false;
      String statusAsString = status.toString();
      for (VsanClusterHealthGroup healthGroup : healthGroups) {
        if (statusAsString.equals(healthGroup.getGroupHealth())) {
          worstHealthGroup = healthGroup;
          worstHealthFound = true;
          break;
        } 
      } 
      if (worstHealthFound)
        break; 
    } 
    return worstHealthGroup;
  }
  
  private String getOverallHealthDescriptionWithIssue(VsanClusterHealthGroup healthGroup, Locale locale) {
    return healthGroup.getGroupName() + " " + this._localizedMessageProvider.getMessage(NON_GREEN_OVERALL_HEALTH_DESCRIPTION_APPENDIX_KEY, locale);
  }
  
  private void localizeHealthTests(List<VsanClusterHealthTest> healthTests, Locale locale) {
    for (VsanClusterHealthTest healthTest : healthTests) {
      VsanClusterHealthResultBase[] testDetails = healthTest.getTestDetails();
      if (testDetails != null)
        for (VsanClusterHealthResultBase testDetail : testDetails) {
          if (testDetail instanceof VsanClusterHealthResultTable) {
            VsanClusterHealthResultTable testTable = (VsanClusterHealthResultTable)testDetail;
            testTable.setLabel(this._localizedMessageProvider.getMessage(testTable
                  .getLabel(), locale));
            VsanClusterHealthResultColumnInfo[] columns = testTable.getColumns();
            if (columns != null)
              for (VsanClusterHealthResultColumnInfo columnInfo : columns)
                columnInfo.setLabel(this._localizedMessageProvider.getMessage(columnInfo
                      .getLabel(), locale));  
          } 
        }  
    } 
  }
}
