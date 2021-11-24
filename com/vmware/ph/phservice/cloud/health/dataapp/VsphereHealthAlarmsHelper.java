package com.vmware.ph.phservice.cloud.health.dataapp;

import com.vmware.ph.phservice.cloud.health.HealthSystem;
import com.vmware.ph.phservice.common.internal.i18n.LocalizedMessageProvider;
import com.vmware.ph.phservice.common.vim.internal.vc.pc.VcPropertyCollectorReader;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorReader;
import com.vmware.ph.phservice.common.vmomi.pc.PropertyCollectorUtil;
import com.vmware.vim.binding.impl.vmodl.KeyAnyValueImpl;
import com.vmware.vim.binding.impl.vmodl.TypeNameImpl;
import com.vmware.vim.binding.vim.ManagedEntity;
import com.vmware.vim.binding.vim.alarm.Alarm;
import com.vmware.vim.binding.vim.alarm.AlarmExpression;
import com.vmware.vim.binding.vim.alarm.AlarmManager;
import com.vmware.vim.binding.vim.alarm.AlarmSetting;
import com.vmware.vim.binding.vim.alarm.AlarmSpec;
import com.vmware.vim.binding.vim.alarm.AlarmState;
import com.vmware.vim.binding.vim.alarm.EventAlarmExpression;
import com.vmware.vim.binding.vim.alarm.OrAlarmExpression;
import com.vmware.vim.binding.vim.event.Event;
import com.vmware.vim.binding.vim.event.EventEx;
import com.vmware.vim.binding.vim.event.EventManager;
import com.vmware.vim.binding.vim.fault.InvalidEvent;
import com.vmware.vim.binding.vmodl.KeyAnyValue;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.TypeName;
import com.vmware.vim.binding.vmodl.fault.ManagedObjectNotFound;
import com.vmware.vim.binding.vmodl.query.InvalidProperty;
import com.vmware.vim.binding.vmodl.query.PropertyCollector;
import com.vmware.vim.vmomi.core.types.VmodlTypeMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VsphereHealthAlarmsHelper {
  private static final Log _log = LogFactory.getLog(VsphereHealthAlarmsHelper.class);
  
  private static final String ALARM_NAME = "com.vmware.adc.general.alarm.name";
  
  private static final String ALARM_DESCRIPTION = "com.vmware.adc.general.alarm.description";
  
  private static final String ALARM_NAME_FILTER_PROPERTY = "info.name";
  
  private static final String ALARM_EXPRESSION_COMPARISON_EQUALS_OPERATOR = "equals";
  
  private static final String VSPHERE_HEALTH_USER_NAME = "vSphere Health";
  
  private static final String TRIGGER_EVENT_TYPE = "vim.event.EventEx";
  
  private static final String TRIGGER_EVENT_CURRENT_STATUS_ATTRIBUTE = "curstatus";
  
  private static final String TRIGGER_EVENT_TYPE_ID = "vsphere.online.health.alarm.event";
  
  private static final String TRIGGER_EVENT_MESSAGE = "com.vmware.adc.trigger.event.message";
  
  private final VcClient _vcClient;
  
  private final LocalizedMessageProvider _localizedMessageProvider;
  
  private final VcPropertyCollectorReader _pcReader;
  
  private final VmodlTypeMap _vmodlTypeMap;
  
  private final AlarmManager _alarmManager;
  
  private final EventManager _eventManager;
  
  private final ManagedObjectReference _alarmEntity;
  
  public VsphereHealthAlarmsHelper(VcClient vcClient, AlarmManager alarmManager, EventManager eventManager, ManagedObjectReference alarmEntity, VcPropertyCollectorReader pcReader, LocalizedMessageProvider localizedMessageProvider) {
    this._vcClient = vcClient;
    this._alarmManager = alarmManager;
    this._eventManager = eventManager;
    this._alarmEntity = alarmEntity;
    this._pcReader = pcReader;
    this._localizedMessageProvider = localizedMessageProvider;
    this._vmodlTypeMap = this._vcClient.getVmodlContext().getVmodlTypeMap();
  }
  
  public ManagedObjectReference registerHealthAlarm(Locale locale) {
    ManagedObjectReference registeredAlarmMoref = null;
    AlarmSpec alarmSpec = buildAlarmSpec(locale);
    try {
      registeredAlarmMoref = this._alarmManager.create(this._alarmEntity, alarmSpec);
    } catch (Exception e) {
      _log.warn("Failed to register vSphere Online Health alarm", e);
    } 
    return registeredAlarmMoref;
  }
  
  public boolean isHealthAlarmTriggered(AlarmState registeredAlarmState) {
    ManagedEntity.Status alarmStatus = registeredAlarmState.getOverallStatus();
    return (alarmStatus == ManagedEntity.Status.yellow || alarmStatus == ManagedEntity.Status.red);
  }
  
  public boolean isHealthAlarmEnabled(ManagedObjectReference registeredAlarmMoref) {
    Alarm registeredAlarm = (Alarm)this._vcClient.createMo(registeredAlarmMoref);
    return registeredAlarm.getInfo().isEnabled();
  }
  
  public AlarmState getAlarmStateFromAlarmMoref(ManagedObjectReference registeredAlarm) throws AlarmStateNotFoundException {
    if (registeredAlarm == null)
      return null; 
    AlarmState[] alarmStates = this._alarmManager.getAlarmState(this._alarmEntity);
    for (int i = 0; i < alarmStates.length; i++) {
      AlarmState alarmState = alarmStates[i];
      if (registeredAlarm.equals(alarmState.alarm))
        return alarmState; 
    } 
    throw new AlarmStateNotFoundException("AlarmState was not found for the registered alarm: " + registeredAlarm);
  }
  
  public ManagedObjectReference getRegisteredHealthAlarmMoref(Locale locale) {
    String localized_alarm_name = this._localizedMessageProvider.getMessage("com.vmware.adc.general.alarm.name", locale);
    ManagedObjectReference[] alarms = this._alarmManager.getAlarm(this._alarmEntity);
    List<String> filterProperties = new ArrayList<>();
    filterProperties.add("info.name");
    PropertyCollector.FilterSpec pcFilterSpec = PropertyCollectorUtil.createMoRefsFilterSpec(alarms, filterProperties, this._vmodlTypeMap);
    List<PropertyCollectorReader.PcResourceItem> pcResourceItems = getRegisteredAlarmPcResourceItems(pcFilterSpec, filterProperties);
    for (PropertyCollectorReader.PcResourceItem pcResourceItem : pcResourceItems) {
      List<Object> propertyValues = pcResourceItem.getPropertyValues();
      if (propertyValues.get(1).equals(localized_alarm_name))
        return (ManagedObjectReference)propertyValues.get(0); 
    } 
    return null;
  }
  
  public void triggerHealthAlarm(HealthSystem.HealthStatus currentHealthStatus, Locale locale) {
    EventEx alarmEvent = buildTriggerEvent(currentHealthStatus, locale);
    try {
      this._eventManager.postEvent((Event)alarmEvent, null);
    } catch (InvalidEvent e) {
      _log.warn("Error while posting vSphere Online Health trigger event", (Throwable)e);
    } 
  }
  
  public boolean isAlarmRegistered(ManagedObjectReference alarmMoref) {
    if (alarmMoref == null)
      return false; 
    try {
      Alarm alarm = (Alarm)this._vcClient.createMo(alarmMoref);
      alarm.getInfo();
      return true;
    } catch (ManagedObjectNotFound e) {
      _log.debug("The vSphere Health alarm is not registered or it was deleted by the user.");
      return false;
    } 
  }
  
  private List<PropertyCollectorReader.PcResourceItem> getRegisteredAlarmPcResourceItems(PropertyCollector.FilterSpec filterSpec, List<String> filterProperties) {
    try {
      List<PropertyCollectorReader.PcResourceItem> pcResourceItems = this._pcReader.retrieveContent(filterSpec, filterProperties, 0, -1);
      return pcResourceItems;
    } catch (InvalidProperty e) {
      _log.warn("Error while retrieving the reigstered health alarm", (Throwable)e);
      return Collections.emptyList();
    } 
  }
  
  private AlarmSpec buildAlarmSpec(Locale locale) {
    OrAlarmExpression orAlarmExpression = buildAlarmExpression();
    AlarmSetting alarmSetting = new AlarmSetting(0, 0);
    String localized_alarm_name = this._localizedMessageProvider.getMessage("com.vmware.adc.general.alarm.name", locale);
    String localized_alarm_description = this._localizedMessageProvider.getMessage("com.vmware.adc.general.alarm.description", locale);
    AlarmSpec alarmSpec = new AlarmSpec(localized_alarm_name, localized_alarm_name, localized_alarm_description, true, (AlarmExpression)orAlarmExpression, null, Integer.valueOf(0), alarmSetting);
    return alarmSpec;
  }
  
  private OrAlarmExpression buildAlarmExpression() {
    List<AlarmExpression> alarmExpressions = new ArrayList<>();
    for (HealthSystem.HealthStatus healthStatus : Arrays.<HealthSystem.HealthStatus>asList(HealthSystem.HealthStatus.values())) {
      ManagedEntity.Status alarmStatus = convertHealthStatusToAlarmStatus(healthStatus);
      EventAlarmExpression.Comparison comparison = new EventAlarmExpression.Comparison("curstatus", "equals", healthStatus.toString());
      EventAlarmExpression eventAlarmExpression = new EventAlarmExpression(new EventAlarmExpression.Comparison[] { comparison }, (TypeName)new TypeNameImpl("vim.event.EventEx"), "vsphere.online.health.alarm.event", null, alarmStatus);
      alarmExpressions.add(eventAlarmExpression);
    } 
    AlarmExpression[] eventAlarmExpressionArray = new AlarmExpression[alarmExpressions.size()];
    eventAlarmExpressionArray = alarmExpressions.<AlarmExpression>toArray(eventAlarmExpressionArray);
    return new OrAlarmExpression(eventAlarmExpressionArray);
  }
  
  private ManagedEntity.Status convertHealthStatusToAlarmStatus(HealthSystem.HealthStatus healthStatus) {
    ManagedEntity.Status alarmStatus = null;
    switch (healthStatus) {
      case UNKNOWN:
        alarmStatus = ManagedEntity.Status.gray;
        return alarmStatus;
      case SKIPPED:
      case INFO:
        alarmStatus = ManagedEntity.Status.green;
        return alarmStatus;
    } 
    alarmStatus = ManagedEntity.Status.valueOf(healthStatus.toString());
    return alarmStatus;
  }
  
  private EventEx buildTriggerEvent(HealthSystem.HealthStatus newHealthStatus, Locale locale) {
    String message = this._localizedMessageProvider.getMessage("com.vmware.adc.trigger.event.message", locale);
    String severity = healthStatusToEventSeverity(newHealthStatus);
    KeyAnyValueImpl current_status_argument = new KeyAnyValueImpl();
    current_status_argument.setKey("curstatus");
    current_status_argument.setValue(newHealthStatus.toString());
    KeyAnyValueImpl[] arguments = { current_status_argument };
    EventEx alarmTriggerEvent = new EventEx();
    alarmTriggerEvent.setCreatedTime(Calendar.getInstance());
    alarmTriggerEvent.setUserName("vSphere Health");
    alarmTriggerEvent.setMessage(message);
    alarmTriggerEvent.setFullFormattedMessage(message);
    alarmTriggerEvent.setEventTypeId("vsphere.online.health.alarm.event");
    alarmTriggerEvent.setSeverity(severity);
    alarmTriggerEvent.setArguments((KeyAnyValue[])arguments);
    return alarmTriggerEvent;
  }
  
  private String healthStatusToEventSeverity(HealthSystem.HealthStatus healthStatus) {
    Event.EventSeverity eventSeverity = Event.EventSeverity.info;
    if (healthStatus == HealthSystem.HealthStatus.RED) {
      eventSeverity = Event.EventSeverity.error;
    } else if (healthStatus == HealthSystem.HealthStatus.YELLOW) {
      eventSeverity = Event.EventSeverity.warning;
    } 
    return eventSeverity.toString();
  }
}
