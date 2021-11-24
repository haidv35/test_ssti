package com.vmware.ph.phservice.common.ph;

import com.vmware.ph.client.api.PhClient;
import com.vmware.ph.client.api.commondataformat.dimensions.Collector;
import com.vmware.ph.client.api.exceptions.PhClientConnectionException;
import com.vmware.ph.exceptions.collector.InvalidCollectorException;
import com.vmware.ph.exceptions.resource.ResourceNotFoundException;
import com.vmware.ph.phservice.common.exceptionsCollection.ExceptionsContextManager;
import com.vmware.ph.phservice.common.manifest.ManifestContentProvider;
import com.vmware.ph.phservice.common.ph.internal.ManifestSignatureValidator;
import com.vmware.ph.phservice.common.ph.internal.signature.ManifestSignatureGeneralException;

public class PhManifestContentProvider implements ManifestContentProvider {
  private final PhClientFactory _phClientFactory;
  
  private final ManifestSignatureValidator _manifestValidator;
  
  public PhManifestContentProvider(PhClientFactory phClientFactory, ManifestSignatureValidator manifestValidator) {
    this._phClientFactory = phClientFactory;
    this._manifestValidator = manifestValidator;
  }
  
  public boolean isEnabled() {
    return true;
  }
  
  public String getManifestContent(String collectorId, String collectorInstanceId) throws ManifestContentProvider.ManifestException {
    String content;
    try (PhClient phClient = this._phClientFactory.create(new Collector(collectorId, collectorInstanceId))) {
      String rawContent = phClient.getManifest(collectorId, collectorInstanceId);
      content = this._manifestValidator.verifyManifest(rawContent);
    } catch (InvalidCollectorException e) {
      throw (ManifestContentProvider.ManifestException)ExceptionsContextManager.store(new ManifestContentProvider.ManifestException(ManifestContentProvider.ManifestExceptionType.INVALID_COLLECTOR_ERROR, e));
    } catch (ResourceNotFoundException e) {
      throw (ManifestContentProvider.ManifestException)ExceptionsContextManager.store(new ManifestContentProvider.ManifestException(ManifestContentProvider.ManifestExceptionType.MANIFEST_NOT_FOUND_ERROR, e));
    } catch (PhClientConnectionException|RuntimeException e) {
      throw (ManifestContentProvider.ManifestException)ExceptionsContextManager.store(new ManifestContentProvider.ManifestException(ManifestContentProvider.ManifestExceptionType.GENERAL_ERROR, e));
    } catch (ManifestSignatureGeneralException|com.vmware.ph.phservice.common.ph.internal.signature.ManifestSignatureException e) {
      throw (ManifestContentProvider.ManifestException)ExceptionsContextManager.store(new ManifestContentProvider.ManifestException(ManifestContentProvider.ManifestExceptionType.SIGNATURE_ERROR, e));
    } 
    return content;
  }
}
