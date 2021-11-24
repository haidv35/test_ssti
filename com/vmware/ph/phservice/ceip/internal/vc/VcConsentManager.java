package com.vmware.ph.phservice.ceip.internal.vc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vmware.ph.phservice.ceip.ConsentException;
import com.vmware.ph.phservice.ceip.ConsentManager;
import com.vmware.ph.phservice.common.vim.VimContext;
import com.vmware.ph.phservice.common.vim.VimContextProvider;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.vim.binding.phonehome.data.ConsentConfigurationData;
import com.vmware.vim.binding.vim.fault.InvalidName;
import com.vmware.vim.binding.vim.option.OptionManager;
import com.vmware.vim.binding.vim.option.OptionValue;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.RuntimeFault;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VcConsentManager implements ConsentManager {
  private static final String CONSENT_VC_SETTING_KEY = "VirtualCenter.DataCollector.ConsentData";
  
  private static final Logger _log = LoggerFactory.getLogger(VcConsentManager.class);
  
  private final VimContextProvider _vimContextProvider;
  
  private VimContext _vimContext;
  
  public VcConsentManager(VimContextProvider vimContextProvider) {
    this._vimContextProvider = Objects.<VimContextProvider>requireNonNull(vimContextProvider, "The VimContextProvider must be specified.");
  }
  
  public VcConsentManager(VimContext vimContext) {
    this
      ._vimContext = Objects.<VimContext>requireNonNull(vimContext, "The VimContext must be specified.");
    this._vimContextProvider = null;
  }
  
  public ConsentConfigurationData readConsent() throws ConsentException {
    Object value;
    initVimContext();
    validateVimContext();
    try (VcClient vcClient = this._vimContext.getVcClientBuilder(true).build()) {
      value = readOptionValue(vcClient, "VirtualCenter.DataCollector.ConsentData");
    } 
    if (value == null) {
      _log.debug("ConsentData read from OptionManager is null.");
      return null;
    } 
    String stringValue = (String)value;
    _log.debug("ConsentData read from OptionManager is: {}", stringValue);
    try {
      ObjectMapper mapper = new ObjectMapper();
      ConsentConfigurationData result = (ConsentConfigurationData)mapper.readValue(stringValue, ConsentConfigurationData.class);
      _log.debug("readConsent completed successfully: {}", result);
      return result;
    } catch (IOException e) {
      _log.warn("Cannot deserialize " + ConsentConfigurationData.class
          .getName() + " from the following value: " + stringValue + ". Will default to null.");
      return null;
    } 
  }
  
  public void writeConsent(ConsentConfigurationData consent) throws ConsentException {
    initVimContext();
    validateVimContext();
    _log.debug("writeConsent: start, consent: {}", consent);
    String stringValue = null;
    if (consent != null)
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.writeValue(outputStream, consent);
        stringValue = outputStream.toString(StandardCharsets.UTF_8.name());
      } catch (IOException e) {
        throw new Error("Failed to serialize " + ConsentConfigurationData.class
            .getName() + " to Json.", e);
      }  
    try (VcClient vcClient = this._vimContext.getVcClientBuilder(true).build()) {
      writeOptionValue(vcClient, "VirtualCenter.DataCollector.ConsentData", stringValue);
      _log.debug("writeConsent: completed successfully");
    } 
  }
  
  public boolean isActive() {
    initVimContext();
    boolean isActive = (this._vimContext != null);
    _log.debug("Active state for {} is {}.", VcConsentManager.class.getSimpleName(), Boolean.valueOf(isActive));
    return isActive;
  }
  
  OptionManager initializeOptionManager(VcClient vcClient) {
    ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
    ClassLoader thisClassLoader = VcConsentManager.class.getClassLoader();
    Thread.currentThread().setContextClassLoader(thisClassLoader);
    try {
      ManagedObjectReference vcSettingsId = vcClient.getServiceInstanceContent().getSetting();
      return vcClient.<OptionManager>createMo(vcSettingsId);
    } finally {
      Thread.currentThread().setContextClassLoader(originalLoader);
    } 
  }
  
  private Object readOptionValue(VcClient vcClient, String key) throws ConsentException {
    OptionValue[] result;
    _log.debug("Retrieving value for option with key: {}", key);
    OptionManager optionManager = initializeOptionManager(vcClient);
    if (optionManager == null)
      throw new ConsentException("Cannot initialize OptionManager"); 
    try {
      result = optionManager.queryView(key);
    } catch (InvalidName e) {
      throw new ConsentException("Cannot find option with name " + key + " .", e);
    } catch (RuntimeFault|com.vmware.vim.vmomi.client.exception.RemoteException e) {
      throw new ConsentException("Failed to read option with name " + key + " .", e);
    } 
    _log.debug("Got {} result(s).", Integer.valueOf(result.length));
    Object firstResult = (result.length > 0) ? result[0].getValue() : null;
    _log.debug("Retrieved {} = {}", key, firstResult);
    return firstResult;
  }
  
  private void writeOptionValue(VcClient vcClient, String key, Object value) throws ConsentException {
    OptionManager optionManager = initializeOptionManager(vcClient);
    if (optionManager == null)
      throw new ConsentException("Unable to initialze OptionManager"); 
    _log.debug("Updating option {} with value {}", key, value);
    try {
      OptionValue optionValue = new OptionValue(key, value);
      optionManager.updateValues(new OptionValue[] { optionValue });
    } catch (InvalidName e) {
      throw new ConsentException(
          String.format("vCenter service does not support configuration key `%s' (incompatible vCenter version)", new Object[] { key }), e);
    } catch (RuntimeFault|com.vmware.vim.vmomi.client.exception.RemoteException e) {
      throw new ConsentException("Failed to write option " + key + " with value " + value + ".", e);
    } 
    _log.debug("Updating option {} with value {} completed successfully.", key, value);
  }
  
  private void initVimContext() {
    if (this._vimContext == null)
      this._vimContext = this._vimContextProvider.getVimContext(); 
  }
  
  private void validateVimContext() throws ConsentException {
    if (this._vimContext == null)
      throw new ConsentException(
          String.format("The %s is not active. The consent cannot be read.", new Object[] { VcConsentManager.class.getSimpleName() })); 
  }
}
