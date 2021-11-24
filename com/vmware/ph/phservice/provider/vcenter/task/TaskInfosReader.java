package com.vmware.ph.phservice.provider.vcenter.task;

import com.vmware.ph.phservice.common.ItemsStream;
import com.vmware.ph.phservice.common.vim.vc.VcClient;
import com.vmware.vim.binding.vim.TaskFilterSpec;
import com.vmware.vim.binding.vim.TaskHistoryCollector;
import com.vmware.vim.binding.vim.TaskInfo;
import com.vmware.vim.binding.vim.TaskManager;
import com.vmware.vim.binding.vim.fault.InvalidState;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.binding.vmodl.fault.InvalidArgument;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TaskInfosReader implements ItemsStream<TaskInfo> {
  private static final Log _log = LogFactory.getLog(TaskInfosReader.class);
  
  private final VcClient _vcClient;
  
  private final TaskFilterSpec _taskFilterSpec;
  
  private final int _limit;
  
  private TaskHistoryCollector _taskHistoryCollector;
  
  public TaskInfosReader(VcClient vcClient, TaskFilterSpec taskFilterSpec, int limit) {
    this._vcClient = Objects.<VcClient>requireNonNull(vcClient);
    this._taskFilterSpec = Objects.<TaskFilterSpec>requireNonNull(taskFilterSpec);
    this._limit = limit;
  }
  
  TaskInfosReader(VcClient vcClient, TaskFilterSpec taskFilterSpec, int limit, TaskHistoryCollector taskHistoryCollector) {
    this(vcClient, taskFilterSpec, limit);
    this._taskHistoryCollector = taskHistoryCollector;
  }
  
  public void close() throws IOException {
    if (this._taskHistoryCollector != null) {
      this._taskHistoryCollector.remove();
      this._taskHistoryCollector = null;
    } 
  }
  
  public List<TaskInfo> read(int numItems) throws IllegalArgumentException {
    if (this._taskHistoryCollector == null)
      try {
        this
          ._taskHistoryCollector = createTaskHistoryCollector(this._vcClient, this._taskFilterSpec);
      } catch (InvalidState|IllegalStateException e) {
        _log.warn("Failed to create a TaskHistoryCollector", e);
        return Collections.emptyList();
      }  
    List<TaskInfo> tasksInfo = new ArrayList<>();
    try {
      TaskInfo[] nextTasksInfo = this._taskHistoryCollector.readNext(numItems);
      if (nextTasksInfo != null)
        tasksInfo.addAll(Arrays.asList(nextTasksInfo)); 
    } catch (InvalidArgument e) {
      throw new IllegalArgumentException(e);
    } 
    return tasksInfo;
  }
  
  public int getLimit() {
    return this._limit;
  }
  
  private static TaskHistoryCollector createTaskHistoryCollector(VcClient vcClient, TaskFilterSpec taskFilterSpec) throws InvalidState, IllegalStateException {
    ManagedObjectReference taskManagerMoRef = vcClient.getServiceInstanceContent().getTaskManager();
    TaskManager taskManager = vcClient.<TaskManager>createMo(taskManagerMoRef);
    ManagedObjectReference taskHistoryCollectorMoRef = taskManager.createCollector(taskFilterSpec);
    TaskHistoryCollector taskHistoryCollector = vcClient.<TaskHistoryCollector>createMo(taskHistoryCollectorMoRef);
    return taskHistoryCollector;
  }
}
