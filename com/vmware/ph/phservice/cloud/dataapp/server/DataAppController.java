package com.vmware.ph.phservice.cloud.dataapp.server;

import com.vmware.ph.phservice.cloud.dataapp.service.DataApp;
import com.vmware.ph.phservice.common.cdf.dataapp.PluginData;
import com.vmware.ph.phservice.common.internal.exceptions.StatusCodeException;
import com.vmware.ph.phservice.common.server.HttpUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DataAppController {
  private static final String DATAAPP_SEND_PATH = "/dataapp/send";
  
  private static final String QUERY_PATH = "/v1/results";
  
  private final DataApp _dataAppService;
  
  public DataAppController(DataApp dataAppService) {
    this._dataAppService = dataAppService;
  }
  
  @RequestMapping(method = {RequestMethod.POST}, value = {"/dataapp/send"})
  public Callable<ResponseEntity<String>> sendDataAppData(HttpServletRequest httpRequest, @RequestParam("_c") final String collectorId, @RequestParam("_i") final String collectorInstanceId, @RequestParam(value = "_n", required = false) final String collectionId, @RequestHeader(value = "X-Deployment-Secret", required = false) final String deploymentSecret, @RequestHeader(value = "X-Plugin-Type", required = false) final String pluginType, @RequestHeader(value = "X-Data-Type", required = false) final String dataType, @RequestHeader(value = "X-Object-Id", required = false) final String objectId) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    IOUtils.copy((InputStream)httpRequest.getInputStream(), output);
    final byte[] content = output.toByteArray();
    final boolean isCompressed = HttpUtil.isCompressed(httpRequest);
    return new Callable<ResponseEntity<String>>() {
        public ResponseEntity<String> call() {
          try {
            DataAppController.this._dataAppService.uploadData(collectorId, collectorInstanceId, collectionId, deploymentSecret, new PluginData(pluginType, content, isCompressed, dataType, objectId));
            return new ResponseEntity(HttpStatus.CREATED);
          } catch (StatusCodeException e) {
            return new ResponseEntity(e
                .getMessage(), 
                HttpStatus.valueOf(e.getStatusCode()));
          } 
        }
      };
  }
  
  @RequestMapping(method = {RequestMethod.GET}, value = {"/v1/results"})
  public Callable<ResponseEntity<String>> getResult(@RequestParam("collectorId") final String collectorId, @RequestParam("deploymentId") final String collectorInstanceId, @RequestParam(value = "type", required = false) final String dataType, @RequestParam(value = "objectId", required = false) final String objectId, @RequestParam(value = "since", required = false) final Long sinceTimestamp, @RequestHeader(value = "X-Plugin-Type", required = false) String pluginType, @RequestHeader(value = "X-Deployment-Secret", required = false) final String deploymentSecret) {
    return new Callable<ResponseEntity<String>>() {
        public ResponseEntity<String> call() {
          try {
            String resultJson = DataAppController.this._dataAppService.getResult(collectorId, collectorInstanceId, deploymentSecret, dataType, objectId, sinceTimestamp);
            return new ResponseEntity(resultJson, HttpStatus.OK);
          } catch (StatusCodeException e) {
            return new ResponseEntity(e
                .getMessage(), 
                HttpStatus.valueOf(e.getStatusCode()));
          } 
        }
      };
  }
}
