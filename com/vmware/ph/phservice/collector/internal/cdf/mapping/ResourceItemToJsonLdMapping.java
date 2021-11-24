package com.vmware.ph.phservice.collector.internal.cdf.mapping;

import com.vmware.ph.client.api.commondataformat20.types.JsonLd;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityHelper;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityJsonLd;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityJsonLdToJsonLdConverter;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityPatternEvaluator;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.velocity.VelocityPatternEvaluatorFactory;
import com.vmware.ph.phservice.collector.internal.data.NamedPropertiesResourceItem;
import com.vmware.ph.phservice.common.cdf.jsonld20.VmodlToJsonLdSerializer;
import com.vmware.ph.phservice.common.vmomi.VmodlContextProvider;
import com.vmware.ph.phservice.provider.common.internal.Context;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResourceItemToJsonLdMapping implements Mapping<NamedPropertiesResourceItem, Collection<JsonLd>> {
  private static final Log _log = LogFactory.getLog(ResourceItemToJsonLdMapping.class);
  
  protected String _forType;
  
  protected String _mappingCode;
  
  private VelocityJsonLd _velocityJsonLd;
  
  private VelocityPatternEvaluatorFactory _velocityPatternEvaluatorFactory;
  
  public ResourceItemToJsonLdMapping(String forType, String mappingCode, VelocityPatternEvaluatorFactory velocityPatternEvaluatorFactory) {
    this._forType = forType;
    this._mappingCode = mappingCode;
    this._velocityPatternEvaluatorFactory = velocityPatternEvaluatorFactory;
    this._velocityJsonLd = new VelocityJsonLd();
  }
  
  public Collection<JsonLd> map(NamedPropertiesResourceItem input, Context context) {
    if (validateInput(input)) {
      if (_log.isDebugEnabled())
        _log.debug("The specified input object matches all expectations, now will try to produce result for it."); 
    } else {
      return Collections.emptyList();
    } 
    List<JsonLd> results = Collections.emptyList();
    if (!this._mappingCode.isEmpty()) {
      String expressionResult = evaluateMappingExpression(input, context);
      if (null != expressionResult) {
        if (_log.isDebugEnabled())
          _log.debug("Execution of the velocity expression completed successfully, as a result " + this._velocityJsonLd.object + " object was produced. I start serializing them."); 
        VmodlContext vimVmodlContext = VmodlContextProvider.getVmodlContextForPacakgesAndClassLoader(new String[] { "com.vmware.vim.binding.vim", "com.vmware.vim.vsan.binding.vim" }, getClass().getClassLoader(), false);
        VmodlToJsonLdSerializer serializer = new VmodlToJsonLdSerializer(vimVmodlContext.getVmodlTypeMap(), Arrays.asList(new String[] { "com.vmware.vim.binding.vim", "com.vmware.vim.vsan.binding.vim" }));
        JsonLd jsonLd = (new VelocityJsonLdToJsonLdConverter()).convert(this._velocityJsonLd.object, serializer);
        results = Collections.singletonList(jsonLd);
      } else if (_log.isDebugEnabled()) {
        _log.debug("Execution of the velocity expression did not complete successfully, return nothing (null).");
      } 
    } else if (_log.isDebugEnabled()) {
      _log.debug("This mapping contains no velocity code to produce results. Skipping this mapping: " + this);
    } 
    return Collections.unmodifiableList(results);
  }
  
  protected boolean validateInput(NamedPropertiesResourceItem input) {
    String resultType;
    Object resourceObject = input.getResourceObject();
    if (resourceObject instanceof ManagedObjectReference) {
      ManagedObjectReference moRef = (ManagedObjectReference)resourceObject;
      resultType = moRef.getType();
    } else {
      resultType = resourceObject.getClass().getName();
    } 
    if (!this._forType.isEmpty() && !this._forType.equals(resultType)) {
      if (_log.isDebugEnabled())
        _log.debug(getClass().getSimpleName() + " mapping is configured to process query results of type '" + this._forType + "' but was invoked with result of type  '" + resultType + "'. Skipping this mapping: " + this); 
      return false;
    } 
    return true;
  }
  
  private String evaluateMappingExpression(NamedPropertiesResourceItem input, Context context) {
    Map<String, Object> objectsToAdd = new HashMap<>();
    objectsToAdd.putAll((Map<? extends String, ?>)context);
    objectsToAdd.putAll(VelocityHelper.prepareVelocityPropertiesForResourceItemsThatMayContainMoref(input.getResourceItem()));
    objectsToAdd.putAll(VelocityHelper.prepareVelocityPropertiesFromResourceItem(input));
    objectsToAdd.put("LOCAL-cdf20Result", this._velocityJsonLd);
    VelocityPatternEvaluator velocityPatternEvaluator = this._velocityPatternEvaluatorFactory.create(objectsToAdd, input);
    String logTag = "[" + getClass().getSimpleName() + " forType=" + this._forType + "]";
    return velocityPatternEvaluator.evaluateMappingPattern(this._mappingCode, logTag);
  }
  
  public String toString() {
    return getClass().getSimpleName() + " [forType=" + this._forType + ", mappingCode=" + this._mappingCode + "]";
  }
  
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
  
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(obj, this);
  }
}
