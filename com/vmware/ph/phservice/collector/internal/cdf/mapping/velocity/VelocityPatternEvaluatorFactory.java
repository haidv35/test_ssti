package com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity;

import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class VelocityPatternEvaluatorFactory {
  public VelocityPatternEvaluator create(Map<String, Object> objectsToAdd) {
    VelocityContext velocityContext = createBaseVelocityContext(objectsToAdd);
    VelocityEngine velocityEngine = VelocityHelper.createVelocityEngine();
    return new VelocityPatternEvaluator(velocityContext, velocityEngine);
  }
  
  public VelocityPatternEvaluator create(Map<String, Object> objectsToAdd, NamedPropertiesResourceItem input) {
    VelocityContext velocityContext = createBaseVelocityContext(objectsToAdd);
    if (input != null)
      VelocityHelper.updateContextWith(velocityContext, input); 
    VelocityEngine velocityEngine = VelocityHelper.createVelocityEngine();
    return new VelocityPatternEvaluator(velocityContext, velocityEngine);
  }
  
  public VelocityPatternEvaluator create(Map<String, Object> objectsToAdd, Payload.Builder payloadBuilder) {
    VelocityContext velocityContext = createBaseVelocityContext(objectsToAdd);
    if (payloadBuilder != null)
      VelocityHelper.updateContextWith(velocityContext, payloadBuilder); 
    VelocityEngine velocityEngine = VelocityHelper.createVelocityEngine();
    return new VelocityPatternEvaluator(velocityContext, velocityEngine);
  }
  
  private static VelocityContext createBaseVelocityContext(Map<String, Object> objectsToAdd) {
    VelocityContext velocityContext = VelocityHelper.createVelocityContextWithPredefinedGlobalObjects();
    if (objectsToAdd != null)
      VelocityHelper.updateContextWith(velocityContext, objectsToAdd); 
    return velocityContext;
  }
}
