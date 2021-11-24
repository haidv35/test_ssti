package com.vmware.ph.phservice.common.internal.security;

import java.net.URI;
import org.springframework.web.util.UriComponentsBuilder;

public class EnvoySidecarUtil {
  static final int ENVOY_SIDECAR_PORT = 1080;
  
  private static final String HTTP_SCHEME = "http";
  
  private static final String HTTPS_SCHEME = "https";
  
  private static final String LOCALHOST = "localhost";
  
  static final String HTTP_1_PROTOCOL = "http1";
  
  static final String VECS_CA = "external-vecs";
  
  public static URI toEnvoyLocalUri(URI uri) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
    switchToHostAndPortOfEnvoySidecar(builder);
    return builder.build().toUri();
  }
  
  public static URI toEnvoyRemoteUri(URI uri) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
    switchToHostAndPortOfEnvoySidecar(builder);
    String hostName = uri.getHost();
    int port = getPort(uri);
    String envoyRoutePrefix = buildEnvoyRoutePrefix(hostName, port);
    builder
      .replacePath(envoyRoutePrefix)
      .path(uri.getPath());
    return builder.build().toUri();
  }
  
  private static void switchToHostAndPortOfEnvoySidecar(UriComponentsBuilder uriComponentsBuilder) {
    uriComponentsBuilder
      .scheme("http")
      .host("localhost")
      .port(1080);
  }
  
  private static int getPort(URI uri) {
    String scheme = uri.getScheme();
    int port = uri.getPort();
    if (port == -1)
      if ("http".equals(scheme)) {
        port = 80;
      } else if ("https".equals(scheme)) {
        port = 443;
      }  
    return port;
  }
  
  private static String buildEnvoyRoutePrefix(String vcHostName, int vcPort) {
    return "/external-vecs/http1/" + vcHostName + "/" + vcPort;
  }
}
