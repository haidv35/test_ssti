package com.vmware.ph.phservice.provider.vcenter.license.collector.dataretriever;

import com.vmware.ph.phservice.provider.common.DataProviderUtil;
import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.vcenter.license.client.LicenseClient;
import com.vmware.vim.binding.cis.license.management.SystemManagementService;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LicenseServiceDataRetrieverImpl implements DataRetriever<Object> {
  private static final String SEARCH_METHOD_TEMPLATE = "search%ss";
  
  private static final int SINGLE_PARAMETER = 1;
  
  private static final String ID_PROPERTY = "id";
  
  private static final Map<String, String> ID_PROPERTY_MAP;
  
  private static final Log _log = LogFactory.getLog(LicenseServiceDataRetrieverImpl.class);
  
  private final LicenseClient _licenseClient;
  
  private final String _resourceModelType;
  
  static {
    ID_PROPERTY_MAP = new HashMap<>();
    ID_PROPERTY_MAP.put("LicenseUtilizationImpl", "license/id");
    ID_PROPERTY_MAP.put("ProductUtilizationImpl", "product/id");
  }
  
  public LicenseServiceDataRetrieverImpl(LicenseClient licenseClient, String resourceModelType) {
    this._licenseClient = licenseClient;
    this._resourceModelType = resourceModelType;
  }
  
  public List<Object> retrieveData() {
    Object[] retrievedObjects = searchForResourceModelObjects();
    List<Object> retrievedData = null;
    if (retrievedObjects != null) {
      retrievedData = Arrays.asList(retrievedObjects);
    } else {
      retrievedData = Collections.emptyList();
    } 
    return retrievedData;
  }
  
  public String getKey(Object vmodlObject) {
    String propertyName = ID_PROPERTY_MAP.get(vmodlObject.getClass().getSimpleName());
    if (propertyName == null)
      propertyName = "id"; 
    Object key = DataProviderUtil.getPropertyValue(vmodlObject, propertyName);
    return key.toString();
  }
  
  private Object[] searchForResourceModelObjects() {
    Object[] dataObjects = null;
    try {
      Method searchMethod = acquireSearchMethod();
      if (searchMethod != null) {
        dataObjects = (Object[])searchMethod.invoke(this._licenseClient
            .getSystemManagementService(), new Object[] { null });
      } else if (_log.isDebugEnabled()) {
        _log.debug(String.format("Cannot find search method for resource model %s", new Object[] { this._resourceModelType }));
      } 
    } catch (IllegalAccessException|IllegalArgumentException|java.lang.reflect.InvocationTargetException|com.vmware.vim.vmomi.client.exception.ConnectionException e) {
      if (_log.isDebugEnabled())
        _log.debug(String.format("Invoking the search method for resource %s failed", new Object[] { this._resourceModelType }), e); 
    } 
    return dataObjects;
  }
  
  private Method acquireSearchMethod() {
    String targetSearchMethodName = String.format("search%ss", new Object[] { this._resourceModelType });
    Class<?> systemManagementServiceClass = SystemManagementService.class;
    Method[] systemManagementServiceMethods = systemManagementServiceClass.getMethods();
    if (systemManagementServiceMethods == null)
      return null; 
    Method searchMethod = null;
    for (Method method : systemManagementServiceMethods) {
      if (method.getName().equals(targetSearchMethodName) && (method
        .getParameterTypes()).length == 1) {
        searchMethod = method;
        break;
      } 
    } 
    return searchMethod;
  }
}
