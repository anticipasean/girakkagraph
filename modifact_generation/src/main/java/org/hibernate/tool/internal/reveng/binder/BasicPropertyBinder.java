package org.hibernate.tool.internal.reveng.binder;

import org.hibernate.mapping.Column;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.hibernate.tool.internal.reveng.util.RevengUtils;

class BasicPropertyBinder extends AbstractBinder {

  private final SimpleValueBinder simpleValueBinder;
  private final PropertyBinder propertyBinder;

  private BasicPropertyBinder(BinderContext binderContext) {
    super(binderContext);
    simpleValueBinder = SimpleValueBinder.create(binderContext);
    propertyBinder = PropertyBinder.create(binderContext);
  }

  static BasicPropertyBinder create(BinderContext binderContext) {
    return new BasicPropertyBinder(binderContext);
  }

  Property bind(String propertyName, Table table, Column column) {
    return propertyBinder.bind(
        table,
        propertyName,
        simpleValueBinder.bind(table, column, false),
        RevengUtils.createAssociationInfo(null, null, true, true));
  }
}
