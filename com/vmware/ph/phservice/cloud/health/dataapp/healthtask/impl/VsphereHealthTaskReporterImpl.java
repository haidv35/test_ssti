package com.vmware.ph.phservice.cloud.health.dataapp.healthtask.impl;

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.aj.MethodLogger;
import com.vmware.ph.phservice.cloud.health.dataapp.healthtask.VsphereHealthTaskReporter;
import com.vmware.ph.phservice.common.internal.i18n.LocalizedMessageProvider;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.ph.phservice.common.vmomi.internal.server.VmomiUtil;
import com.vmware.vim.binding.vim.ServiceInstanceContent;
import com.vmware.vim.binding.vim.Task;
import com.vmware.vim.binding.vim.TaskInfo;
import com.vmware.vim.binding.vim.TaskManager;
import com.vmware.vim.binding.vim.fault.VimFault;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.Locale;
import java.util.Objects;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.runtime.internal.Conversions;
import org.aspectj.runtime.reflect.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Loggable(value = 1, prepend = true, trim = false)
public class VsphereHealthTaskReporterImpl implements VsphereHealthTaskReporter {
  private static final Logger _logger = LoggerFactory.getLogger(VsphereHealthTaskReporterImpl.class);
  
  private static final String DEFAULT_SESSION_USER_KEY = "com.vmware.adc.health.task.user";
  
  static final String FAULT_TASK_NOT_FOUND_KEY = "com.vmware.adc.health.fault.taskNotFound";
  
  static final String FAULT_TASK_CREATION_FAILED_KEY = "com.vmware.adc.health.fault.taskCreationFailed";
  
  private static final String VSPHERE_HEALTH_TASK_TYPE_ID = "com.vmware.vsphere.health.tasks.queryhealthsummary";
  
  private static final int PERCENT_DONE_INITIAL_PROGRESS = 10;
  
  private static final int PERCENT_DONE_TASK_SUCCESSFUL_COMPLETE = 100;
  
  private final VcClient _vcClient;
  
  private final LocalizedMessageProvider _localizedMessageProvider;
  
  private Task _taskMo;
  
  static {
    ajc$preClinit();
  }
  
  public VsphereHealthTaskReporterImpl(VcClient vcClient, LocalizedMessageProvider localizedMessageProvider) {
    this._vcClient = Objects.<VcClient>requireNonNull(vcClient);
    this._localizedMessageProvider = Objects.<LocalizedMessageProvider>requireNonNull(localizedMessageProvider);
  }
  
  public void triggerTask(ManagedObjectReference moRef, Locale locale, String sessionUser) {
    ManagedObjectReference managedObjectReference = moRef;
    Locale locale1 = locale;
    String str = sessionUser;
    Object[] arrayOfObject = new Object[3];
    arrayOfObject[0] = managedObjectReference;
    arrayOfObject[1] = locale1;
    arrayOfObject[2] = str;
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_0, this, this, arrayOfObject);
    if (!MethodLogger.ajc$cflowCounter$0.isValid()) {
      Object[] arrayOfObject1 = new Object[5];
      arrayOfObject1[0] = this;
      arrayOfObject1[1] = managedObjectReference;
      arrayOfObject1[2] = locale1;
      arrayOfObject1[3] = str;
      arrayOfObject1[4] = joinPoint;
      VsphereHealthTaskReporterImpl$AjcClosure1 vsphereHealthTaskReporterImpl$AjcClosure1;
      MethodLogger.aspectOf().wrapClass((vsphereHealthTaskReporterImpl$AjcClosure1 = new VsphereHealthTaskReporterImpl$AjcClosure1(arrayOfObject1)).linkClosureAndJoinPoint(69648));
      return;
    } 
    triggerTask_aroundBody0(this, managedObjectReference, locale1, str, joinPoint);
  }
  
  public void reportProgress(int percentDone) {
    int i = percentDone;
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_1, this, this, Conversions.intObject(i));
    if (!MethodLogger.ajc$cflowCounter$0.isValid()) {
      Object[] arrayOfObject = new Object[3];
      arrayOfObject[0] = this;
      arrayOfObject[1] = Conversions.intObject(i);
      arrayOfObject[2] = joinPoint;
      VsphereHealthTaskReporterImpl$AjcClosure3 vsphereHealthTaskReporterImpl$AjcClosure3;
      MethodLogger.aspectOf().wrapClass((vsphereHealthTaskReporterImpl$AjcClosure3 = new VsphereHealthTaskReporterImpl$AjcClosure3(arrayOfObject)).linkClosureAndJoinPoint(69648));
      return;
    } 
    reportProgress_aroundBody2(this, i, joinPoint);
  }
  
  public void reportSuccess() throws Exception {
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_2, this, this);
    if (!MethodLogger.ajc$cflowCounter$0.isValid()) {
      Object[] arrayOfObject = new Object[2];
      arrayOfObject[0] = this;
      arrayOfObject[1] = joinPoint;
      VsphereHealthTaskReporterImpl$AjcClosure5 vsphereHealthTaskReporterImpl$AjcClosure5;
      MethodLogger.aspectOf().wrapClass((vsphereHealthTaskReporterImpl$AjcClosure5 = new VsphereHealthTaskReporterImpl$AjcClosure5(arrayOfObject)).linkClosureAndJoinPoint(69648));
      return;
    } 
    reportSuccess_aroundBody4(this, joinPoint);
  }
  
  public void reportFailure(Exception error) throws Exception {
    Exception exception = error;
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_3, this, this, exception);
    if (!MethodLogger.ajc$cflowCounter$0.isValid()) {
      Object[] arrayOfObject = new Object[3];
      arrayOfObject[0] = this;
      arrayOfObject[1] = exception;
      arrayOfObject[2] = joinPoint;
      VsphereHealthTaskReporterImpl$AjcClosure7 vsphereHealthTaskReporterImpl$AjcClosure7;
      MethodLogger.aspectOf().wrapClass((vsphereHealthTaskReporterImpl$AjcClosure7 = new VsphereHealthTaskReporterImpl$AjcClosure7(arrayOfObject)).linkClosureAndJoinPoint(69648));
      return;
    } 
    reportFailure_aroundBody6(this, exception, joinPoint);
  }
  
  public void close() {
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_4, this, this);
    if (!MethodLogger.ajc$cflowCounter$0.isValid()) {
      Object[] arrayOfObject = new Object[2];
      arrayOfObject[0] = this;
      arrayOfObject[1] = joinPoint;
      VsphereHealthTaskReporterImpl$AjcClosure9 vsphereHealthTaskReporterImpl$AjcClosure9;
      MethodLogger.aspectOf().wrapClass((vsphereHealthTaskReporterImpl$AjcClosure9 = new VsphereHealthTaskReporterImpl$AjcClosure9(arrayOfObject)).linkClosureAndJoinPoint(69648));
      return;
    } 
    close_aroundBody8(this, joinPoint);
  }
  
  private ManagedObjectReference createVsphereHealthTask(ManagedObjectReference moRef, String sessionUser) {
    TaskInfo taskInfo;
    try {
      taskInfo = getVcTaskManager().createTask(moRef, "com.vmware.vsphere.health.tasks.queryhealthsummary", sessionUser, false, null, null);
    } catch (RuntimeException fault) {
      throw VmomiUtil.generateRuntimeFault(this._localizedMessageProvider, "com.vmware.adc.health.fault.taskCreationFailed", fault);
    } 
    ManagedObjectReference taskMoRef = null;
    if (taskInfo != null)
      taskMoRef = taskInfo.getTask(); 
    if (taskInfo == null || taskMoRef == null)
      throw VmomiUtil.generateRuntimeFault(this._localizedMessageProvider, "com.vmware.adc.health.fault.taskCreationFailed", null); 
    _logger.info("Successfully created vSphere Online health task with moRef: {}", taskMoRef);
    return taskMoRef;
  }
  
  private void updateTask(Integer percentDone, TaskInfo.State newState, Exception fault) throws VimFault {
    if (this._taskMo == null)
      throw VmomiUtil.generateRuntimeFault(this._localizedMessageProvider, "com.vmware.adc.health.fault.taskNotFound", null); 
    TaskInfo.State currentState = this._taskMo.getInfo().getState();
    if (currentState.equals(TaskInfo.State.running)) {
      if (percentDone != null)
        this._taskMo.UpdateProgress(percentDone.intValue()); 
      if (newState != null)
        this._taskMo.setState(newState, null, fault); 
    } else if (currentState.equals(TaskInfo.State.queued) && 
      newState != null) {
      this._taskMo.setState(newState, null, fault);
      if (newState.equals(TaskInfo.State.running) && percentDone != null)
        this._taskMo.UpdateProgress(percentDone.intValue()); 
    } 
  }
  
  private TaskManager getVcTaskManager() {
    try {
      ServiceInstanceContent serviceInstanceContent = this._vcClient.getServiceInstanceContent();
      return (TaskManager)this._vcClient.createMo(serviceInstanceContent.getTaskManager());
    } catch (RuntimeException e) {
      String message = "Failed to get TaskManager.";
      if (_logger.isDebugEnabled()) {
        _logger.debug(message, e);
      } else {
        _logger.error(message);
      } 
      throw new RuntimeException("Failed to get TaskManager.");
    } 
  }
  
  private Task initializeTaskMo(ManagedObjectReference taskMoRef) {
    return (Task)this._vcClient.createMo(taskMoRef);
  }
}
