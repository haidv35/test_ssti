package com.vmware.cis.data.internal.provider.ext.relationship;

import com.vmware.cis.data.internal.util.PropertyUtil;
import com.vmware.cis.data.internal.util.QualifiedProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

final class RelationshipQueryFactory {
  public static List<RelationshipQuery> createRelationshipQueriesForSelect(RelatedPropertyDescriptor relatedPropertyDescriptor, List<RelationshipHop> relationshipHops) {
    assert relatedPropertyDescriptor != null;
    assert relationshipHops != null;
    assert relationshipHops.isEmpty();
    List<RelationshipHop> relationshipHopsForProperty = RelationshipHopFactory.createRelationshipHops(relatedPropertyDescriptor);
    relationshipHops.addAll(relationshipHopsForProperty);
    String sourceModel = QualifiedProperty.forQualifiedName(relatedPropertyDescriptor.getName()).getResourceModel();
    String targetModel = QualifiedProperty.forQualifiedName(relatedPropertyDescriptor.getTargetName()).getResourceModel();
    return createRelationshipQueries(relationshipHopsForProperty, sourceModel, targetModel, true);
  }
  
  public static List<RelationshipQuery> createRelationshipQueriesForFilter(RelatedPropertyDescriptor relatedPropertyDescriptor, List<RelationshipHop> relationshipHops) {
    assert relatedPropertyDescriptor != null;
    assert relationshipHops != null;
    assert relationshipHops.isEmpty();
    List<RelationshipHop> relationshipHopsForProperty = RelationshipHopFactory.createRelationshipHops(relatedPropertyDescriptor);
    relationshipHopsForProperty = RelationshipHopFactory.reverseRelationshipHops(relationshipHopsForProperty);
    relationshipHops.addAll(relationshipHopsForProperty);
    String sourceModel = QualifiedProperty.forQualifiedName(relatedPropertyDescriptor.getTargetName()).getResourceModel();
    String targetModel = QualifiedProperty.forQualifiedName(relatedPropertyDescriptor.getName()).getResourceModel();
    return createRelationshipQueries(relationshipHopsForProperty, sourceModel, targetModel, false);
  }
  
  private static List<RelationshipQuery> createRelationshipQueries(List<RelationshipHop> relationshipHops, String sourceModel, String targetModel, boolean isForwardDirection) {
    assert relationshipHops != null;
    assert !relationshipHops.isEmpty();
    assert sourceModel != null;
    assert targetModel != null;
    List<RelationshipQuery> relationshipQueries = new ArrayList<>();
    int relationshipHopsCount = relationshipHops.size();
    for (int r = 0; r < relationshipHopsCount - 1; r++) {
      RelationshipHop relationshipHop = relationshipHops.get(r);
      RelationshipHop nextRelationshipHop = relationshipHops.get(r + 1);
      String targetProperty = nextRelationshipHop.getSourceModelProperty();
      String sourceJoinProperty = relationshipHop.getSourceModelProperty();
      String targetJoinProperty = relationshipHop.getTargetModelProperty();
      LinkedHashSet<String> targetProperties = new LinkedHashSet<>(Arrays.asList(new String[] { targetProperty }));
      boolean selectType = (isForwardDirection && r == relationshipHopsCount - 2 && PropertyUtil.isModelKey(targetProperty));
      if (selectType)
        targetProperties.add("@type"); 
      RelationshipQuery relationshipQuery = new RelationshipQuery(targetProperties, sourceJoinProperty, targetJoinProperty);
      relationshipQueries.add(relationshipQuery);
    } 
    return Collections.unmodifiableList(relationshipQueries);
  }
}
