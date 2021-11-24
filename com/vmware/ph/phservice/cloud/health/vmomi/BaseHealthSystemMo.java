package com.vmware.ph.phservice.cloud.health.vmomi;

import com.vmware.vim.binding.vim.fault.NotFound;
import com.vmware.vim.binding.vim.fault.VsanFault;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.fault.InvalidArgument;
import com.vmware.vim.binding.vmodl.fault.NotSupported;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vsan.binding.vim.cluster.VsanAttachToSrOperation;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterAdvCfgSyncResult;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterClomdLivenessResult;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterCreateVmHealthTestResult;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterDataProtectionCfgSyncResult;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterDatastoreUsageResult;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterDpdLivenessResult;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterEncryptionHealthSummary;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterFileServiceHealthSummary;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHclInfo;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthCheckInfo;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthConfigs;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthSummary;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthSystemVersionResult;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHostVmknicMapping;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterLimitHealthResult;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterNetworkHealthResult;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterNetworkLoadTestResult;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterTelemetryProxyConfig;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterVmdkLoadTestResult;
import com.vmware.vim.vsan.binding.vim.cluster.VsanHealthExtMgmtPreCheckResult;
import com.vmware.vim.vsan.binding.vim.cluster.VsanHistoryItemQuerySpec;
import com.vmware.vim.vsan.binding.vim.cluster.VsanObjectExtAttrs;
import com.vmware.vim.vsan.binding.vim.cluster.VsanStorageWorkloadType;
import com.vmware.vim.vsan.binding.vim.cluster.VsanVcClusterHealthSystem;
import com.vmware.vim.vsan.binding.vim.cluster.VsanVsanClusterPcapResult;
import com.vmware.vim.vsan.binding.vim.host.VsanArchivalAccessibilityResult;
import com.vmware.vim.vsan.binding.vim.host.VsanObjectOverallHealth;
import com.vmware.vim.vsan.binding.vim.host.VsanPhysicalDiskHealthSummary;
import com.vmware.vim.vsan.binding.vim.host.VsanSmartStatsHostSummary;
import com.vmware.vim.vsan.binding.vim.host.VsanVmdkLoadTestSpec;
import com.vmware.vim.vsan.binding.vim.vsan.VsanHclReleaseConstraint;

public abstract class BaseHealthSystemMo implements VsanVcClusterHealthSystem {
  public void sendVsanTelemetry(ManagedObjectReference cluster, Future<Void> result) {
    result.setException((Exception)new NotSupported());
  }
  
  public void sendVsanTelemetry(ManagedObjectReference cluster) throws NotFound, VsanFault, NotSupported {
    throw new NotSupported();
  }
  
  public void setVsanClusterTelemetryConfig(ManagedObjectReference cluster, VsanClusterHealthConfigs vsanClusterHealthConfig, Future<Void> result) {
    result.setException((Exception)new NotSupported());
  }
  
  public void setVsanClusterTelemetryConfig(ManagedObjectReference cluster, VsanClusterHealthConfigs vsanClusterHealthConfig) throws NotSupported {
    throw new NotSupported();
  }
  
  public void restoreClusterRebootWithNAMM(ManagedObjectReference cluster, Integer scheduleTime, Future<Boolean> future) {
    throw new NotSupported();
  }
  
  public boolean restoreClusterRebootWithNAMM(ManagedObjectReference cluster, Integer scheduleTime) throws InvalidArgument, VsanFault {
    throw new NotSupported();
  }
  
  public void testVsanClusterTelemetryProxy(VsanClusterTelemetryProxyConfig cluster, Future<Boolean> result) {
    result.setException((Exception)new NotSupported());
  }
  
  public boolean testVsanClusterTelemetryProxy(VsanClusterTelemetryProxyConfig cluster) throws VsanFault, NotSupported {
    throw new NotSupported();
  }
  
  public void queryClusterCreateVmHealthHistoryTest(ManagedObjectReference paramManagedObjectReference, Integer paramInteger, Future<VsanClusterCreateVmHealthTestResult[]> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterCreateVmHealthTestResult[] queryClusterCreateVmHealthHistoryTest(ManagedObjectReference paramManagedObjectReference, Integer paramInteger) throws NotFound {
    throw new NotSupported();
  }
  
  public void preCheckClusterForManageExtension(ManagedObjectReference paramManagedObjectReference, boolean paramBoolean, String paramString, Future<VsanHealthExtMgmtPreCheckResult> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanHealthExtMgmtPreCheckResult preCheckClusterForManageExtension(ManagedObjectReference paramManagedObjectReference, boolean paramBoolean, String paramString) throws NotFound, VsanFault {
    throw new NotSupported();
  }
  
  public void queryClusterVmdkLoadHistoryTest(ManagedObjectReference paramManagedObjectReference, Integer paramInteger, String paramString, Future<VsanClusterVmdkLoadTestResult[]> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterVmdkLoadTestResult[] queryClusterVmdkLoadHistoryTest(ManagedObjectReference paramManagedObjectReference, Integer paramInteger, String paramString) throws NotFound {
    throw new NotSupported();
  }
  
  public void queryVsanClusterHealthCheckInterval(ManagedObjectReference paramManagedObjectReference, Future<Integer> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public int queryVsanClusterHealthCheckInterval(ManagedObjectReference paramManagedObjectReference) throws NotFound {
    throw new NotSupported();
  }
  
  public void queryClusterNetworkPerfHistoryTest(ManagedObjectReference paramManagedObjectReference, Integer paramInteger, Future<VsanClusterNetworkLoadTestResult[]> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterNetworkLoadTestResult[] queryClusterNetworkPerfHistoryTest(ManagedObjectReference paramManagedObjectReference, Integer paramInteger) throws NotFound {
    throw new NotSupported();
  }
  
  public void getClusterStatus(ManagedObjectReference paramManagedObjectReference, String paramString, Future<String> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public String getClusterStatus(ManagedObjectReference paramManagedObjectReference, String paramString) throws NotFound {
    throw new NotSupported();
  }
  
  public void queryVerifyClusterHealthSystemVersions(ManagedObjectReference paramManagedObjectReference, Future<VsanClusterHealthSystemVersionResult> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterHealthSystemVersionResult queryVerifyClusterHealthSystemVersions(ManagedObjectReference paramManagedObjectReference) throws NotFound {
    throw new NotSupported();
  }
  
  public void purgeHclFiles(String[] paramArrayOfString, Future<Void> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public void purgeHclFiles(String[] paramArrayOfString) throws NotFound, VsanFault, NotSupported {
    throw new NotSupported();
  }
  
  public VsanClusterAdvCfgSyncResult[] queryAdvCfgSync(ManagedObjectReference cluster, String[] options, Boolean includeAllAdvOptions, Boolean nonDefaultOnly) throws NotFound {
    throw new NotSupported();
  }
  
  public void queryAdvCfgSync(ManagedObjectReference cluster, String[] options, Boolean includeAllAdvOptions, Boolean nonDefaultOnly, Future<VsanClusterAdvCfgSyncResult[]> future) {
    future.setException((Exception)new NotSupported());
  }
  
  public void queryObjectHealthSummary(ManagedObjectReference paramManagedObjectReference, String[] paramArrayOfString, Boolean paramBoolean1, Boolean paramBoolean2, Future<VsanObjectOverallHealth> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanObjectOverallHealth queryObjectHealthSummary(ManagedObjectReference paramManagedObjectReference, String[] paramArrayOfString, Boolean paramBoolean1, Boolean paramBoolean2) throws NotFound {
    throw new NotSupported();
  }
  
  public void setLogLevel(String paramString, Future<Void> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public void setLogLevel(String paramString) throws NotFound {
    throw new NotSupported();
  }
  
  public void queryVerifyClusterNetworkSettings(ManagedObjectReference paramManagedObjectReference, Future<VsanClusterNetworkHealthResult> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterNetworkHealthResult queryVerifyClusterNetworkSettings(ManagedObjectReference paramManagedObjectReference) throws NotFound {
    throw new NotSupported();
  }
  
  public void uploadHclDb(String paramString, Future<Boolean> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public boolean uploadHclDb(String paramString) throws NotFound, VsanFault, NotSupported {
    throw new NotSupported();
  }
  
  public void querySmartStatsSummary(ManagedObjectReference paramManagedObjectReference, Future<VsanSmartStatsHostSummary[]> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanSmartStatsHostSummary[] querySmartStatsSummary(ManagedObjectReference paramManagedObjectReference) throws NotFound {
    throw new NotSupported();
  }
  
  public void updateHclDbFromWeb(String paramString, Future<Boolean> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public boolean updateHclDbFromWeb(String paramString) throws NotFound, VsanFault, NotSupported {
    throw new NotSupported();
  }
  
  public void checkClomdLiveness(ManagedObjectReference paramManagedObjectReference, Future<VsanClusterClomdLivenessResult> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterClomdLivenessResult checkClomdLiveness(ManagedObjectReference paramManagedObjectReference) throws NotFound {
    throw new NotSupported();
  }
  
  public void repairClusterObjectsImmediate(ManagedObjectReference paramManagedObjectReference, String[] paramArrayOfString, Future<ManagedObjectReference> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public ManagedObjectReference repairClusterObjectsImmediate(ManagedObjectReference paramManagedObjectReference, String[] paramArrayOfString) throws NotFound, VsanFault {
    throw new NotSupported();
  }
  
  public VsanClusterNetworkLoadTestResult queryClusterNetworkPerfTest(ManagedObjectReference cluster, boolean multicast, Integer durationSec) throws NotFound, NotSupported {
    throw new NotSupported();
  }
  
  public void queryClusterNetworkPerfTest(ManagedObjectReference cluster, boolean multicast, Integer durationSec, Future<VsanClusterNetworkLoadTestResult> future) {
    future.setException((Exception)new NotSupported());
  }
  
  public void queryClusterCreateVmHealthTest(ManagedObjectReference paramManagedObjectReference, int paramInt, Future<VsanClusterCreateVmHealthTestResult> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterCreateVmHealthTestResult queryClusterCreateVmHealthTest(ManagedObjectReference paramManagedObjectReference, int paramInt) throws NotFound {
    throw new NotSupported();
  }
  
  public void queryVsanProxyConfig(Future<VsanClusterTelemetryProxyConfig> future) {
    future.setException((Exception)new NotSupported());
  }
  
  public VsanClusterTelemetryProxyConfig queryVsanProxyConfig() {
    throw new NotSupported();
  }
  
  public void queryEncryptionHealthSummary(ManagedObjectReference paramManagedObjectReference, Future<VsanClusterEncryptionHealthSummary> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterEncryptionHealthSummary queryEncryptionHealthSummary(ManagedObjectReference paramManagedObjectReference) throws NotFound {
    throw new NotSupported();
  }
  
  public void queryAllSupportedHealthChecks(Future<VsanClusterHealthCheckInfo[]> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterHealthCheckInfo[] queryAllSupportedHealthChecks() throws NotFound {
    throw new NotSupported();
  }
  
  public void getClusterHclInfo(ManagedObjectReference paramManagedObjectReference, Boolean paramBoolean1, Boolean paramBoolean2, String paramString, Future<VsanClusterHclInfo> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterHclInfo getClusterHclInfo(ManagedObjectReference paramManagedObjectReference, Boolean paramBoolean1, Boolean paramBoolean2, String paramString) throws NotFound, VsanFault {
    throw new NotSupported();
  }
  
  public void prepareCluster(ManagedObjectReference paramManagedObjectReference, String paramString, Future<ManagedObjectReference> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public ManagedObjectReference prepareCluster(ManagedObjectReference paramManagedObjectReference, String paramString) throws NotFound, VsanFault {
    throw new NotSupported();
  }
  
  public void queryAttachToSrHistory(ManagedObjectReference paramManagedObjectReference, Integer paramInteger, String paramString, Future<VsanAttachToSrOperation[]> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanAttachToSrOperation[] queryAttachToSrHistory(ManagedObjectReference paramManagedObjectReference, Integer paramInteger, String paramString) throws NotFound {
    throw new NotSupported();
  }
  
  public void checkArchivalAccessibility(ManagedObjectReference paramManagedObjectReference, Future<VsanArchivalAccessibilityResult[]> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanArchivalAccessibilityResult[] checkArchivalAccessibility(ManagedObjectReference paramManagedObjectReference) {
    throw new NotSupported();
  }
  
  public void getClusterReleaseRecommendation(ManagedObjectReference cluster, String[] minor, String[] major, Future<VsanHclReleaseConstraint[]> future) {
    throw new NotSupported();
  }
  
  public VsanHclReleaseConstraint[] getClusterReleaseRecommendation(ManagedObjectReference cluster, String[] minor, String[] major) throws NotFound, VsanFault, NotSupported {
    throw new NotSupported();
  }
  
  public void getClusterHclConstraints(ManagedObjectReference cluster, String release, Future<VsanHclReleaseConstraint> future) {
    future.setException((Exception)new NotSupported());
  }
  
  public VsanHclReleaseConstraint getClusterHclConstraints(ManagedObjectReference cluster, String release) throws NotFound, VsanFault, NotSupported {
    throw new NotSupported();
  }
  
  public void rebalanceCluster(ManagedObjectReference paramManagedObjectReference, ManagedObjectReference[] paramArrayOfManagedObjectReference, Future<ManagedObjectReference> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public ManagedObjectReference rebalanceCluster(ManagedObjectReference paramManagedObjectReference, ManagedObjectReference[] paramArrayOfManagedObjectReference) throws NotFound, VsanFault {
    throw new NotSupported();
  }
  
  public void runVmdkLoadTest(ManagedObjectReference paramManagedObjectReference, String paramString1, Integer paramInteger, VsanVmdkLoadTestSpec[] paramArrayOfVsanVmdkLoadTestSpec, String paramString2, Future<ManagedObjectReference> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public ManagedObjectReference runVmdkLoadTest(ManagedObjectReference paramManagedObjectReference, String paramString1, Integer paramInteger, VsanVmdkLoadTestSpec[] paramArrayOfVsanVmdkLoadTestSpec, String paramString2) throws NotFound {
    throw new NotSupported();
  }
  
  public void remediateDataProtectionConfigInCluster(ManagedObjectReference paramManagedObjectReference, Future<ManagedObjectReference> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public ManagedObjectReference remediateDataProtectionConfigInCluster(ManagedObjectReference paramManagedObjectReference) throws NotFound {
    throw new NotSupported();
  }
  
  public void stopRebalanceCluster(ManagedObjectReference paramManagedObjectReference, ManagedObjectReference[] paramArrayOfManagedObjectReference, Future<ManagedObjectReference> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public ManagedObjectReference stopRebalanceCluster(ManagedObjectReference paramManagedObjectReference, ManagedObjectReference[] paramArrayOfManagedObjectReference) throws NotFound, VsanFault {
    throw new NotSupported();
  }
  
  public void queryVsanClusterHealthConfig(ManagedObjectReference paramManagedObjectReference, Future<VsanClusterHealthConfigs> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterHealthConfigs queryVsanClusterHealthConfig(ManagedObjectReference paramManagedObjectReference) {
    throw new NotSupported();
  }
  
  public void attachVsanSupportBundleToSr(ManagedObjectReference paramManagedObjectReference, String paramString, Future<ManagedObjectReference> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public ManagedObjectReference attachVsanSupportBundleToSr(ManagedObjectReference paramManagedObjectReference, String paramString) throws NotFound, VsanFault, NotSupported {
    throw new NotSupported();
  }
  
  public void downloadHclFile(String[] paramArrayOfString, Future<ManagedObjectReference> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public ManagedObjectReference downloadHclFile(String[] paramArrayOfString) throws NotFound, VsanFault, NotSupported {
    throw new NotSupported();
  }
  
  public void queryCaptureVsanPcap(ManagedObjectReference paramManagedObjectReference, int paramInt, VsanClusterHostVmknicMapping[] paramArrayOfVsanClusterHostVmknicMapping, Boolean paramBoolean1, Boolean paramBoolean2, String[] paramArrayOfString, int[] paramArrayOfInt, String paramString, Future<VsanVsanClusterPcapResult> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanVsanClusterPcapResult queryCaptureVsanPcap(ManagedObjectReference paramManagedObjectReference, int paramInt, VsanClusterHostVmknicMapping[] paramArrayOfVsanClusterHostVmknicMapping, Boolean paramBoolean1, Boolean paramBoolean2, String[] paramArrayOfString, int[] paramArrayOfInt, String paramString) throws NotFound {
    throw new NotSupported();
  }
  
  public void queryClusterVmdkWorkloadTypes(Future<VsanStorageWorkloadType[]> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanStorageWorkloadType[] queryClusterVmdkWorkloadTypes() throws NotFound {
    throw new NotSupported();
  }
  
  public void queryPhysicalDiskHealthSummary(ManagedObjectReference paramManagedObjectReference, Future<VsanPhysicalDiskHealthSummary[]> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanPhysicalDiskHealthSummary[] queryPhysicalDiskHealthSummary(ManagedObjectReference paramManagedObjectReference) throws NotFound {
    throw new NotSupported();
  }
  
  public void queryCheckLimits(ManagedObjectReference paramManagedObjectReference, Future<VsanClusterLimitHealthResult> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterLimitHealthResult queryCheckLimits(ManagedObjectReference paramManagedObjectReference) throws NotFound {
    throw new NotSupported();
  }
  
  public void queryDataProtectionCfgSync(ManagedObjectReference paramManagedObjectReference, Future<VsanClusterDataProtectionCfgSyncResult[]> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterDataProtectionCfgSyncResult[] queryDataProtectionCfgSync(ManagedObjectReference paramManagedObjectReference) throws NotFound {
    throw new NotSupported();
  }
  
  public void isRebalanceRunning(ManagedObjectReference paramManagedObjectReference, ManagedObjectReference[] paramArrayOfManagedObjectReference, Future<Boolean> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public boolean isRebalanceRunning(ManagedObjectReference paramManagedObjectReference, ManagedObjectReference[] paramArrayOfManagedObjectReference) throws NotFound {
    throw new NotSupported();
  }
  
  public void setVsanClusterHealthCheckInterval(ManagedObjectReference paramManagedObjectReference, int paramInt, Future<Void> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public void setVsanClusterHealthCheckInterval(ManagedObjectReference paramManagedObjectReference, int paramInt) throws NotFound, VsanFault, NotSupported {
    throw new NotSupported();
  }
  
  public void checkDpdLiveness(ManagedObjectReference paramManagedObjectReference, Future<VsanClusterDpdLivenessResult> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterDpdLivenessResult checkDpdLiveness(ManagedObjectReference paramManagedObjectReference) throws NotFound, VsanFault {
    throw new NotSupported();
  }
  
  public void uninstallCluster(ManagedObjectReference paramManagedObjectReference, String paramString, Future<ManagedObjectReference> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public ManagedObjectReference uninstallCluster(ManagedObjectReference paramManagedObjectReference, String paramString) throws NotFound, VsanFault {
    throw new NotSupported();
  }
  
  public void checkDatastoreUsage(ManagedObjectReference paramManagedObjectReference, Future<VsanClusterDatastoreUsageResult> paramFuture) {
    paramFuture.setException((Exception)new NotSupported());
  }
  
  public VsanClusterDatastoreUsageResult checkDatastoreUsage(ManagedObjectReference paramManagedObjectReference) throws VsanFault {
    throw new NotSupported();
  }
  
  public void queryVsanObjExtAttrs(ManagedObjectReference managedObjectReference, String[] strings, Future<VsanObjectExtAttrs[]> future) {
    future.setException((Exception)new NotSupported());
  }
  
  public VsanObjectExtAttrs[] queryVsanObjExtAttrs(ManagedObjectReference managedObjectReference, String[] strings) throws NotFound {
    throw new NotSupported();
  }
  
  public ManagedObjectReference downloadAndInstallVendorTool(ManagedObjectReference cluster) throws NotFound, VsanFault, NotSupported {
    throw new NotSupported();
  }
  
  public void downloadAndInstallVendorTool(ManagedObjectReference cluster, Future<ManagedObjectReference> future) {
    future.setException((Exception)new NotSupported());
  }
  
  public ManagedObjectReference queryClusterHealthSummaryTask(ManagedObjectReference cluster, ManagedObjectReference[] hosts, Boolean includeDataProtectionHealth) throws NotFound {
    throw new NotSupported();
  }
  
  public void queryClusterHealthSummaryTask(ManagedObjectReference cluster, ManagedObjectReference[] hosts, Boolean includeDataProtectionHealth, Future<ManagedObjectReference> future) {
    future.setException((Exception)new NotSupported());
  }
  
  public VsanClusterHealthSummary[] queryClusterHistoryHealthSummary(VsanHistoryItemQuerySpec spec) throws NotFound, NotSupported {
    throw new NotSupported();
  }
  
  public void queryClusterHistoryHealthSummary(VsanHistoryItemQuerySpec spec, Future<VsanClusterHealthSummary[]> future) {
    future.setException((Exception)new NotSupported());
  }
  
  public VsanClusterFileServiceHealthSummary queryFileServiceHealthSummary(ManagedObjectReference cluster) throws InvalidArgument {
    throw new NotSupported();
  }
  
  public void prepareClusterRebootWithNAMM(ManagedObjectReference cluster, Integer scheduleTime, Future<Boolean> future) {
    throw new NotSupported();
  }
  
  public boolean prepareClusterRebootWithNAMM(ManagedObjectReference cluster, Integer scheduleTime) throws NotFound, VsanFault {
    throw new NotSupported();
  }
  
  public void queryFileServiceHealthSummary(ManagedObjectReference cluster, Future<VsanClusterFileServiceHealthSummary> future) {
    future.setException((Exception)new NotSupported());
  }
  
  public void queryClusterHealthSummary(ManagedObjectReference cluster, Integer vmCreateTimeout, String[] objUuids, Boolean includeObjUuids, String[] fields, Boolean fetchFromCache, String perspective, ManagedObjectReference[] hosts, Boolean includeDataProtectionHealth, Future<VsanClusterHealthSummary> future) {
    throw new NotSupported();
  }
  
  public VsanClusterHealthSummary queryClusterHealthSummary(ManagedObjectReference cluster, Integer vmCreateTimeout, String[] objUuids, Boolean includeObjUuids, String[] fields, Boolean fetchFromCache, String perspective, ManagedObjectReference[] hosts, Boolean includeDataProtectionHealth) throws NotFound, VsanFault {
    throw new NotSupported();
  }
  
  public ManagedObjectReference _getRef() {
    throw new NotSupported();
  }
}
