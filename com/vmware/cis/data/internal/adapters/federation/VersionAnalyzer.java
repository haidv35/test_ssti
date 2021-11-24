package com.vmware.cis.data.internal.adapters.federation;

public final class VersionAnalyzer {
  private static final MajorMinorVersion BEGINNING_OF_RISE = new MajorMinorVersion(6, 5);
  
  private static final MajorMinorVersion BEGINNING_OF_VIM_DP = new MajorMinorVersion(6, 6);
  
  public static boolean isVersionBeforeRise(String version) {
    assert version != null;
    MajorMinorVersion actual = MajorMinorVersion.parseVersion(version);
    return isVersionBeforeRise(actual);
  }
  
  public static boolean isVersionBeforeRise(MajorMinorVersion version) {
    assert version != null;
    return (BEGINNING_OF_RISE.compareTo(version) > 0);
  }
  
  public static boolean isVersionBeforeVimDp(String version) {
    assert version != null;
    MajorMinorVersion actual = MajorMinorVersion.parseVersion(version);
    return isVersionBeforeVimDp(actual);
  }
  
  public static boolean isVersionBeforeVimDp(MajorMinorVersion version) {
    assert version != null;
    return (BEGINNING_OF_VIM_DP.compareTo(version) > 0);
  }
}
