package com.vmware.ph.phservice.common.vmomi;

import com.vmware.vim.vmomi.core.exception.VmodlAlreadyLoadedException;
import com.vmware.vim.vmomi.core.types.VmodlContext;
import java.util.Map;

public class VmodlContextProvider {
  public static VmodlContext getVmodlContextForPacakgeAndClass(Map<String, Class<?>> vmodlPackageNameToPackageClass, boolean lazyInit) {
    for (Map.Entry<String, Class<?>> vmodlPackageToPackageClassEntry : vmodlPackageNameToPackageClass.entrySet()) {
      String vmodlPackageName = vmodlPackageToPackageClassEntry.getKey();
      Class<?> vmodlPackageClass = vmodlPackageToPackageClassEntry.getValue();
      getVmodlContextForPacakgeAndClass(vmodlPackageName, vmodlPackageClass, lazyInit);
    } 
    return VmodlContext.getContext();
  }
  
  public static VmodlContext getVmodlContextForPacakgeAndClass(String vmodlPackageName, Class<?> vmodlPackageClass, boolean lazyInit) {
    return getVmodlContextForPacakgeAndClassLoader(vmodlPackageName, vmodlPackageClass
        
        .getClassLoader(), lazyInit);
  }
  
  public static VmodlContext getVmodlContextForPacakgeAndClassLoader(Map<String, ClassLoader> vmodlPackageNameToPackageClassLoader, boolean lazyInit) {
    for (Map.Entry<String, ClassLoader> vmodlPackageToPackageClassLoaderEntry : vmodlPackageNameToPackageClassLoader.entrySet()) {
      String vmodlPackageName = vmodlPackageToPackageClassLoaderEntry.getKey();
      ClassLoader vmodlPackageClassLoader = vmodlPackageToPackageClassLoaderEntry.getValue();
      getVmodlContextForPacakgeAndClassLoader(vmodlPackageName, vmodlPackageClassLoader, lazyInit);
    } 
    return VmodlContext.getContext();
  }
  
  public static VmodlContext getVmodlContextForPacakgeAndClassLoader(String vmodlPackageName, ClassLoader vmodlPackageClassLoader, boolean lazyInit) {
    return getVmodlContextForPacakgesAndClassLoader(new String[] { vmodlPackageName }, vmodlPackageClassLoader, lazyInit);
  }
  
  public static VmodlContext getVmodlContextForPacakgesAndClassLoader(String[] vmodlPackageNames, ClassLoader vmodlPackageClassLoader, boolean lazyInit) {
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      try {
        Thread.currentThread().setContextClassLoader(vmodlPackageClassLoader);
        VmodlContext.initContext(vmodlPackageNames, lazyInit);
      } catch (VmodlAlreadyLoadedException vmodlAlreadyLoadedException) {}
    } finally {
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    } 
    return VmodlContext.getContext();
  }
}
