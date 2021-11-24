package com.vmware.ph.phservice.proxy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PopDiscoveryUtil {
  private static final Logger _logger = LoggerFactory.getLogger(PopDiscoveryUtil.class);
  
  private static final String EXTRACT_PROXY_CMD = "grep nameserver /etc/resolv.conf | awk '$1 == \"nameserver\" {print $2}'";
  
  private static final String[] GET_SH_LOCATION_CMD = new String[] { "which", "sh" };
  
  private static final String PROXY_SERVER_PREFIX = "pop.";
  
  public static Optional<String> getPopFqdn() {
    List<String> shCommandLocation = getCmdOutput(GET_SH_LOCATION_CMD);
    if (shCommandLocation.isEmpty()) {
      _logger.warn("Cannot find 'sh' command location. PoP discovery interrupted.");
      return Optional.empty();
    } 
    String shCmd = shCommandLocation.get(0);
    List<String> hosts = getCmdOutput(new String[] { shCmd, "-c", "grep nameserver /etc/resolv.conf | awk '$1 == \"nameserver\" {print $2}'" });
    String[] getFqdnCmd = { "dig", "-x", "", "+short" };
    Optional<String> hostFqdn = hosts.stream().map(host -> {
          getFqdnCmd[2] = host;
          Optional<String> popFqdn = getCmdOutput(getFqdnCmd).stream().filter(()).map(()).findFirst();
          return popFqdn.isPresent() ? popFqdn.get() : null;
        }).filter(popFqdn -> (popFqdn != null)).findFirst();
    return hostFqdn;
  }
  
  private static List<String> getCmdOutput(String[] cmdWithArgs) {
    List<String> lines;
    try {
      Process process = Runtime.getRuntime().exec(cmdWithArgs);
      _logger.debug("Executing {}...", Arrays.toString((Object[])cmdWithArgs));
      process.waitFor();
      _logger.debug("Execution finished.");
      try (BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        lines = output.lines().collect((Collector)Collectors.toList());
      } 
    } catch (IOException|InterruptedException e) {
      String message = String.format("Error while executing command %s", new Object[] { Arrays.toString((Object[])cmdWithArgs) });
      _logger.error(message, e);
      lines = Collections.emptyList();
    } 
    _logger.debug("Return lines: {}", lines);
    return lines;
  }
}
