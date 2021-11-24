package com.vmware.ph.phservice.push.telemetry.internal.log;

import java.net.URI;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.AppenderRefComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class LogTelemetryConfigurationFactory extends ConfigurationFactory {
  static final String CURRENT_LOG_FILE_EXTENSION = ".json";
  
  static final String COMPRESSED_LOG_FILE_EXTENSION = ".json.gz";
  
  static final String COMPRESSED_LOG_FILE_PATTERN = ".%i.json.gz";
  
  private static final Level CONFIGURATION_LOG_LEVEL = Level.WARN;
  
  private static final Level TELEMETRY_LOG_LEVEL = Level.INFO;
  
  private static final String ROUTING_APPENDER_NAME = "LogTelemetryServiceRouting";
  
  private final RollingAppenderComponentBuilder _rollingFileAppenderBuilder;
  
  public LogTelemetryConfigurationFactory(String logTelemetryRollingInterval, String logTelemetryRollingMaxFileSize, String logTelemetryRollingMaxFilesCount, int bufferSizeInBytes) {
    this._rollingFileAppenderBuilder = new RollingAppenderComponentBuilder(logTelemetryRollingInterval, logTelemetryRollingMaxFileSize, logTelemetryRollingMaxFilesCount, bufferSizeInBytes);
  }
  
  protected String[] getSupportedTypes() {
    return new String[] { "*" };
  }
  
  public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
    return getConfiguration(loggerContext, (source != null) ? source
        
        .toString() : null, null);
  }
  
  public Configuration getConfiguration(LoggerContext loggerContext, String name, URI configLocation) {
    ConfigurationBuilder<BuiltConfiguration> configurationBuilder = newConfigurationBuilder();
    configurationBuilder
      .setStatusLevel(CONFIGURATION_LOG_LEVEL)
      .setPackages(LogTelemetryConfigurationFactory.class.getPackage().getName());
    AppenderComponentBuilder rollingFileAppender = this._rollingFileAppenderBuilder.createRollingFileAppender(configurationBuilder);
    AppenderComponentBuilder routingAppender = createRoutingAppender(configurationBuilder, rollingFileAppender, "$${ctx:logTelemetryDirPath}_${ctx:logTelemetryFileName}");
    configurationBuilder.add(routingAppender);
    RootLoggerComponentBuilder rootLogger = createRootLogger(configurationBuilder, routingAppender);
    configurationBuilder.add(rootLogger);
    return (Configuration)configurationBuilder.build();
  }
  
  private static RootLoggerComponentBuilder createRootLogger(ConfigurationBuilder<BuiltConfiguration> configurationBuilder, AppenderComponentBuilder routingAppender) {
    AppenderRefComponentBuilder loggerAppenderRef = configurationBuilder.newAppenderRef(routingAppender.getName());
    RootLoggerComponentBuilder rootLogger = (RootLoggerComponentBuilder)((RootLoggerComponentBuilder)configurationBuilder.newRootLogger(TELEMETRY_LOG_LEVEL).addAttribute("additivity", false)).add(loggerAppenderRef);
    return rootLogger;
  }
  
  private static AppenderComponentBuilder createRoutingAppender(ConfigurationBuilder<BuiltConfiguration> configurationBuilder, AppenderComponentBuilder wrappedAppender, String routePattern) {
    AppenderComponentBuilder routingAppender = configurationBuilder.newAppender("LogTelemetryServiceRouting", "Routing");
    ComponentBuilder appenderRoutes = configurationBuilder.newComponent("Routes").addAttribute("pattern", routePattern);
    ComponentBuilder appenderRoute = configurationBuilder.newComponent("Route").addComponent((ComponentBuilder)wrappedAppender);
    appenderRoutes.addComponent(appenderRoute);
    routingAppender.addComponent(appenderRoutes);
    return routingAppender;
  }
  
  private static class RollingAppenderComponentBuilder {
    private static final String ROLLING_APPENDER_NAME = "${ctx:logTelemetryDirPath}_${ctx:logTelemetryFileName}";
    
    private static final String ROLLING_APPENDER_FILENAME = "${ctx:logTelemetryDirPath}/${ctx:logTelemetryFileName}.json";
    
    private static final String ROLLING_APPENDER_FILE_PATTERN = "${ctx:logTelemetryDirPath}/${ctx:logTelemetryFileName}.%i.json.gz";
    
    private static final String ROLLING_APPENDER_PATTERN = "%m%n";
    
    private final String _logTelemetryRollingInterval;
    
    private final String _logTelemetryRollingMaxFileSize;
    
    private final String _logTelemetryRollingMaxFilesCount;
    
    private final int _bufferSizeInBytes;
    
    private RollingAppenderComponentBuilder(String logTelemetryRollingInterval, String logTelemetryRollingMaxFileSize, String logTelemetryRollingMaxFilesCount, int bufferSizeInBytes) {
      this._logTelemetryRollingInterval = logTelemetryRollingInterval;
      this._logTelemetryRollingMaxFileSize = logTelemetryRollingMaxFileSize;
      this._logTelemetryRollingMaxFilesCount = logTelemetryRollingMaxFilesCount;
      this._bufferSizeInBytes = bufferSizeInBytes;
    }
    
    private AppenderComponentBuilder createRollingFileAppender(ConfigurationBuilder<BuiltConfiguration> configurationBuilder) {
      LayoutComponentBuilder layoutBuilder = (LayoutComponentBuilder)configurationBuilder.newLayout("PatternLayout").addAttribute("pattern", "%m%n");
      AppenderComponentBuilder rollingFileAppender = ((AppenderComponentBuilder)((AppenderComponentBuilder)((AppenderComponentBuilder)((AppenderComponentBuilder)((AppenderComponentBuilder)configurationBuilder.newAppender("rolling", "RollingFile").addAttribute("name", "${ctx:logTelemetryDirPath}_${ctx:logTelemetryFileName}")).addAttribute("createOnDemand", true)).addAttribute("fileName", "${ctx:logTelemetryDirPath}/${ctx:logTelemetryFileName}.json")).addAttribute("filePattern", "${ctx:logTelemetryDirPath}/${ctx:logTelemetryFileName}.%i.json.gz")).addAttribute("bufferSize", this._bufferSizeInBytes)).add(layoutBuilder);
      addPoliciesToRollingFileAppender(rollingFileAppender, configurationBuilder);
      addStrategyToRollingFileAppender(rollingFileAppender, configurationBuilder);
      return rollingFileAppender;
    }
    
    private void addPoliciesToRollingFileAppender(AppenderComponentBuilder rollingFileAppender, ConfigurationBuilder<BuiltConfiguration> configurationBuilder) {
      ComponentBuilder compositePolicy = configurationBuilder.newComponent("Policies");
      ComponentBuilder timeBasedTriggeringPolicy = configurationBuilder.newComponent("TimeSinceLastRolloverTriggeringPolicy").addAttribute("interval", this._logTelemetryRollingInterval);
      ComponentBuilder sizeBasedTriggeringPolicy = configurationBuilder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", this._logTelemetryRollingMaxFileSize);
      compositePolicy.addComponent(timeBasedTriggeringPolicy);
      compositePolicy.addComponent(sizeBasedTriggeringPolicy);
      rollingFileAppender.addComponent(compositePolicy);
    }
    
    private void addStrategyToRollingFileAppender(AppenderComponentBuilder rollingFileAppender, ConfigurationBuilder<BuiltConfiguration> configurationBuilder) {
      ComponentBuilder rolloverStrategy = configurationBuilder.newComponent("CircularRolloverStrategy").addAttribute("max", this._logTelemetryRollingMaxFilesCount);
      rollingFileAppender.addComponent(rolloverStrategy);
    }
  }
}
