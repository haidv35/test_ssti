package com.vmware.cis.data.internal.adapters.vapi;

import com.vmware.cis.data.internal.adapters.tagging.DefaultTaggableEntityReferenceConverter;
import com.vmware.cis.data.internal.adapters.tagging.TaggableEntityReferenceConverter;
import com.vmware.cis.data.internal.adapters.util.vapi.VapiApiProviderPool;
import com.vmware.cis.data.internal.adapters.vapi.impl.DefaultVapiPropertyValueConverter;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.lang.Validate;

public final class VapiDataProviderConfig {
  private final VapiApiProviderPool contentLibraryApiProviderPool;
  
  private final VapiApiProviderPool computePolicyApiProviderPool;
  
  private final VapiApiProviderPool taggingApiProviderPool;
  
  private final VapiPropertyValueConverter propertyValueConverter;
  
  private final Collection<Class<?>> queryModels;
  
  private final TaggableEntityReferenceConverter entityConverter;
  
  public static final class Builder {
    private final VapiApiProviderPool _contentLibraryApiProviderPool;
    
    private final VapiApiProviderPool _computePolicyApiProviderPool;
    
    private final VapiApiProviderPool _taggingApiProviderPool;
    
    private VapiPropertyValueConverter propertyValueConverter = new DefaultVapiPropertyValueConverter();
    
    private TaggableEntityReferenceConverter _entityConverter = DefaultTaggableEntityReferenceConverter.DEFAULT_CONVERTER;
    
    private Collection<Class<?>> _queryModels;
    
    private Builder(VapiApiProviderPool contentLibraryApiProviderPool, VapiApiProviderPool computePolicyApiProviderPool, VapiApiProviderPool taggingApiProviderPool) {
      assert contentLibraryApiProviderPool != null;
      assert computePolicyApiProviderPool != null;
      assert taggingApiProviderPool != null;
      this._contentLibraryApiProviderPool = contentLibraryApiProviderPool;
      this._computePolicyApiProviderPool = computePolicyApiProviderPool;
      this._taggingApiProviderPool = taggingApiProviderPool;
    }
    
    public static Builder create(VapiApiProviderPool contentLibraryApiProviderPool, VapiApiProviderPool computePolicyApiProviderPool, VapiApiProviderPool taggingApiProviderPool) {
      assert contentLibraryApiProviderPool != null;
      assert computePolicyApiProviderPool != null;
      assert taggingApiProviderPool != null;
      return new Builder(contentLibraryApiProviderPool, computePolicyApiProviderPool, taggingApiProviderPool);
    }
    
    public Builder withPropertyValueConverter(VapiPropertyValueConverter propertyValueConverter) {
      Validate.notNull(propertyValueConverter);
      this.propertyValueConverter = propertyValueConverter;
      return this;
    }
    
    public Builder withExtendedModels(Collection<Class<?>> queryModels) {
      Validate.notNull(queryModels);
      this._queryModels = queryModels;
      return this;
    }
    
    public Builder withTaggableEntityReferenceConverter(TaggableEntityReferenceConverter entityConverter) {
      Validate.notNull(entityConverter);
      this._entityConverter = entityConverter;
      return this;
    }
    
    public VapiDataProviderConfig build() {
      Collection<Class<?>> queryModelsToUse = (this._queryModels == null) ? Collections.<Class<?>>emptyList() : this._queryModels;
      return new VapiDataProviderConfig(this._contentLibraryApiProviderPool, this._computePolicyApiProviderPool, this._taggingApiProviderPool, this.propertyValueConverter, queryModelsToUse, this._entityConverter);
    }
  }
  
  private VapiDataProviderConfig(VapiApiProviderPool contentLibraryApiProviderPool, VapiApiProviderPool computePolicyApiProviderPool, VapiApiProviderPool taggingApiProviderPool, VapiPropertyValueConverter valueConverter, Collection<Class<?>> queryModels, TaggableEntityReferenceConverter entityConverter) {
    assert contentLibraryApiProviderPool != null;
    assert computePolicyApiProviderPool != null;
    assert taggingApiProviderPool != null;
    assert valueConverter != null;
    assert queryModels != null;
    assert entityConverter != null;
    this.contentLibraryApiProviderPool = contentLibraryApiProviderPool;
    this.computePolicyApiProviderPool = computePolicyApiProviderPool;
    this.taggingApiProviderPool = taggingApiProviderPool;
    this.propertyValueConverter = valueConverter;
    this.queryModels = queryModels;
    this.entityConverter = entityConverter;
  }
  
  public VapiPropertyValueConverter getPropertyValueConverter() {
    return this.propertyValueConverter;
  }
  
  public Collection<Class<?>> getExtendedModels() {
    return this.queryModels;
  }
  
  public TaggableEntityReferenceConverter getTaggableEntityReferenceConverter() {
    return this.entityConverter;
  }
  
  public VapiApiProviderPool getContentLibraryApiProviderPool() {
    return this.contentLibraryApiProviderPool;
  }
  
  public VapiApiProviderPool getComputePolicyApiProviderPool() {
    return this.computePolicyApiProviderPool;
  }
  
  public VapiApiProviderPool getTaggingApiProviderPool() {
    return this.taggingApiProviderPool;
  }
}
