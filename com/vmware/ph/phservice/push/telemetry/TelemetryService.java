package com.vmware.ph.phservice.push.telemetry;

import java.util.concurrent.Future;

public interface TelemetryService {
  Future<Boolean> processTelemetry(String paramString1, String paramString2, TelemetryRequest[] paramArrayOfTelemetryRequest);
}
