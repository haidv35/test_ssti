package com.vmware.ph.phservice.collector.internal.manifest.xml;

import com.vmware.ph.client.api.commondataformat20.Payload;
import com.vmware.ph.phservice.collector.internal.NamedQueryResultSetMapping;
import com.vmware.ph.phservice.collector.internal.NoOpNamedQueryResultSetMapping;
import com.vmware.ph.phservice.collector.internal.cdf.mapping.Mapping;
import com.vmware.ph.phservice.collector.internal.data.NamedQuery;
import com.vmware.ph.phservice.collector.internal.data.NamedQueryResultSet;
import com.vmware.ph.phservice.collector.internal.manifest.Manifest;
import com.vmware.ph.phservice.collector.internal.manifest.ManifestParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.JaxbMappingParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.mapping.MappingParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.obfuscation.JaxbObfuscationRulesParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.obfuscation.ObfuscationRulesParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.JaxbRequestSpecParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.QueryUtils;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.RequestSpecParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.query.data.RequestSpec;
import com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling.JaxbRequestScheduleSpecParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling.RequestScheduleSpecConverter;
import com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling.RequestScheduleSpecParser;
import com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling.data.RequestScheduleSpec;
import com.vmware.ph.phservice.collector.internal.manifest.xml.scheduling.data.ScheduleSpec;
import com.vmware.ph.phservice.collector.scheduler.CollectionSchedule;
import com.vmware.ph.phservice.common.internal.obfuscation.ObfuscationRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

public class XmlManifestParser implements ManifestParser {
  private static final Log _log = LogFactory.getLog(XmlManifestParser.class);
  
  private static final int DEFAULT_PAGE_SIZE = 5000;
  
  private final RequestSpecParser _requestParser;
  
  private final RequestScheduleSpecParser _requestScheduleSpecParser;
  
  private final MappingParser _mappingParser;
  
  private final ObfuscationRulesParser _obfuscationRulesParser;
  
  private boolean _isLegacySchedulingSupported = false;
  
  public XmlManifestParser(boolean isLegacySchedulingSupported) {
    this();
    this._isLegacySchedulingSupported = isLegacySchedulingSupported;
  }
  
  public XmlManifestParser() {
    this(new JaxbRequestSpecParser(), new JaxbRequestScheduleSpecParser(), new JaxbMappingParser(), new JaxbObfuscationRulesParser());
  }
  
  XmlManifestParser(RequestSpecParser requestSpecParser, RequestScheduleSpecParser requestScheduleSpecParser, MappingParser mappingParser, ObfuscationRulesParser obfuscationRulesParser, boolean isLegacySchedulingSupported) {
    this(requestSpecParser, requestScheduleSpecParser, mappingParser, obfuscationRulesParser);
    this._isLegacySchedulingSupported = isLegacySchedulingSupported;
  }
  
  XmlManifestParser(RequestSpecParser requestSpecParser, RequestScheduleSpecParser requestScheduleSpecParser, MappingParser mappingParser, ObfuscationRulesParser obfuscationRulesParser) {
    this._requestParser = requestSpecParser;
    this._requestScheduleSpecParser = requestScheduleSpecParser;
    this._mappingParser = mappingParser;
    this._obfuscationRulesParser = obfuscationRulesParser;
  }
  
  public boolean isApplicable(String manifestStr) {
    return (manifestStr != null && manifestStr.startsWith("<manifest"));
  }
  
  public Manifest getManifest(String manifestStr, CollectionSchedule schedule) {
    Document manifestDoc = getManifestDocForSchedule(manifestStr, schedule, this._isLegacySchedulingSupported);
    if (manifestDoc == null) {
      if (_log.isInfoEnabled())
        _log.info(
            String.format("Manifest section for %s schedule is empty.", new Object[] { schedule })); 
      return null;
    } 
    NamedQuery[] queries = parseQueries(manifestDoc, this._requestParser);
    NamedQuery[] queriesForSchedule = getNamedQueriesForSchedule(manifestDoc, schedule, queries);
    if (queriesForSchedule == null || queriesForSchedule.length == 0) {
      if (_log.isDebugEnabled())
        _log.debug(
            String.format("Manifest section for %s schedule is empty.", new Object[] { schedule })); 
      return null;
    } 
    NamedQueryResultSetMapping<Payload> namedQueryMapping = getNamedQueryMapping(manifestDoc, this._mappingParser);
    List<ObfuscationRule> obfuscationRules = parseObfuscationRules(manifestDoc, this._obfuscationRulesParser);
    int recommendedPageSize = XmlManifestUtils.getRecommendedPageSize(manifestDoc, 5000);
    Manifest result = Manifest.Builder.forQueries(queriesForSchedule).withMapping(getNonNullNamedQueryResultSetMapping(namedQueryMapping)).withObfuscationRules(getNonNullObfuscationRules(obfuscationRules)).withRecommendedPageSize(recommendedPageSize).build();
    return result;
  }
  
  public Set<CollectionSchedule> getManifestSchedules(String manifestStr) {
    Document manifestDoc = XmlManifestUtils.getNonDailySection(manifestStr);
    RequestScheduleSpec requestScheduleSpec = XmlManifestUtils.parseRequestScheduleSpec(manifestDoc, this._requestScheduleSpecParser);
    Set<CollectionSchedule> schedules = Collections.emptySet();
    if (requestScheduleSpec == null) {
      if (_log.isDebugEnabled())
        _log.debug("The manifest contains legacy scheduling only."); 
      if (this._isLegacySchedulingSupported)
        schedules = getLegacyCollectionSchedules(manifestStr, manifestDoc); 
    } else {
      schedules = new TreeSet<>();
      for (ScheduleSpec scheduleSpec : requestScheduleSpec.getSchedules()) {
        CollectionSchedule schedule = RequestScheduleSpecConverter.convertScheduleSpecToSchedule(scheduleSpec);
        schedules.add(schedule);
      } 
    } 
    if (_log.isInfoEnabled())
      _log.info(
          String.format("The manifest contains the following schedules: %s.", new Object[] { schedules })); 
    return schedules;
  }
  
  private static Document getManifestDocForSchedule(String manifestStr, CollectionSchedule collectionSchedule, boolean isLegacySchedulingSupported) {
    Document manifestDocument;
    boolean containsRequestScheduleSection = XmlManifestUtils.containsRequestScheduleSection(manifestStr);
    if (containsRequestScheduleSection) {
      manifestDocument = XmlManifestUtils.getNonDailySection(manifestStr);
    } else if (isLegacySchedulingSupported) {
      manifestDocument = getManifestDocForLegacySchedule(manifestStr, collectionSchedule);
    } else {
      manifestDocument = null;
    } 
    return manifestDocument;
  }
  
  private static Document getManifestDocForLegacySchedule(String manifestStr, CollectionSchedule collectionSchedule) {
    Document manifestDocument = null;
    if (CollectionSchedule.DAILY.equals(collectionSchedule)) {
      manifestDocument = XmlManifestUtils.getDailySection(manifestStr);
    } else if (CollectionSchedule.WEEKLY.equals(collectionSchedule)) {
      manifestDocument = XmlManifestUtils.getNonDailySection(manifestStr);
    } else if (collectionSchedule == null) {
      Document dailyManifestDoc = XmlManifestUtils.getDailySection(manifestStr);
      if (dailyManifestDoc != null) {
        manifestDocument = dailyManifestDoc;
      } else {
        manifestDocument = XmlManifestUtils.getNonDailySection(manifestStr);
      } 
    } 
    return manifestDocument;
  }
  
  private static List<ObfuscationRule> parseObfuscationRules(Document manifestDoc, ObfuscationRulesParser obfuscationRulesParser) {
    List<ObfuscationRule> obfuscationRules = XmlManifestUtils.parseObfuscationRules(manifestDoc, obfuscationRulesParser);
    return obfuscationRules;
  }
  
  private static NamedQuery[] parseQueries(Document manifestDoc, RequestSpecParser requestParser) {
    RequestSpec request = XmlManifestUtils.parseRequestSpec(manifestDoc, requestParser);
    if (request.getQueries() == null)
      return new NamedQuery[0]; 
    NamedQuery[] queries = new NamedQuery[request.getQueries().size()];
    for (int i = 0; i < queries.length; i++)
      queries[i] = QueryUtils.viseQuerySpecToRiseQuery(request.getQueries().get(i)); 
    return queries;
  }
  
  private static NamedQueryResultSetMapping<Payload> getNamedQueryMapping(Document manifestDoc, MappingParser mappingParser) {
    Mapping<NamedQueryResultSet, Payload> mapping = XmlManifestUtils.parseMapping(manifestDoc, mappingParser);
    NamedQueryResultSetMapping<Payload> namedQueryMapping = (NamedQueryResultSetMapping)mapping;
    return namedQueryMapping;
  }
  
  private static <T> NamedQueryResultSetMapping<T> getNonNullNamedQueryResultSetMapping(NamedQueryResultSetMapping<T> resultSetMapping) {
    if (resultSetMapping == null)
      return new NoOpNamedQueryResultSetMapping<>(); 
    return resultSetMapping;
  }
  
  private NamedQuery[] getNamedQueriesForSchedule(Document manifestDoc, CollectionSchedule schedule, NamedQuery[] queries) {
    if (schedule == null)
      return queries; 
    RequestScheduleSpec requestScheduleSpec = XmlManifestUtils.parseRequestScheduleSpec(manifestDoc, this._requestScheduleSpecParser);
    if (requestScheduleSpec == null) {
      boolean isLegacySchedule = (CollectionSchedule.DAILY.equals(schedule) || CollectionSchedule.WEEKLY.equals(schedule));
      return isLegacySchedule ? queries : null;
    } 
    Map<CollectionSchedule, NamedQuery[]> schedulesToQueries = RequestScheduleSpecConverter.convertToSchedulesToQueriesMap(requestScheduleSpec, queries);
    return schedulesToQueries.get(schedule);
  }
  
  private Set<CollectionSchedule> getLegacyCollectionSchedules(String manifestContent, Document weeklyManifestDoc) {
    Document dailyManifestDoc = XmlManifestUtils.getDailySection(manifestContent);
    if (dailyManifestDoc == null)
      return Collections.singleton(CollectionSchedule.WEEKLY); 
    RequestSpec request = XmlManifestUtils.parseRequestSpec(weeklyManifestDoc, this._requestParser);
    if (request.getQueries() == null || request.getQueries().isEmpty())
      return Collections.singleton(CollectionSchedule.DAILY); 
    return new HashSet<>(Arrays.asList(new CollectionSchedule[] { CollectionSchedule.DAILY, CollectionSchedule.WEEKLY }));
  }
  
  private static List<ObfuscationRule> getNonNullObfuscationRules(List<ObfuscationRule> obfuscationRules) {
    if (obfuscationRules == null)
      return new ArrayList<>(); 
    return obfuscationRules;
  }
}
