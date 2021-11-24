package com.vmware.cis.data.internal.adapters.federation;

import com.vmware.cis.data.internal.adapters.vmomi.impl.VlsiClientUtil;
import com.vmware.vim.binding.vim.AboutInfo;
import com.vmware.vim.binding.vim.ServiceInstance;
import com.vmware.vim.binding.vim.version.internal.version8;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.Client;
import com.vmware.vim.vmomi.client.ClientConfiguration;
import com.vmware.vim.vmomi.client.http.HttpClientConfiguration;
import com.vmware.vim.vmomi.client.http.HttpConfiguration;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import com.vmware.vim.vmomi.core.types.VmodlType;
import java.net.URI;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VcServiceInstanceIdentifier implements IdentifiableServiceInstance {
  private static final Logger _logger = LoggerFactory.getLogger(VcServiceInstanceIdentifier.class);
  
  private final URI _vmomiUri;
  
  private final HttpConfiguration _vlsiHttpConfig;
  
  private final VmodlContext _vmodlContext;
  
  private final String _instanceUuid;
  
  private final String _version;
  
  private final String _build;
  
  public VcServiceInstanceIdentifier(VmodlContext vmodlContext, URI vmomiUri, HttpConfiguration vlsiHttpConfig) {
    Validate.notNull(vmodlContext, "Argument `vmodlContext' is required.");
    Validate.notNull(vmomiUri, "Argument `vmomiUri' is required.");
    Validate.notNull(vlsiHttpConfig, "Argument `vlsiHttpConfig' is required.");
    this._vmomiUri = vmomiUri;
    this._vlsiHttpConfig = vlsiHttpConfig;
    this._vmodlContext = vmodlContext;
    AboutInfo vcAboutInfo = getVcAboutInfo();
    this._instanceUuid = (vcAboutInfo == null) ? null : vcAboutInfo.getInstanceUuid();
    this._version = (vcAboutInfo == null) ? null : vcAboutInfo.getVersion();
    this._build = (vcAboutInfo == null) ? null : vcAboutInfo.getBuild();
  }
  
  public String getServiceInstanceUuid() {
    return this._instanceUuid;
  }
  
  public String getVersion() {
    return this._version;
  }
  
  public String getBuild() {
    return this._build;
  }
  
  private AboutInfo getVcAboutInfo() {
    AboutInfo vcAboutInfo = null;
    Client client = createClient();
    try {
      ServiceInstance serviceInstance = getServiceInstance(client);
      vcAboutInfo = serviceInstance.getContent().getAbout();
    } catch (Exception e) {
      _logger.error("Failed to connect to VPXD at '{}'", this._vmomiUri, e);
    } finally {
      client.shutdown();
    } 
    return vcAboutInfo;
  }
  
  private Client createClient() {
    HttpClientConfiguration clientConfig = HttpClientConfiguration.Factory.newInstance();
    clientConfig.setHttpConfiguration(this._vlsiHttpConfig);
    Client client = Client.Factory.createClient(this._vmomiUri, version8.class, this._vmodlContext, (ClientConfiguration)clientConfig);
    return client;
  }
  
  private ServiceInstance getServiceInstance(Client vlsiClient) {
    VmodlType srvInstType = this._vmodlContext.getVmodlTypeMap().getVmodlType(ServiceInstance.class);
    ManagedObjectReference ref = new ManagedObjectReference(srvInstType.getWsdlName(), "ServiceInstance", null);
    return VlsiClientUtil.<ServiceInstance>createStub(vlsiClient, ServiceInstance.class, ref);
  }
}
