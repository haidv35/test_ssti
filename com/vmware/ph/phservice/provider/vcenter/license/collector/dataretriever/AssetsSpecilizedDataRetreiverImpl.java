package com.vmware.ph.phservice.provider.vcenter.license.collector.dataretriever;

import com.vmware.ph.phservice.common.PageUtil;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vim.vc.util.HostReader;
import com.vmware.ph.phservice.provider.common.DataRetriever;
import com.vmware.ph.phservice.provider.vcenter.license.client.LicenseClient;
import com.vmware.vim.binding.cis.license.Asset;
import com.vmware.vim.binding.cis.license.AssetIdentifier;
import com.vmware.vim.binding.cis.license.AssetInfo;
import com.vmware.vim.binding.cis.license.License;
import com.vmware.vim.binding.cis.license.LicenseInfo;
import com.vmware.vim.binding.cis.license.SerialKeyLicenseInfo;
import com.vmware.vim.binding.cis.license.fault.NotFoundFault;
import com.vmware.vim.binding.cis.license.management.AssetSearchSpec;
import com.vmware.vim.binding.cis.license.management.SystemManagementService;
import com.vmware.vim.binding.impl.cis.license.AssetIdentifierImpl;
import com.vmware.vim.binding.impl.cis.license.management.AssetSearchSpecByIdentifiersImpl;
import com.vmware.vim.binding.impl.cis.license.management.AssetSearchSpecByProductFamilyNamesImpl;
import com.vmware.vim.binding.impl.vmodl.KeyAnyValueImpl;
import com.vmware.vim.binding.vmodl.KeyAnyValue;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.client.exception.ConnectionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AssetsSpecilizedDataRetreiverImpl implements DataRetriever<Asset> {
  private static final String ESX_SERVER_PRODUCT_FAMILY_NAME = "VMware ESX Server";
  
  private static final String VC_SERVER_PRODUCT_FAMILY_NAME = "VMware VirtualCenter Server";
  
  private static final int ESX_PAGE_SIZE = 200;
  
  private static final int ESX_OPTIMIZATION_THRESHOLD = 600;
  
  private static final String EVALUATION_LICENSE_KEY = "00000-00000-00000-00000-00000";
  
  private static final String NO_LICENSE_KEY = "";
  
  private static final String VC_ID_PROPERTY = "vcId";
  
  private static final String LICENSE_SERIAL_KEY_PROPERTY = "licenseKey";
  
  private static final Log _log = LogFactory.getLog(AssetsSpecilizedDataRetreiverImpl.class);
  
  private final LicenseClient _licenseClient;
  
  private final VcClient _vcClient;
  
  private final HostReader _hostReader;
  
  public AssetsSpecilizedDataRetreiverImpl(LicenseClient licenseClient, VcClient vcClient) {
    this._licenseClient = licenseClient;
    this._vcClient = vcClient;
    this._hostReader = new HostReader(this._vcClient);
  }
  
  public List<Asset> retrieveData() {
    Map<String, String> vcScopeToVcInstanceIdMap = new HashMap<>();
    List<Asset> retrievedAssets = new ArrayList<>();
    List<Asset> vcAssets = searchAssetsByProductFamilyName(new String[] { "VMware VirtualCenter Server" }, false, null);
    for (Asset vcAsset : vcAssets) {
      AssetInfo vcAssetInfo = vcAsset.getInfo();
      vcScopeToVcInstanceIdMap.put(vcAssetInfo.getScopeId(), vcAssetInfo.getInstanceId());
    } 
    retrievedAssets.addAll(augmentAssets(vcAssets, vcScopeToVcInstanceIdMap));
    List<Asset> nonVcAndEsxAssets = searchAssetsByProductFamilyName(new String[] { "VMware ESX Server", "VMware VirtualCenter Server" }, true, null);
    retrievedAssets.addAll(augmentAssets(nonVcAndEsxAssets, vcScopeToVcInstanceIdMap));
    String givenVcInstanceId = this._vcClient.getServiceInstanceContent().getAbout().getInstanceUuid();
    for (Asset vcAsset : vcAssets) {
      AssetInfo vcAssetInfo = vcAsset.getInfo();
      if (!givenVcInstanceId.equals(vcAssetInfo.getInstanceId()))
        continue; 
      List<Asset> esxAssets = searchEsxAssetsInVc(vcAssetInfo
          .getInstanceId(), vcAssetInfo
          .getScopeId());
      retrievedAssets.addAll(augmentAssets(esxAssets, vcScopeToVcInstanceIdMap));
    } 
    return retrievedAssets;
  }
  
  public String getKey(Asset asset) {
    return asset.getId().toString();
  }
  
  private List<Asset> searchAssetsByProductFamilyName(String[] productFamilyNames, boolean isInverseSearch, String scopeId) {
    AssetSearchSpecByProductFamilyNamesImpl assetSearchSpecByProductFamilyNamesImpl = new AssetSearchSpecByProductFamilyNamesImpl();
    assetSearchSpecByProductFamilyNamesImpl.setProductFamilyNames(productFamilyNames);
    assetSearchSpecByProductFamilyNamesImpl.setInverseSearch(Boolean.valueOf(isInverseSearch));
    if (scopeId != null)
      assetSearchSpecByProductFamilyNamesImpl.setScopeId(scopeId); 
    List<Asset> assetsAsList = searchAssets((AssetSearchSpec)assetSearchSpecByProductFamilyNamesImpl);
    return assetsAsList;
  }
  
  private List<Asset> searchAssets(AssetSearchSpec assetSearchSpec) {
    SystemManagementService systemManagementService = this._licenseClient.getSystemManagementService();
    Asset[] assets = null;
    try {
      assets = systemManagementService.searchAssets(assetSearchSpec);
    } catch (ConnectionException ce) {
      if (_log.isDebugEnabled())
        _log.debug("Connection problem was caught while trying to search for assets. This means that no telemetry data for assets will be collected.", (Throwable)ce); 
    } catch (NotFoundFault e) {
      if (_log.isDebugEnabled())
        _log.debug("Asset not found.", (Throwable)e); 
    } 
    List<Asset> assetsAsList = null;
    if (assets != null) {
      assetsAsList = Arrays.asList(assets);
    } else {
      assetsAsList = Collections.emptyList();
    } 
    return assetsAsList;
  }
  
  private List<Asset> searchEsxAssetsInVc(String vcInstanceId, String vcNodeId) {
    List<Asset> assets = null;
    String givenVcInstanceId = this._vcClient.getServiceInstanceContent().getAbout().getInstanceUuid();
    if (vcInstanceId.equals(givenVcInstanceId)) {
      List<ManagedObjectReference> hostMoRefs = this._hostReader.getHostMoRefs();
      if (hostMoRefs.size() > 600)
        assets = searchEsxAssetsByHostMoRefsWithPaging(hostMoRefs, vcNodeId); 
    } 
    if (assets == null)
      assets = searchAssetsByProductFamilyName(new String[] { "VMware ESX Server" }, false, vcNodeId); 
    return assets;
  }
  
  private List<Asset> searchEsxAssetsByHostMoRefsWithPaging(List<ManagedObjectReference> hostMoRefs, String vcNodeId) {
    List<Asset> hostAssets = new ArrayList<>();
    List<Integer> pageOffsets = PageUtil.getPagesOffsets(hostMoRefs.size(), 200);
    for (Iterator<Integer> iterator = pageOffsets.iterator(); iterator.hasNext(); ) {
      int pageOffset = ((Integer)iterator.next()).intValue();
      List<ManagedObjectReference> pagedHostMoRefs = PageUtil.pageItems(hostMoRefs, pageOffset, 200);
      List<Asset> pagedHostAssets = searchEsxAssetsByHostMoRefs(pagedHostMoRefs, vcNodeId);
      hostAssets.addAll(pagedHostAssets);
    } 
    return hostAssets;
  }
  
  private List<Asset> searchEsxAssetsByHostMoRefs(List<ManagedObjectReference> hostMoRefs, String vcNodeId) {
    List<AssetIdentifier> hostAssetIdentifiers = createAssetIdentifiersFromHostMoRefs(hostMoRefs, vcNodeId);
    AssetSearchSpecByIdentifiersImpl assetSearchSpecByIdentifiersImpl = new AssetSearchSpecByIdentifiersImpl();
    assetSearchSpecByIdentifiersImpl.setScopeId(vcNodeId);
    assetSearchSpecByIdentifiersImpl.setAssetIdentifiers(hostAssetIdentifiers
        .<AssetIdentifier>toArray(
          new AssetIdentifier[hostAssetIdentifiers.size()]));
    List<Asset> hostAssets = searchAssets((AssetSearchSpec)assetSearchSpecByIdentifiersImpl);
    return hostAssets;
  }
  
  private static List<AssetIdentifier> createAssetIdentifiersFromHostMoRefs(List<ManagedObjectReference> hostMoRefs, String vcNodeId) {
    List<AssetIdentifier> assetIdentifiers = new ArrayList<>();
    for (ManagedObjectReference hostMoRef : hostMoRefs) {
      AssetIdentifierImpl assetIdentifierImpl = new AssetIdentifierImpl();
      assetIdentifierImpl.setInstanceId(hostMoRef.getValue());
      assetIdentifierImpl.setScopeId(vcNodeId);
      assetIdentifiers.add(assetIdentifierImpl);
    } 
    return assetIdentifiers;
  }
  
  private static List<Asset> augmentAssets(List<Asset> assets, Map<String, String> vcScopeToVcInstanceIdMap) {
    List<Asset> augmentedAssets = augmentAssetsWithVcId(assets, vcScopeToVcInstanceIdMap);
    augmentedAssets = augmentAssetsWithLicenseSerialKey(augmentedAssets);
    return augmentedAssets;
  }
  
  private static List<Asset> augmentAssetsWithVcId(List<Asset> assets, Map<String, String> vcScopeToVcInstanceIdMap) {
    for (Asset asset : assets) {
      String assetScopeId = asset.getInfo().getScopeId();
      String discoveredVcInstnaceId = null;
      if (assetScopeId != null)
        discoveredVcInstnaceId = vcScopeToVcInstanceIdMap.get(assetScopeId); 
      setAssetProperty(asset, "vcId", discoveredVcInstnaceId);
    } 
    return assets;
  }
  
  private static List<Asset> augmentAssetsWithLicenseSerialKey(List<Asset> assets) {
    String licenseKey = "";
    for (Asset asset : assets) {
      AssetInfo assetInfo = asset.getInfo();
      if (assetInfo.isInEvaluation()) {
        licenseKey = "00000-00000-00000-00000-00000";
      } else {
        License license = assetInfo.getLicense();
        if (license != null) {
          LicenseInfo licenseInfo = license.getInfo();
          licenseKey = extractSerialKey(licenseInfo);
        } 
      } 
      setAssetProperty(asset, "licenseKey", licenseKey);
    } 
    return assets;
  }
  
  private static String extractSerialKey(LicenseInfo licenseInfo) {
    if (licenseInfo instanceof SerialKeyLicenseInfo) {
      SerialKeyLicenseInfo serialKeyLicenseInfo = (SerialKeyLicenseInfo)licenseInfo;
      String[] serialKeys = serialKeyLicenseInfo.getSerialKeys();
      assert serialKeys != null && serialKeys.length > 0;
      return serialKeys[0];
    } 
    return "";
  }
  
  private static void setAssetProperty(Asset asset, String key, Object value) {
    AssetInfo assetInfo = asset.getInfo();
    KeyAnyValueImpl keyAnyValueImpl = new KeyAnyValueImpl();
    keyAnyValueImpl.setKey(key);
    if (value == null) {
      keyAnyValueImpl.setValue(null);
    } else {
      keyAnyValueImpl.setValue(value);
    } 
    KeyAnyValue[] assetProperties = assetInfo.getProperties();
    KeyAnyValue[] modifiedAssetProperties = null;
    int numProperties = 0;
    if (assetProperties == null) {
      modifiedAssetProperties = new KeyAnyValue[1];
    } else {
      numProperties = assetProperties.length;
      modifiedAssetProperties = new KeyAnyValue[numProperties + 1];
      System.arraycopy(assetProperties, 0, modifiedAssetProperties, 0, numProperties);
    } 
    modifiedAssetProperties[numProperties] = (KeyAnyValue)keyAnyValueImpl;
    assetInfo.setProperties(modifiedAssetProperties);
  }
}
