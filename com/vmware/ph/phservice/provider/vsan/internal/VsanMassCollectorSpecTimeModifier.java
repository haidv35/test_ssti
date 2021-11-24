package com.vmware.ph.phservice.provider.vsan.internal;

import com.vmware.ph.phservice.common.Pair;
import com.vmware.ph.phservice.common.internal.TimeIntervalUtil;
import com.vmware.vim.binding.vim.TaskFilterSpec;
import com.vmware.vim.binding.vim.event.EventFilterSpec;
import com.vmware.vim.binding.vmodl.KeyAnyValue;
import com.vmware.vim.vsan.binding.vim.VsanMassCollectorPropertyParams;
import com.vmware.vim.vsan.binding.vim.VsanMassCollectorSpec;
import com.vmware.vim.vsan.binding.vim.cluster.VsanPerfQuerySpec;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.SerializationUtils;

public class VsanMassCollectorSpecTimeModifier {
  private static final long DEFAULT_PERF_QUERY_INTERVAL_MILLIS = 3600000L;
  
  private static final String PERF_QUERY_SPEC_PROPERTY_PARAM_NAME = "querySpecs";
  
  private static final Set<String> VSAN_PERF_PROPERTIES = new HashSet<>();
  
  static {
    VSAN_PERF_PROPERTIES.add("vsanPerf");
  }
  
  private static final Set<String> EVENT_PROPERTIES = new HashSet<>();
  
  static {
    EVENT_PROPERTIES.add("hostEvents");
    EVENT_PROPERTIES.add("clusterEvents");
  }
  
  private static final Set<String> TASK_PROPERTIES = new HashSet<>();
  
  private final Pair<Calendar, Calendar> _queryInterval;
  
  static {
    TASK_PROPERTIES.add("hostTasks");
    TASK_PROPERTIES.add("clusterTasks");
  }
  
  public VsanMassCollectorSpecTimeModifier(long startTimeMillis, long endTimeMillis) {
    this._queryInterval = TimeIntervalUtil.createIntervalFromTimestamps(startTimeMillis, endTimeMillis);
  }
  
  public void synchronizeSpec(VsanMassCollectorSpec massCollectorSpec) {
    processVsanPerfProperties(massCollectorSpec);
    processEventProperties(massCollectorSpec);
    processTaskProperties(massCollectorSpec);
  }
  
  private void processVsanPerfProperties(VsanMassCollectorSpec massCollectorSpec) {
    List<VsanMassCollectorPropertyParams> vsanPerfPropertyParams = getPropertyParamsForProperties(massCollectorSpec, VSAN_PERF_PROPERTIES);
    List<VsanMassCollectorPropertyParams> updatedVsanPerfPropertyParams = processVsanPerfProperties(vsanPerfPropertyParams);
    List<VsanMassCollectorPropertyParams> allNonVsanPerfPropertyParams = getPropertyParamsExcludingProperties(massCollectorSpec, VSAN_PERF_PROPERTIES);
    List<VsanMassCollectorPropertyParams> updatedPropertyParams = new ArrayList<>();
    updatedPropertyParams.addAll(allNonVsanPerfPropertyParams);
    updatedPropertyParams.addAll(updatedVsanPerfPropertyParams);
    massCollectorSpec.setPropertiesParams(updatedPropertyParams
        .<VsanMassCollectorPropertyParams>toArray(new VsanMassCollectorPropertyParams[0]));
  }
  
  private void processEventProperties(VsanMassCollectorSpec massCollectorSpec) {
    List<VsanMassCollectorPropertyParams> allEventPropertyParams = new ArrayList<>();
    allEventPropertyParams.addAll(
        getPropertyParamsForProperties(massCollectorSpec, EVENT_PROPERTIES));
    for (VsanMassCollectorPropertyParams eventPropertyParams : allEventPropertyParams) {
      for (KeyAnyValue propertyParam : eventPropertyParams.getPropertyParams()) {
        if (propertyParam.getValue() instanceof EventFilterSpec)
          updateEventStartEndTime((EventFilterSpec)propertyParam.getValue()); 
      } 
    } 
  }
  
  private void processTaskProperties(VsanMassCollectorSpec massCollectorSpec) {
    List<VsanMassCollectorPropertyParams> allTaskPropertyParams = new ArrayList<>();
    allTaskPropertyParams.addAll(
        getPropertyParamsForProperties(massCollectorSpec, TASK_PROPERTIES));
    for (VsanMassCollectorPropertyParams taskPropertyParams : allTaskPropertyParams) {
      for (KeyAnyValue propertyParam : taskPropertyParams.getPropertyParams()) {
        if (propertyParam.getValue() instanceof TaskFilterSpec) {
          TaskFilterSpec taskFilterSpec = (TaskFilterSpec)propertyParam.getValue();
          updateTaskStartEndTime(taskFilterSpec);
        } 
      } 
    } 
  }
  
  private void updateEventStartEndTime(EventFilterSpec eventFilterSpec) {
    EventFilterSpec.ByTime eventTime = eventFilterSpec.getTime();
    if (eventTime != null) {
      eventTime.setBeginTime((Calendar)this._queryInterval.getFirst());
      eventTime.setEndTime((Calendar)this._queryInterval.getSecond());
    } 
  }
  
  private void updateTaskStartEndTime(TaskFilterSpec taskFilterSpec) {
    TaskFilterSpec.ByTime taskTime = taskFilterSpec.getTime();
    if (taskTime != null) {
      taskTime.setBeginTime((Calendar)this._queryInterval.getFirst());
      taskTime.setEndTime((Calendar)this._queryInterval.getSecond());
    } 
  }
  
  private List<VsanMassCollectorPropertyParams> processVsanPerfProperties(List<VsanMassCollectorPropertyParams> allPerfPropertyParams) {
    List<VsanMassCollectorPropertyParams> resultPropertyParams = new ArrayList<>();
    List<String> processedEntityRefIds = new ArrayList<>();
    for (VsanMassCollectorPropertyParams perfPropertyParams : allPerfPropertyParams) {
      List<VsanPerfQuerySpec> uniquePerfQuerySpecs = new ArrayList<>();
      for (KeyAnyValue propertyParam : perfPropertyParams.getPropertyParams()) {
        if (propertyParam.getValue() instanceof VsanPerfQuerySpec[]) {
          VsanPerfQuerySpec[] perfQuerySpecs = (VsanPerfQuerySpec[])propertyParam.getValue();
          List<VsanPerfQuerySpec> notProcessedPerfQuerySpecs = filterNotProcessedPerfQuerySpecs(processedEntityRefIds, perfQuerySpecs);
          uniquePerfQuerySpecs.addAll(notProcessedPerfQuerySpecs);
        } 
      } 
      List<VsanMassCollectorPropertyParams> newPerfPropertyParams = buildNewPerfPropertyParams(perfPropertyParams, uniquePerfQuerySpecs);
      resultPropertyParams.addAll(newPerfPropertyParams);
    } 
    return resultPropertyParams;
  }
  
  private List<VsanMassCollectorPropertyParams> buildNewPerfPropertyParams(VsanMassCollectorPropertyParams perfPropertyParams, List<VsanPerfQuerySpec> uniquePerfQuerySpecs) {
    List<Pair<Calendar, Calendar>> perfQueryIntervals = TimeIntervalUtil.splitInterval(this._queryInterval, 3600000L);
    List<VsanMassCollectorPropertyParams> newPerfPropertyParams = new ArrayList<>();
    for (VsanPerfQuerySpec perfQuerySpec : uniquePerfQuerySpecs) {
      for (Pair<Calendar, Calendar> perfQueryInterval : perfQueryIntervals) {
        VsanMassCollectorPropertyParams propertyParamForInterval = clonePropertyParamsForInterval(perfPropertyParams, perfQuerySpec, perfQueryInterval);
        newPerfPropertyParams.add(propertyParamForInterval);
      } 
    } 
    return newPerfPropertyParams;
  }
  
  private static List<VsanPerfQuerySpec> filterNotProcessedPerfQuerySpecs(List<String> processedEntityRefIds, VsanPerfQuerySpec[] perfQuerySpecs) {
    List<VsanPerfQuerySpec> notProcessedPerfQuerySpecs = new ArrayList<>();
    for (VsanPerfQuerySpec perfQuerySpec : perfQuerySpecs) {
      String entityRefId = perfQuerySpec.getEntityRefId();
      if (!processedEntityRefIds.contains(entityRefId)) {
        processedEntityRefIds.add(entityRefId);
        notProcessedPerfQuerySpecs.add(perfQuerySpec);
      } 
    } 
    return notProcessedPerfQuerySpecs;
  }
  
  private static VsanMassCollectorPropertyParams clonePropertyParamsForInterval(VsanMassCollectorPropertyParams propertyParams, VsanPerfQuerySpec perfQuerySpec, Pair<Calendar, Calendar> perfQueryInterval) {
    VsanMassCollectorPropertyParams newPropertyParams = (VsanMassCollectorPropertyParams)SerializationUtils.clone((Serializable)propertyParams);
    List<VsanPerfQuerySpec> replacementQuerySpecs = new ArrayList<>();
    VsanPerfQuerySpec newPerfQuerySpec = clonePerfQuerySpecForInterval(perfQuerySpec, perfQueryInterval);
    replacementQuerySpecs.add(newPerfQuerySpec);
    replacePerfQuerySpecs(newPropertyParams, replacementQuerySpecs);
    return newPropertyParams;
  }
  
  private static VsanPerfQuerySpec clonePerfQuerySpecForInterval(VsanPerfQuerySpec perfQuerySpec, Pair<Calendar, Calendar> interval) {
    VsanPerfQuerySpec newPerfQuerySpec = (VsanPerfQuerySpec)SerializationUtils.clone((Serializable)perfQuerySpec);
    newPerfQuerySpec.setStartTime((Calendar)interval.getFirst());
    newPerfQuerySpec.setEndTime((Calendar)interval.getSecond());
    return newPerfQuerySpec;
  }
  
  private static void replacePerfQuerySpecs(VsanMassCollectorPropertyParams propertyParams, List<VsanPerfQuerySpec> newPerfQuerySpecs) {
    if (newPerfQuerySpecs == null)
      return; 
    for (KeyAnyValue propertyParamEntry : propertyParams.getPropertyParams()) {
      if ("querySpecs".equals(propertyParamEntry.getKey()))
        propertyParamEntry.setValue(newPerfQuerySpecs
            .toArray(new VsanPerfQuerySpec[0])); 
    } 
  }
  
  private static List<VsanMassCollectorPropertyParams> getPropertyParamsForProperties(VsanMassCollectorSpec massCollectorSpec, Set<String> includedProperties) {
    String[] properties = massCollectorSpec.getProperties();
    if (properties == null)
      return Collections.emptyList(); 
    List<VsanMassCollectorPropertyParams> selectedPropertyParams = new ArrayList<>();
    List<String> massCollectorSpecProperties = Arrays.asList(properties);
    for (String property : includedProperties) {
      if (massCollectorSpecProperties.contains(property))
        for (VsanMassCollectorPropertyParams propertyParams : massCollectorSpec.getPropertiesParams()) {
          if (includedProperties.contains(propertyParams.getPropertyName()))
            selectedPropertyParams.add(propertyParams); 
        }  
    } 
    return selectedPropertyParams;
  }
  
  private static List<VsanMassCollectorPropertyParams> getPropertyParamsExcludingProperties(VsanMassCollectorSpec massCollectorSpec, Set<String> excludedProperties) {
    VsanMassCollectorPropertyParams[] propertiesParams = massCollectorSpec.getPropertiesParams();
    if (propertiesParams == null)
      return Collections.emptyList(); 
    List<VsanMassCollectorPropertyParams> selectedPropertyParams = new ArrayList<>();
    for (VsanMassCollectorPropertyParams propertyParams : propertiesParams) {
      if (!excludedProperties.contains(propertyParams.getPropertyName()))
        selectedPropertyParams.add(propertyParams); 
    } 
    return selectedPropertyParams;
  }
}
