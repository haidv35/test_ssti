package com.vmware.ph.phservice.push.telemetry.server;

import com.vmware.ph.phservice.common.internal.IdFormatUtil;
import com.vmware.ph.phservice.common.internal.LogUtil;
import com.vmware.ph.phservice.common.server.HttpUtil;
import com.vmware.ph.phservice.common.server.throttler.RateLimiter;
import com.vmware.ph.phservice.push.telemetry.TelemetryLevel;
import com.vmware.ph.phservice.push.telemetry.TelemetryLevelService;
import com.vmware.ph.phservice.push.telemetry.TelemetryRequest;
import com.vmware.ph.phservice.push.telemetry.TelemetryService;
import com.vmware.ph.phservice.push.telemetry.server.throttler.RateLimiterProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AsyncTelemetryController {
  private static final Log _log = LogFactory.getLog(AsyncTelemetryController.class);
  
  private static final String STAGE_PATH = "/ph-stg/api/hyper/send";
  
  private static final String PROD_PATH = "/ph/api/hyper/send";
  
  private static final String STAGE_LEVEL_PATH = "/ph-stg/api/level";
  
  private static final String PROD_LEVEL_PATH = "/ph/api/level";
  
  private final TelemetryService _prodTelemetryService;
  
  private final TelemetryService _stageTelemetryService;
  
  private final TelemetryLevelService _prodTelemetryLevelService;
  
  private final TelemetryLevelService _stageTelemetryLevelService;
  
  private final long _telemetryRequestPermitTimeoutMillis;
  
  private final RateLimiter _globalTelemetryRateLimiter;
  
  private RateLimiterProvider _prodRateLimiterProvider;
  
  private RateLimiterProvider _stageRateLimiterProvider;
  
  private List<String> _collectorIdWhitelist;
  
  public AsyncTelemetryController(TelemetryService prodTelemetryService, TelemetryService stageTelemetryService, TelemetryLevelService prodTelemetryLevelService, TelemetryLevelService stageTelemetryLevelService, RateLimiter globalTelemetryRateLimiter, long telemetryRequestPermitTimeoutMillis, List<String> collectorIdWhitelist) {
    this._prodTelemetryService = prodTelemetryService;
    this._stageTelemetryService = stageTelemetryService;
    this._prodTelemetryLevelService = prodTelemetryLevelService;
    this._stageTelemetryLevelService = stageTelemetryLevelService;
    this._globalTelemetryRateLimiter = globalTelemetryRateLimiter;
    this._telemetryRequestPermitTimeoutMillis = telemetryRequestPermitTimeoutMillis;
    this._collectorIdWhitelist = collectorIdWhitelist;
  }
  
  public void setProdRateLimiterProvider(RateLimiterProvider prodRateLimiterProvider) {
    this._prodRateLimiterProvider = prodRateLimiterProvider;
  }
  
  public void setStageRateLimiterProvider(RateLimiterProvider stageRateLimiterProvider) {
    this._stageRateLimiterProvider = stageRateLimiterProvider;
  }
  
  @RequestMapping(method = {RequestMethod.POST}, value = {"/ph/api/hyper/send"})
  public Callable<ResponseEntity<Void>> handleSendRequest(HttpServletRequest httpRequest, @RequestParam(value = "_v", required = false) String version, @RequestParam("_c") String collectorId, @RequestParam(value = "_i", required = false) String collectorInstanceId) throws IOException {
    return handleSendRequest(this._prodTelemetryService, this._prodRateLimiterProvider, httpRequest, version, collectorId, collectorInstanceId);
  }
  
  @RequestMapping(method = {RequestMethod.POST}, value = {"/ph-stg/api/hyper/send"})
  public Callable<ResponseEntity<Void>> handleStageSendRequest(HttpServletRequest httpRequest, @RequestParam(value = "_v", required = false) String version, @RequestParam("_c") String collectorId, @RequestParam(value = "_i", required = false) String collectorInstanceId) throws IOException {
    return handleSendRequest(this._stageTelemetryService, this._stageRateLimiterProvider, httpRequest, version, collectorId, collectorInstanceId);
  }
  
  @RequestMapping(method = {RequestMethod.GET}, value = {"/ph/api/level"})
  public Callable<ResponseEntity<String>> handleGetLevelRequest(@RequestParam("_c") String collectorId, @RequestParam(value = "_i", required = false) String collectorInstanceId) {
    return handleGetLevelRequest(this._prodTelemetryLevelService, this._prodRateLimiterProvider, collectorId, collectorInstanceId);
  }
  
  @RequestMapping(method = {RequestMethod.GET}, value = {"/ph-stg/api/level"})
  public Callable<ResponseEntity<String>> handleStageGetLevelRequest(@RequestParam("_c") String collectorId, @RequestParam(value = "_i", required = false) String collectorInstanceId) {
    return handleGetLevelRequest(this._stageTelemetryLevelService, this._stageRateLimiterProvider, collectorId, collectorInstanceId);
  }
  
  private Callable<ResponseEntity<Void>> handleSendRequest(final TelemetryService telemetryService, final RateLimiterProvider rateLimiterProvider, HttpServletRequest httpRequest, String version, final String collectorId, final String collectorInstanceId) throws IOException {
    final TelemetryRequest telemetryRequest = createTelemetryRequest(httpRequest, version, collectorId, collectorInstanceId);
    return new Callable<ResponseEntity<Void>>() {
        public ResponseEntity<Void> call() throws Exception {
          if (!AsyncTelemetryController.this.isRequestPermitted(collectorId, collectorInstanceId, rateLimiterProvider))
            return new ResponseEntity(HttpStatus.TOO_MANY_REQUESTS); 
          if (!IdFormatUtil.isValidCollectorInstanceId(collectorInstanceId) || 
            !AsyncTelemetryController.this._collectorIdWhitelist.contains(collectorId)) {
            AsyncTelemetryController._log.debug(String.format("Incorrect collectorId '%s' or collectorInstanceId '%s'. Returning 400.", new Object[] { LogUtil.sanitiseForLog(this.val$collectorId), 
                    LogUtil.sanitiseForLog(this.val$collectorInstanceId) }));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
          } 
          telemetryService.processTelemetry(telemetryRequest
              .getCollectorId(), telemetryRequest
              .getCollectorIntanceId(), new TelemetryRequest[] { this.val$telemetryRequest });
          return new ResponseEntity(HttpStatus.CREATED);
        }
      };
  }
  
  private Callable<ResponseEntity<String>> handleGetLevelRequest(final TelemetryLevelService telemetryLevelService, final RateLimiterProvider rateLimiterProvider, final String collectorId, final String collectorInstanceId) {
    return new Callable<ResponseEntity<String>>() {
        public ResponseEntity<String> call() throws Exception {
          if (!AsyncTelemetryController.this.isRequestPermitted(collectorId, collectorInstanceId, rateLimiterProvider))
            return new ResponseEntity(HttpStatus.TOO_MANY_REQUESTS); 
          if (!AsyncTelemetryController.this._collectorIdWhitelist.contains(collectorId)) {
            AsyncTelemetryController._log.debug(String.format("Incorrect collectorId '%s' or collectorInstanceId '%s'. Returning 400.", new Object[] { LogUtil.sanitiseForLog(this.val$collectorId), 
                    LogUtil.sanitiseForLog(this.val$collectorInstanceId) }));
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
          } 
          TelemetryLevel level = telemetryLevelService.getTelemetryLevel(collectorId, collectorInstanceId);
          ResponseEntity<String> telemetryLevelResponseEntity = new ResponseEntity(AsyncTelemetryController.wrapStringAsJsonString(level.toString()), HttpStatus.OK);
          return telemetryLevelResponseEntity;
        }
      };
  }
  
  private boolean isRequestPermitted(String collectorId, String collectorInstanceId, RateLimiterProvider collectorRateLimiterProvider) {
    boolean isRequestPermitted = this._globalTelemetryRateLimiter.tryAcquire(this._telemetryRequestPermitTimeoutMillis);
    if (isRequestPermitted && collectorRateLimiterProvider != null) {
      RateLimiter collectorRateLimiter = collectorRateLimiterProvider.getRateLimiter(collectorId, collectorInstanceId);
      if (collectorRateLimiter != null)
        isRequestPermitted = collectorRateLimiter.tryAcquire(this._telemetryRequestPermitTimeoutMillis); 
    } 
    if (!isRequestPermitted)
      _log.warn("Request will not be processed due to high load."); 
    return isRequestPermitted;
  }
  
  private static TelemetryRequest createTelemetryRequest(HttpServletRequest httpRequest, String version, String collectorId, String collectorInstanceId) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IOUtils.copy((InputStream)httpRequest.getInputStream(), out);
    boolean isCompressed = HttpUtil.isCompressed(httpRequest);
    TelemetryRequest telemetryRequest = new TelemetryRequest(version, collectorId, collectorInstanceId, out.toByteArray(), isCompressed);
    return telemetryRequest;
  }
  
  private static final String wrapStringAsJsonString(String responseBody) {
    return String.format("\"%s\"", new Object[] { responseBody });
  }
}
