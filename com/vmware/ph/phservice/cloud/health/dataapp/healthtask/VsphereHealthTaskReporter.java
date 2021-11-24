package com.vmware.ph.phservice.cloud.health.dataapp.healthtask;

import com.vmware.ph.phservice.cloud.dataapp.internal.ProgressReporter;
import com.vmware.vim.binding.vim.fault.VimFault;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.util.Locale;

public interface VsphereHealthTaskReporter extends ProgressReporter {
  void triggerTask(ManagedObjectReference paramManagedObjectReference, Locale paramLocale, String paramString) throws VimFault, Exception;
  
  void close();
}
