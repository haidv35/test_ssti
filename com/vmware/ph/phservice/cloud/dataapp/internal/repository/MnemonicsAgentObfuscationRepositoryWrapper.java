package com.vmware.ph.phservice.cloud.dataapp.internal.repository;

import com.vmware.ph.phservice.cloud.dataapp.DataAppAgentId;
import com.vmware.ph.phservice.cloud.dataapp.repository.AgentObfuscationRepository;
import com.vmware.ph.phservice.common.Builder;
import com.vmware.ph.phservice.common.internal.obfuscation.MnemonicsConversionUtil;
import java.util.List;
import java.util.Map;

public class MnemonicsAgentObfuscationRepositoryWrapper implements AgentObfuscationRepository {
  private final AgentObfuscationRepository _wrappedRepository;
  
  private final Builder<List<String>> _mnemonicWordsBuilder;
  
  public MnemonicsAgentObfuscationRepositoryWrapper(AgentObfuscationRepository wrappedRepository, Builder<List<String>> mnemonicsListBuilder) {
    this._wrappedRepository = wrappedRepository;
    this._mnemonicWordsBuilder = mnemonicsListBuilder;
  }
  
  public void writeObfuscationMap(DataAppAgentId agentId, String objectId, Map<String, String> obfuscationMap) {
    List<String> mnemonicWords = this._mnemonicWordsBuilder.build();
    obfuscationMap = MnemonicsConversionUtil.convertToMnemonics(obfuscationMap, mnemonicWords, "-");
    this._wrappedRepository.writeObfuscationMap(agentId, objectId, obfuscationMap);
  }
  
  public Map<String, String> readObfuscationMap(DataAppAgentId agentId, String objectId) {
    return this._wrappedRepository.readObfuscationMap(agentId, objectId);
  }
}
