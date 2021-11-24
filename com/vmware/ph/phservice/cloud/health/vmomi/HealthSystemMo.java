package com.vmware.ph.phservice.cloud.health.vmomi;

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.aj.MethodLogger;
import com.vmware.ph.phservice.cloud.health.HealthSystem;
import com.vmware.vim.binding.vim.fault.VsanFault;
import com.vmware.vim.binding.vmodl.ManagedObjectReference;
import com.vmware.vim.vmomi.core.Future;
import com.vmware.vim.vsan.binding.vim.cluster.VsanClusterHealthSummary;
import com.vmware.vim.vsan.binding.vim.cluster.VsanVcClusterHealthSystem;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.runtime.internal.Conversions;
import org.aspectj.runtime.reflect.Factory;

@Loggable(value = 1, prepend = true, trim = false)
public class HealthSystemMo extends BaseHealthSystemMo implements VsanVcClusterHealthSystem {
  public static final String MO_REF_TYPE = "VsanVcClusterHealthSystem";
  
  public static final String MO_REF_ID = "cloud-health";
  
  private final HealthSystem _healthSystem;
  
  private final ManagedObjectReference _ref;
  
  static {
    ajc$preClinit();
  }
  
  public HealthSystemMo(HealthSystem healthSystem) {
    this._healthSystem = healthSystem;
    this._ref = new ManagedObjectReference("VsanVcClusterHealthSystem", "cloud-health");
  }
  
  public void queryClusterHealthSummary(ManagedObjectReference moRef, Integer vmCreateTimeout, String[] objUuids, Boolean includeObjUuids, String[] fields, Boolean fetchFromCache, String perspective, ManagedObjectReference[] hosts, Boolean includeDataProtectionHealth, Future result) {
    ManagedObjectReference managedObjectReference = moRef;
    Integer integer = vmCreateTimeout;
    String[] arrayOfString1 = objUuids;
    Boolean bool1 = includeObjUuids;
    String[] arrayOfString2 = fields;
    Boolean bool2 = fetchFromCache;
    String str = perspective;
    ManagedObjectReference[] arrayOfManagedObjectReference = hosts;
    Boolean bool3 = includeDataProtectionHealth;
    Future future = result;
    Object[] arrayOfObject = new Object[10];
    arrayOfObject[0] = managedObjectReference;
    arrayOfObject[1] = integer;
    arrayOfObject[2] = arrayOfString1;
    arrayOfObject[3] = bool1;
    arrayOfObject[4] = arrayOfString2;
    arrayOfObject[5] = bool2;
    arrayOfObject[6] = str;
    arrayOfObject[7] = arrayOfManagedObjectReference;
    arrayOfObject[8] = bool3;
    arrayOfObject[9] = future;
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_0, this, this, arrayOfObject);
    if (!MethodLogger.ajc$cflowCounter$0.isValid()) {
      Object[] arrayOfObject1 = new Object[12];
      arrayOfObject1[0] = this;
      arrayOfObject1[1] = managedObjectReference;
      arrayOfObject1[2] = integer;
      arrayOfObject1[3] = arrayOfString1;
      arrayOfObject1[4] = bool1;
      arrayOfObject1[5] = arrayOfString2;
      arrayOfObject1[6] = bool2;
      arrayOfObject1[7] = str;
      arrayOfObject1[8] = arrayOfManagedObjectReference;
      arrayOfObject1[9] = bool3;
      arrayOfObject1[10] = future;
      arrayOfObject1[11] = joinPoint;
      HealthSystemMo$AjcClosure1 healthSystemMo$AjcClosure1;
      MethodLogger.aspectOf().wrapClass((healthSystemMo$AjcClosure1 = new HealthSystemMo$AjcClosure1(arrayOfObject1)).linkClosureAndJoinPoint(69648));
      return;
    } 
    queryClusterHealthSummary_aroundBody0(this, managedObjectReference, integer, arrayOfString1, bool1, arrayOfString2, bool2, str, arrayOfManagedObjectReference, bool3, future, joinPoint);
  }
  
  public VsanClusterHealthSummary queryClusterHealthSummary(ManagedObjectReference moRef, Integer vmCreateTimeout, String[] objUuids, Boolean includeObjUuids, String[] fields, Boolean fetchFromCache, String perspective, ManagedObjectReference[] hosts, Boolean includeDataProtectionHealth) throws VsanFault {
    ManagedObjectReference managedObjectReference = moRef;
    Integer integer = vmCreateTimeout;
    String[] arrayOfString1 = objUuids;
    Boolean bool1 = includeObjUuids;
    String[] arrayOfString2 = fields;
    Boolean bool2 = fetchFromCache;
    String str = perspective;
    ManagedObjectReference[] arrayOfManagedObjectReference = hosts;
    Boolean bool3 = includeDataProtectionHealth;
    Object[] arrayOfObject = new Object[9];
    arrayOfObject[0] = managedObjectReference;
    arrayOfObject[1] = integer;
    arrayOfObject[2] = arrayOfString1;
    arrayOfObject[3] = bool1;
    arrayOfObject[4] = arrayOfString2;
    arrayOfObject[5] = bool2;
    arrayOfObject[6] = str;
    arrayOfObject[7] = arrayOfManagedObjectReference;
    arrayOfObject[8] = bool3;
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_1, this, this, arrayOfObject);
    if (!MethodLogger.ajc$cflowCounter$0.isValid()) {
      Object[] arrayOfObject1 = new Object[11];
      arrayOfObject1[0] = this;
      arrayOfObject1[1] = managedObjectReference;
      arrayOfObject1[2] = integer;
      arrayOfObject1[3] = arrayOfString1;
      arrayOfObject1[4] = bool1;
      arrayOfObject1[5] = arrayOfString2;
      arrayOfObject1[6] = bool2;
      arrayOfObject1[7] = str;
      arrayOfObject1[8] = arrayOfManagedObjectReference;
      arrayOfObject1[9] = bool3;
      arrayOfObject1[10] = joinPoint;
      HealthSystemMo$AjcClosure3 healthSystemMo$AjcClosure3;
      return (VsanClusterHealthSummary)MethodLogger.aspectOf().wrapClass((healthSystemMo$AjcClosure3 = new HealthSystemMo$AjcClosure3(arrayOfObject1)).linkClosureAndJoinPoint(69648));
    } 
    return queryClusterHealthSummary_aroundBody2(this, managedObjectReference, integer, arrayOfString1, bool1, arrayOfString2, bool2, str, arrayOfManagedObjectReference, bool3, joinPoint);
  }
  
  public void getVsanClusterSilentChecks(ManagedObjectReference moRef, Future result) {
    ManagedObjectReference managedObjectReference = moRef;
    Future future = result;
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_2, this, this, managedObjectReference, future);
    if (!MethodLogger.ajc$cflowCounter$0.isValid()) {
      Object[] arrayOfObject = new Object[4];
      arrayOfObject[0] = this;
      arrayOfObject[1] = managedObjectReference;
      arrayOfObject[2] = future;
      arrayOfObject[3] = joinPoint;
      HealthSystemMo$AjcClosure5 healthSystemMo$AjcClosure5;
      MethodLogger.aspectOf().wrapClass((healthSystemMo$AjcClosure5 = new HealthSystemMo$AjcClosure5(arrayOfObject)).linkClosureAndJoinPoint(69648));
      return;
    } 
    getVsanClusterSilentChecks_aroundBody4(this, managedObjectReference, future, joinPoint);
  }
  
  public String[] getVsanClusterSilentChecks(ManagedObjectReference moRef) {
    ManagedObjectReference managedObjectReference = moRef;
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_3, this, this, managedObjectReference);
    if (!MethodLogger.ajc$cflowCounter$0.isValid()) {
      Object[] arrayOfObject = new Object[3];
      arrayOfObject[0] = this;
      arrayOfObject[1] = managedObjectReference;
      arrayOfObject[2] = joinPoint;
      HealthSystemMo$AjcClosure7 healthSystemMo$AjcClosure7;
      return (String[])MethodLogger.aspectOf().wrapClass((healthSystemMo$AjcClosure7 = new HealthSystemMo$AjcClosure7(arrayOfObject)).linkClosureAndJoinPoint(69648));
    } 
    return getVsanClusterSilentChecks_aroundBody6(this, managedObjectReference, joinPoint);
  }
  
  public void setVsanClusterSilentChecks(ManagedObjectReference moRef, String[] addSilentChecks, String[] removeSilentChecks, Future result) {
    ManagedObjectReference managedObjectReference = moRef;
    String[] arrayOfString1 = addSilentChecks, arrayOfString2 = removeSilentChecks;
    Future future = result;
    Object[] arrayOfObject = new Object[4];
    arrayOfObject[0] = managedObjectReference;
    arrayOfObject[1] = arrayOfString1;
    arrayOfObject[2] = arrayOfString2;
    arrayOfObject[3] = future;
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_4, this, this, arrayOfObject);
    if (!MethodLogger.ajc$cflowCounter$0.isValid()) {
      Object[] arrayOfObject1 = new Object[6];
      arrayOfObject1[0] = this;
      arrayOfObject1[1] = managedObjectReference;
      arrayOfObject1[2] = arrayOfString1;
      arrayOfObject1[3] = arrayOfString2;
      arrayOfObject1[4] = future;
      arrayOfObject1[5] = joinPoint;
      HealthSystemMo$AjcClosure9 healthSystemMo$AjcClosure9;
      MethodLogger.aspectOf().wrapClass((healthSystemMo$AjcClosure9 = new HealthSystemMo$AjcClosure9(arrayOfObject1)).linkClosureAndJoinPoint(69648));
      return;
    } 
    setVsanClusterSilentChecks_aroundBody8(this, managedObjectReference, arrayOfString1, arrayOfString2, future, joinPoint);
  }
  
  public boolean setVsanClusterSilentChecks(ManagedObjectReference moRef, String[] addSilentChecks, String[] removeSilentChecks) throws VsanFault {
    ManagedObjectReference managedObjectReference = moRef;
    String[] arrayOfString1 = addSilentChecks, arrayOfString2 = removeSilentChecks;
    Object[] arrayOfObject = new Object[3];
    arrayOfObject[0] = managedObjectReference;
    arrayOfObject[1] = arrayOfString1;
    arrayOfObject[2] = arrayOfString2;
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_5, this, this, arrayOfObject);
    if (!MethodLogger.ajc$cflowCounter$0.isValid()) {
      Object[] arrayOfObject1 = new Object[5];
      arrayOfObject1[0] = this;
      arrayOfObject1[1] = managedObjectReference;
      arrayOfObject1[2] = arrayOfString1;
      arrayOfObject1[3] = arrayOfString2;
      arrayOfObject1[4] = joinPoint;
      HealthSystemMo$AjcClosure11 healthSystemMo$AjcClosure11;
      return Conversions.booleanValue(MethodLogger.aspectOf().wrapClass((healthSystemMo$AjcClosure11 = new HealthSystemMo$AjcClosure11(arrayOfObject1)).linkClosureAndJoinPoint(69648)));
    } 
    return setVsanClusterSilentChecks_aroundBody10(this, managedObjectReference, arrayOfString1, arrayOfString2, joinPoint);
  }
  
  public ManagedObjectReference _getRef() {
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_6, this, this);
    if (!MethodLogger.ajc$cflowCounter$0.isValid()) {
      Object[] arrayOfObject = new Object[2];
      arrayOfObject[0] = this;
      arrayOfObject[1] = joinPoint;
      HealthSystemMo$AjcClosure13 healthSystemMo$AjcClosure13;
      return (ManagedObjectReference)MethodLogger.aspectOf().wrapClass((healthSystemMo$AjcClosure13 = new HealthSystemMo$AjcClosure13(arrayOfObject)).linkClosureAndJoinPoint(69648));
    } 
    return _getRef_aroundBody12(this, joinPoint);
  }
  
  @Loggable(value = 1, prepend = true, trim = false)
  private VsanClusterHealthSummary queryClusterHealthSummary(ManagedObjectReference moRef, Boolean fetchFromCache, String perspective, Locale locale, String sessionUser) throws VsanFault {
    ManagedObjectReference managedObjectReference = moRef;
    Boolean bool = fetchFromCache;
    String str1 = perspective;
    Locale locale1 = locale;
    String str2 = sessionUser;
    Object[] arrayOfObject1 = new Object[5];
    arrayOfObject1[0] = managedObjectReference;
    arrayOfObject1[1] = bool;
    arrayOfObject1[2] = str1;
    arrayOfObject1[3] = locale1;
    arrayOfObject1[4] = str2;
    JoinPoint joinPoint = Factory.makeJP(ajc$tjp_7, this, this, arrayOfObject1);
    Object[] arrayOfObject2 = new Object[7];
    arrayOfObject2[0] = this;
    arrayOfObject2[1] = managedObjectReference;
    arrayOfObject2[2] = bool;
    arrayOfObject2[3] = str1;
    arrayOfObject2[4] = locale1;
    arrayOfObject2[5] = str2;
    arrayOfObject2[6] = joinPoint;
    HealthSystemMo$AjcClosure15 healthSystemMo$AjcClosure15;
    return (VsanClusterHealthSummary)MethodLogger.aspectOf().wrapMethod((healthSystemMo$AjcClosure15 = new HealthSystemMo$AjcClosure15(arrayOfObject2)).linkClosureAndJoinPoint(69648));
  }
  
  private HashSet<String> optionalArrayToList(String[] addSilentChecks) {
    return new HashSet<>(Arrays.asList((Object[])Optional.ofNullable(addSilentChecks).orElse(new String[0])));
  }
  
  protected Locale getCallerLocale(Future serverFuture) {
    return Locale.ENGLISH;
  }
  
  protected ManagedObjectReference addVcGuidToMoRef(ManagedObjectReference moRef) {
    return moRef;
  }
  
  protected String getSessionUser(Future serverFuture) {
    return null;
  }
}
