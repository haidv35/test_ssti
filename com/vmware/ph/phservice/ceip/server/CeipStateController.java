package com.vmware.ph.phservice.ceip.server;

import com.vmware.ph.phservice.ceip.ConsentException;
import com.vmware.ph.phservice.ceip.ConsentManager;
import com.vmware.ph.phservice.ceip.internal.CeipUtil;
import com.vmware.ph.phservice.ceip.server.dto.GetCeipStateResult;
import com.vmware.ph.phservice.ceip.server.dto.ResultWrapper;
import com.vmware.ph.phservice.common.internal.JsonUtil;
import com.vmware.vim.binding.phonehome.data.ConsentConfigurationData;
import java.util.concurrent.Callable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping({"state"})
public class CeipStateController {
  private final ConsentManager _consentManager;
  
  public CeipStateController(ConsentManager consentManager) {
    this._consentManager = consentManager;
  }
  
  @RequestMapping(method = {RequestMethod.GET})
  public Callable<ResponseEntity<String>> getState() {
    return new Callable<ResponseEntity<String>>() {
        public ResponseEntity<String> call() throws ConsentException {
          ConsentConfigurationData ccData = CeipStateController.this._consentManager.readConsent();
          boolean ceipEnabled = CeipUtil.isCeipConsentAccepted(ccData);
          ResultWrapper<GetCeipStateResult> ceipStateResult = new ResultWrapper<>(new GetCeipStateResult(ceipEnabled));
          String ceipStateResponseAsJson = JsonUtil.toJson(ceipStateResult);
          return new ResponseEntity(ceipStateResponseAsJson, HttpStatus.OK);
        }
      };
  }
  
  public Callable<ResponseEntity<Void>> setCeip(@RequestParam(value = "status", required = true) final boolean status) {
    return new Callable<ResponseEntity<Void>>() {
        public ResponseEntity<Void> call() throws Exception {
          ConsentConfigurationData ccData = CeipUtil.createConsentConfigurationDataForConsentState(status);
          CeipStateController.this._consentManager.writeConsent(ccData);
          return new ResponseEntity(HttpStatus.OK);
        }
      };
  }
}
