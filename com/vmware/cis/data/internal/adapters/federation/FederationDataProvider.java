package com.vmware.cis.data.internal.adapters.federation;

import com.vmware.cis.data.internal.provider.AuthenticationTokenSource;
import com.vmware.cis.data.internal.provider.DataProviderConnection;
import com.vmware.cis.data.internal.provider.DataProviderConnector;
import com.vmware.cis.data.internal.provider.MultiSsoDomainAuthenticationTokenSource;
import com.vmware.cis.data.internal.provider.SsoDomain;
import com.vmware.cis.data.internal.provider.schema.QuerySchemaCache;
import com.vmware.cis.data.internal.util.SingleSsoDomainAuthenticationTokenSource;
import com.vmware.cis.data.internal.util.TaskExecutor;
import com.vmware.cis.data.provider.DataProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FederationDataProvider {
  private static final Logger _logger = LoggerFactory.getLogger(FederationDataProvider.class);
  
  private final QueryRouter _queryRouter;
  
  private final TaskExecutor _taskExecutor;
  
  private final QuerySchemaCache _schemaCache;
  
  private final Collection<FederatedInstanceInfo> _federatedInstances;
  
  public static final class FederatedInstanceInfo {
    private final String _providerName;
    
    private final IdentifiableServiceInstance _instanceIdentifier;
    
    private final String _serviceTypeAndVersion;
    
    private final DataProviderConnector _providerConnector;
    
    private final SsoDomain _ssoDomain;
    
    public FederatedInstanceInfo(String providerName, IdentifiableServiceInstance instanceIdentifier, String serviceTypeAndVersion, DataProviderConnector providerConnector, SsoDomain ssoDomain) {
      Validate.notNull(providerName, "Argument 'providerName' is required");
      Validate.notNull(instanceIdentifier, "Argument 'instanceIdentifier' is required");
      Validate.notNull(serviceTypeAndVersion, "Argument 'serviceTypeAndVersion' is required");
      Validate.notNull(providerConnector, "Argument 'providerConnector' is required");
      this._providerName = providerName;
      this._instanceIdentifier = instanceIdentifier;
      this._serviceTypeAndVersion = serviceTypeAndVersion;
      this._providerConnector = providerConnector;
      this._ssoDomain = ssoDomain;
    }
    
    public String getProviderName() {
      return this._providerName;
    }
    
    public IdentifiableServiceInstance getInstanceIdentifier() {
      return this._instanceIdentifier;
    }
    
    public String getServiceTypeAndVersion() {
      return this._serviceTypeAndVersion;
    }
    
    public DataProviderConnector getDataProvider() {
      return this._providerConnector;
    }
    
    public SsoDomain getSsoDomain() {
      return this._ssoDomain;
    }
    
    public String toString() {
      return this._providerName;
    }
  }
  
  public FederationDataProvider(QueryRouter queryRouter, ExecutorService executorService, QuerySchemaCache schemaCache, Collection<FederatedInstanceInfo> providerInfos) {
    Validate.notNull(queryRouter, "The queryRouter is required.");
    Validate.notEmpty(providerInfos, "At least one provider instance is required.");
    this._queryRouter = queryRouter;
    this._federatedInstances = providerInfos;
    this._schemaCache = schemaCache;
    this._taskExecutor = new TaskExecutor(executorService, TaskExecutor.ErrorHandlingPolicy.LENIENT);
  }
  
  public DataProviderConnection getConnection(MultiSsoDomainAuthenticationTokenSource authn) {
    Validate.notNull(authn, "Argument `authn' is required.");
    List<Callable<FederatedConnectionInfo>> connectTasks = createConnectTasks(authn);
    final List<FederatedConnectionInfo> federatedConnectionInfos = this._taskExecutor.invokeTasks(connectTasks);
    final DataProvider federationQueryHanlder = FederationConnection.create(this._queryRouter, this._taskExecutor, this._schemaCache, 
        toProviderInfos(federatedConnectionInfos));
    return new DataProviderConnection() {
        public void close() throws Exception {
          for (FederationDataProvider.FederatedConnectionInfo connectionInfo : federatedConnectionInfos) {
            try {
              connectionInfo.getCloseable().close();
            } catch (Exception e) {
              FederationDataProvider._logger.warn("Exception while closing DataProviderConnection to {}: {}", new Object[] { connectionInfo
                    
                    .getProviderInfo().getProviderName(), e
                    .getMessage(), e });
            } 
          } 
        }
        
        public DataProvider getDataProvider() {
          return federationQueryHanlder;
        }
      };
  }
  
  private static List<FederationConnection.FederatedProviderInfo> toProviderInfos(List<FederatedConnectionInfo> connecitonInfos) {
    List<FederationConnection.FederatedProviderInfo> providerInfos = new ArrayList<>(connecitonInfos.size());
    for (FederatedConnectionInfo connectionInfo : connecitonInfos)
      providerInfos.add(connectionInfo.getProviderInfo()); 
    return providerInfos;
  }
  
  private List<Callable<FederatedConnectionInfo>> createConnectTasks(MultiSsoDomainAuthenticationTokenSource authn) {
    List<Callable<FederatedConnectionInfo>> taskResults = new ArrayList<>(this._federatedInstances.size());
    for (FederatedInstanceInfo instanceInfo : this._federatedInstances) {
      Callable<FederatedConnectionInfo> task = createConnectTask(instanceInfo, authn);
      taskResults.add(task);
    } 
    return taskResults;
  }
  
  private static final class FederatedConnectionInfo {
    private final AutoCloseable _closeable;
    
    private final FederationConnection.FederatedProviderInfo _providerInfo;
    
    public FederatedConnectionInfo(FederationConnection.FederatedProviderInfo providerInfo, AutoCloseable closeable) {
      assert providerInfo != null;
      assert closeable != null;
      this._providerInfo = providerInfo;
      this._closeable = closeable;
    }
    
    public FederationConnection.FederatedProviderInfo getProviderInfo() {
      return this._providerInfo;
    }
    
    public AutoCloseable getCloseable() {
      return this._closeable;
    }
  }
  
  private Callable<FederatedConnectionInfo> createConnectTask(final FederatedInstanceInfo instanceInfo, MultiSsoDomainAuthenticationTokenSource authn) {
    SsoDomain ssoDomain = instanceInfo.getSsoDomain();
    final AuthenticationTokenSource tokenSource = new SingleSsoDomainAuthenticationTokenSource(authn, ssoDomain);
    return new Callable<FederatedConnectionInfo>() {
        public FederationDataProvider.FederatedConnectionInfo call() throws Exception {
          String instanceUuid = instanceInfo.getInstanceIdentifier().getServiceInstanceUuid();
          String serviceTypeAndVersion = instanceInfo.getServiceTypeAndVersion();
          DataProviderConnection connection = instanceInfo.getDataProvider().getConnection(tokenSource);
          FederationConnection.FederatedProviderInfo providerInfo = new FederationConnection.FederatedProviderInfo(instanceInfo.getProviderName(), instanceUuid, serviceTypeAndVersion, connection.getDataProvider());
          return new FederationDataProvider.FederatedConnectionInfo(providerInfo, connection);
        }
        
        public String toString() {
          return "connect to: " + instanceInfo.getProviderName();
        }
      };
  }
  
  public String toString() {
    if (this._federatedInstances.size() > 1)
      return "Federation(" + 
        StringUtils.join(this._federatedInstances, ", ") + ")"; 
    return ((FederatedInstanceInfo)this._federatedInstances.iterator().next()).toString();
  }
}
