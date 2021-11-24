package com.vmware.ph.phservice.cloud.dataapp.internal.collector;

import com.vmware.ph.phservice.cloud.dataapp.AgentStatus;
import com.vmware.ph.phservice.common.internal.JsonUtil;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

public class AsyncCollectorDataAppAgentImpl extends BaseCollectorDataAppAgentWrapper implements AsyncCollectorDataAppAgent {
  private LocalDateTime _lastCollectedTime = LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC);
  
  private ExecutorService executorService = Executors.newSingleThreadExecutor((ThreadFactory)(new BasicThreadFactory.Builder())
      
      .namingPattern("async-collector-data-app-agent-%d")
      .daemon(true)
      .build());
  
  public AsyncCollectorDataAppAgentImpl(CollectorDataAppAgent wrappedAgent) {
    super(wrappedAgent);
  }
  
  public AgentStatus getAgentStatus() {
    AgentStatus agentStatus = super.getAgentStatus();
    agentStatus.setLastCollectedTime(this._lastCollectedTime
        .toInstant(ZoneOffset.UTC).toEpochMilli());
    return agentStatus;
  }
  
  public Future<String> collectAsync(String manifestContent, String objectId, Object contextData) {
    Callable<String> collectCallable = () -> {
        String result = this._wrappedAgent.collect(manifestContent, objectId, contextData);
        this._lastCollectedTime = LocalDateTime.now(ZoneOffset.UTC);
        return JsonUtil.toPrettyJson(result);
      };
    Future<String> collectFuture = this.executorService.submit(collectCallable);
    return collectFuture;
  }
}
