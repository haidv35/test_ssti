package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.AgentStatus;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentIdProvider;
import com.vmware.ph.phservice.cloud.dataapp.internal.AgentResultRetriever;
import com.vmware.ph.phservice.collector.CollectorOutcome;
import com.vmware.ph.phservice.collector.core.Collector;
import com.vmware.ph.phservice.collector.core.CollectorProvider;
import com.vmware.ph.phservice.collector.internal.scheduler.CollectorLoopExecutionCoordinator;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.collector.scheduler.CollectorLoop;
import com.vmware.ph.phservice.collector.scheduler.CollectorLoopExecutionConfigProvider;
import com.vmware.ph.phservice.collector.scheduler.DefaultCollectorLoop;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.threadstate.ThreadActiveStateManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoopCollectorDataAppAgentWrapper extends BaseCollectorDataAppAgentWrapper {
  static final String DATA_APP_COLLECTOR_THREAD_NAME = "data-app-collector";
  
  private static final Log _log = LogFactory.getLog(LoopCollectorDataAppAgentWrapper.class);
  
  private final CollectorLoopExecutionConfigProvider _loopExecutionConfigProvider;
  
  private final CollectorLoopExecutionCoordinator _loopExecutionCoordinator;
  
  private final AgentResultRetriever _loopResultRetriever;
  
  private final Builder<Map<String, Object>> _objectIdToContextDataBuilder;
  
  private final CollectorLoop _agentCollectorLoop;
  
  private ThreadActiveStateManager _threadActiveStateManager;
  
  public LoopCollectorDataAppAgentWrapper(CollectorDataAppAgent agent, DataAppAgentIdProvider agentIdProvider, CollectorLoopExecutionConfigProvider loopExecutionConfigProvider, CollectorLoopExecutionCoordinator loopExecutionCoordinator, AgentResultRetriever loopResultRetriever, Builder<Map<String, Object>> objectIdToContextDataBuilder, ThreadActiveStateManager threadActiveStateManager) {
    super(agent);
    this._loopExecutionConfigProvider = loopExecutionConfigProvider;
    this._loopExecutionCoordinator = loopExecutionCoordinator;
    this._loopResultRetriever = loopResultRetriever;
    this._objectIdToContextDataBuilder = objectIdToContextDataBuilder;
    this._threadActiveStateManager = threadActiveStateManager;
    this._agentCollectorLoop = createAgentCollectorLoop(agentIdProvider);
    this._agentCollectorLoop.start();
  }
  
  public AgentStatus getAgentStatus() {
    AgentStatus agentStatus = this._wrappedAgent.getAgentStatus();
    agentStatus.setAgentRunning(this._agentCollectorLoop.isRunning());
    return agentStatus;
  }
  
  public void close() {
    if (this._agentCollectorLoop != null)
      try {
        this._agentCollectorLoop.stop();
      } catch (InterruptedException e) {
        _log.warn("Could not stop the data app collector loop. This could lead to resources leak!");
        Thread.currentThread().interrupt();
      }  
    super.close();
  }
  
  CollectorLoop createAgentCollectorLoop(DataAppAgentIdProvider agentIdProvider) {
    DataAppAgentCollectorProvider collectorProvider = new DataAppAgentCollectorProvider();
    String dataAppCollectorLoopName = generateDataAppCollectorThreadName(agentIdProvider);
    DefaultCollectorLoop dataAppCollectorLoop = new DefaultCollectorLoop(collectorProvider, this._loopExecutionConfigProvider, this._loopExecutionCoordinator) {
        protected void runLoop(boolean runOnce) {
          super.runLoop(runOnce);
          flushExceptionsContext();
        }
        
        protected void beforeLoopCycle() {
          ExceptionsContextManager.createCurrentContext();
        }
        
        protected void afterLoopCycle() {
          flushExceptionsContext();
        }
        
        private void flushExceptionsContext() {
          DataAppAgentId agentId = LoopCollectorDataAppAgentWrapper.this.getAgentId();
          if (agentId != null)
            ExceptionsContextManager.flushCurrentContext(agentId); 
        }
      };
    dataAppCollectorLoop.setThreadActiveStateManager(this._threadActiveStateManager);
    dataAppCollectorLoop.setCollectorThreadName(dataAppCollectorLoopName);
    return dataAppCollectorLoop;
  }
  
  CollectorLoop getDataAppCollectorLoop() {
    return this._agentCollectorLoop;
  }
  
  static String generateDataAppCollectorThreadName(DataAppAgentIdProvider agentIdProvider) {
    if (agentIdProvider == null)
      return "data-app-collector"; 
    StringBuilder collectorThreadName = new StringBuilder("data-app-collector");
    String collectorId = agentIdProvider.getCollectorId();
    if (StringUtils.isNotBlank(collectorId)) {
      collectorThreadName.append("-");
      collectorThreadName.append(collectorId);
    } 
    String pluginType = agentIdProvider.getPluginType();
    if (StringUtils.isNotBlank(pluginType)) {
      collectorThreadName.append("-");
      collectorThreadName.append(pluginType);
    } 
    return collectorThreadName.toString();
  }
  
  class DataAppAgentCollectorProvider implements CollectorProvider {
    private AgentCollectionScheduleSpec _agentCollectionScheduleSpec = AgentCollectionScheduleSpec.EMPTY_SPEC;
    
    public boolean isActive() {
      return true;
    }
    
    public Collector getCollector(final CollectionSchedule collectionSchedule) {
      return new Collector() {
          public void run() {
            collect();
          }
          
          public CollectorOutcome collect() {
            Map<String, Object> objectIdToContextData = getObjectIdToContextData(collectionSchedule);
            LoopCollectorDataAppAgentWrapper.this.execute(objectIdToContextData, LoopCollectorDataAppAgentWrapper.this
                
                ._loopResultRetriever, collectionSchedule);
            return CollectorOutcome.PASSED;
          }
          
          public void setContextData(Object contextData) {}
          
          public void close() {}
          
          private Map<String, Object> getObjectIdToContextData(CollectionSchedule collectionSchedule) {
            Map<String, Object> objectIdToContextData = LoopCollectorDataAppAgentWrapper.this._objectIdToContextDataBuilder.build();
            if (objectIdToContextData == null)
              return null; 
            Set<String> objectIdsForSchedule = LoopCollectorDataAppAgentWrapper.DataAppAgentCollectorProvider.this._agentCollectionScheduleSpec.getObjectIds(collectionSchedule);
            Map<String, Object> objectIdToContextDataForSchedule = new HashMap<>(objectIdsForSchedule.size());
            for (String objectId : objectIdsForSchedule) {
              if (objectIdToContextData.containsKey(objectId))
                objectIdToContextDataForSchedule.put(objectId, objectIdToContextData
                    
                    .get(objectId)); 
            } 
            return objectIdToContextDataForSchedule;
          }
        };
    }
    
    public Set<CollectionSchedule> getCollectorSchedules() {
      Map<String, Object> objectIdToContextData = LoopCollectorDataAppAgentWrapper.this._objectIdToContextDataBuilder.build();
      Set<String> objectIds = (objectIdToContextData == null || objectIdToContextData.isEmpty()) ? Collections.<String>singleton(null) : objectIdToContextData.keySet();
      this
        ._agentCollectionScheduleSpec = LoopCollectorDataAppAgentWrapper.this.getCollectionScheduleSpec(objectIds);
      return this._agentCollectionScheduleSpec.getCollectionSchedules();
    }
  }
}
