package org.hibernate.tool.internal.reveng.binder;

import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.ToOne;
import org.hibernate.tool.api.reveng.AssociationInfo;
import org.hibernate.tool.internal.reveng.util.RevengUtils;

class EntityPropertyBinder extends AbstractBinder {

  private final PropertyBinder propertyBinder;

  private EntityPropertyBinder(BinderContext binderContext) {
    super(binderContext);
    this.propertyBinder = PropertyBinder.create(binderContext);
  }

  static EntityPropertyBinder create(BinderContext binderContext) {
    return new EntityPropertyBinder(binderContext);
  }

  Property bind(
      String propertyName,
      boolean mutable,
      Table table,
      ForeignKey fk,
      ToOne value,
      boolean inverseProperty) {
    AssociationInfo associationInfo = determineAssociationInfo(fk, inverseProperty, mutable);
    BinderUtils.updateFetchMode(value, associationInfo.getFetch());
    return propertyBinder.bind(table, propertyName, value, associationInfo);
  }

  private AssociationInfo determineAssociationInfo(
      ForeignKey foreignKey, boolean inverseProperty, boolean mutable) {
    AssociationInfo origin =
        BinderUtils.getAssociationInfo(getRevengStrategy(), foreignKey, inverseProperty);
    if (origin != null) {
      return RevengUtils.createAssociationInfo(
          origin.getCascade(),
          origin.getFetch(),
          origin.getInsert() != null ? origin.getInsert() : mutable,
          origin.getUpdate() != null ? origin.getUpdate() : mutable);
    } else {
      return RevengUtils.createAssociationInfo(null, null, mutable, mutable);
    }
  }
}
