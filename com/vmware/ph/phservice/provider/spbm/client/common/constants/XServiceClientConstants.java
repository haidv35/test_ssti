package com.vmware.ph.phservice.provider.spbm.client.common.constants;

import com.vmware.vim.binding.lookup.ServiceRegistration;
import com.vmware.vim.binding.pbm.version.internal.versions;
import com.vmware.vim.binding.sms.version.internal.versions;

public class XServiceClientConstants {
  public static final String PBM_SERVICE_INSTANCE_TYPE = "PbmServiceInstance";
  
  public static final String SMS_SERVICE_INSTANCE_TYPE = "SmsServiceInstance";
  
  public static final String ALL_SERVICE_INSTANCE_VALUE = "ServiceInstance";
  
  public static final ServiceRegistration.EndpointType PBM_ENDPOINT_TYPE = new ServiceRegistration.EndpointType("https", "com.vmware.vim.pbm");
  
  public static final ServiceRegistration.EndpointType SMS_ENDPOINT_TYPE = new ServiceRegistration.EndpointType("https", "com.vmware.vim.sms");
  
  public static final Class<?> DEFAULT_PBM_VERSION_CLASS = versions.PBM_VERSION_LTS;
  
  public static final Class<?> DEFAULT_SMS_VERSION_CLASS = versions.SMS_VERSION_LTS;
  
  public static final String VIM_BINDINGS = "com.vmware.vim.binding.vim";
  
  public static final String CIS_DATA_PROVIDER_BINDINGS = "com.vmware.vim.binding.cis.data.provider";
  
  public static final String PBM_BINDINGS = "com.vmware.vim.binding.pbm";
  
  public static final String SMS_BINDINGS = "com.vmware.vim.binding.sms";
}
