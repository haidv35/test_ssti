package com.vmware.ph.phservice.collector.internal.manifest.xml.obfuscation;

import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationRule;
import java.util.List;
import org.w3c.dom.Node;

public interface ObfuscationRulesParser {
  List<ObfuscationRule> parse(Node paramNode);
}
