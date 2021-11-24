package com.vmware.ph.phservice.cloud.health.repository;

import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import java.io.IOException;
import java.util.Set;

public interface SilencedTestsRepository {
  Set<String> getSilencedTests(ManagedObjectReference paramManagedObjectReference);
  
  void updateSilencedTests(ManagedObjectReference paramManagedObjectReference, Set<String> paramSet1, Set<String> paramSet2) throws IOException;
}
