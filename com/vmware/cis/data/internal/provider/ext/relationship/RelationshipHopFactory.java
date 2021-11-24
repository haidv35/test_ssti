package com.vmware.cis.data.internal.provider.ext.relationship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

final class RelationshipHopFactory {
  public static List<RelationshipHop> createRelationshipHops(RelatedPropertyDescriptor relatedPropertyDescriptor) {
    assert relatedPropertyDescriptor != null;
    List<RelationshipHop> initialRelationshipHops = buildRelationshipHops(relatedPropertyDescriptor);
    List<RelationshipHop> normalizedRelationshipHops = normalizeRelationshipHops(initialRelationshipHops);
    RelationshipHop targetPropertyRelationshipHop = new RelationshipHop(relatedPropertyDescriptor.getTargetName(), "@modelKey");
    normalizedRelationshipHops.add(targetPropertyRelationshipHop);
    return Collections.unmodifiableList(normalizedRelationshipHops);
  }
  
  public static List<RelationshipHop> reverseRelationshipHops(List<RelationshipHop> relationshipHops) {
    assert relationshipHops != null;
    assert !relationshipHops.isEmpty();
    List<RelationshipHop> reversedRelationshipHops = new ArrayList<>();
    int relationshipHopsCount = relationshipHops.size();
    for (int r = relationshipHopsCount - 1; r >= 0; r--) {
      RelationshipHop relationshipHop = relationshipHops.get(r);
      reversedRelationshipHops.add(new RelationshipHop(relationshipHop
            .getTargetModelProperty(), relationshipHop
            .getSourceModelProperty()));
    } 
    return reversedRelationshipHops;
  }
  
  private static List<RelationshipHop> buildRelationshipHops(RelatedPropertyDescriptor relatedPropertyDescriptor) {
    assert !relatedPropertyDescriptor.getRelationships().isEmpty();
    List<RelationshipHop> relationshipHops = new ArrayList<>();
    for (RelationshipDescriptor relationshipDescriptor : relatedPropertyDescriptor.getRelationships()) {
      RelationshipHop relationshipHop = RelationshipHop.buildForRelationshipDescriptor(relationshipDescriptor);
      relationshipHops.add(relationshipHop);
    } 
    return relationshipHops;
  }
  
  private static List<RelationshipHop> normalizeRelationshipHops(List<RelationshipHop> relationshipHops) {
    assert !relationshipHops.isEmpty();
    if (relationshipHops.size() == 1)
      return relationshipHops; 
    List<RelationshipHop> normalizedRelationshipHops = new ArrayList<>();
    Iterator<RelationshipHop> relationshipHopsIterator = relationshipHops.iterator();
    RelationshipHop relationshipHop = relationshipHopsIterator.next();
    boolean lastNormalized = false;
    while (relationshipHopsIterator.hasNext()) {
      RelationshipHop nextRelationshipHop = relationshipHopsIterator.next();
      if (needsNormalization(relationshipHop, nextRelationshipHop)) {
        RelationshipHop normalizedRelationshipHop = new RelationshipHop(relationshipHop.getSourceModelProperty(), nextRelationshipHop.getTargetModelProperty());
        if (lastNormalized)
          normalizedRelationshipHops.remove(normalizedRelationshipHops.size() - 1); 
        normalizedRelationshipHops.add(normalizedRelationshipHop);
        relationshipHop = normalizedRelationshipHop;
        lastNormalized = true;
        continue;
      } 
      normalizedRelationshipHops.add(relationshipHop);
      relationshipHop = nextRelationshipHop;
      lastNormalized = false;
    } 
    if (!lastNormalized)
      normalizedRelationshipHops.add(relationshipHop); 
    return normalizedRelationshipHops;
  }
  
  private static boolean needsNormalization(RelationshipHop relationshipHop, RelationshipHop nextRelationshipHop) {
    assert relationshipHop != null;
    assert nextRelationshipHop != null;
    return ("@modelKey".equals(relationshipHop
        .getTargetModelProperty()) && "@modelKey"
      .equals(nextRelationshipHop
        .getSourceModelProperty()));
  }
}
