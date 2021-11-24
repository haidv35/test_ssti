package com.vmware.ph.phservice.common.ph;

import com.vmware.ph.client.api.PhClient;
import com.vmware.ph.client.api.commondataformat.dimensions.Collector;
import com.vmware.ph.client.api.exceptions.PhClientConnectionException;
import com.vmware.ph.exceptions.collector.InvalidCollectorException;
import com.vmware.ph.exceptions.resource.ResourceNotFoundException;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;

public class UnsignedManifestContentProvider implements ManifestContentProvider {
  private final PhClientFactory _phClientFactory;
  
  public UnsignedManifestContentProvider(PhClientFactory phClientFactory) {
    this._phClientFactory = phClientFactory;
  }
  
  public boolean isEnabled() {
    return true;
  }
  
  public String getManifestContent(String collectorId, String collectorInstanceId) throws ManifestContentProvider.ManifestException {
    String content;
    try (PhClient phClient = this._phClientFactory.create(new Collector(collectorId, collectorInstanceId))) {
      content = phClient.getManifest(collectorId, collectorInstanceId);
    } catch (InvalidCollectorException e) {
      throw (ManifestContentProvider.ManifestException)ExceptionsContextManager.store(new ManifestContentProvider.ManifestException(ManifestContentProvider.ManifestExceptionType.INVALID_COLLECTOR_ERROR, e));
    } catch (ResourceNotFoundException e) {
      throw (ManifestContentProvider.ManifestException)ExceptionsContextManager.store(new ManifestContentProvider.ManifestException(ManifestContentProvider.ManifestExceptionType.MANIFEST_NOT_FOUND_ERROR, e));
    } catch (PhClientConnectionException|RuntimeException e) {
      throw (ManifestContentProvider.ManifestException)ExceptionsContextManager.store(new ManifestContentProvider.ManifestException(ManifestContentProvider.ManifestExceptionType.GENERAL_ERROR, e));
    } 
    return content;
  }
}
