package org.hibernate.tool.internal.reveng.binder;

import org.hibernate.mapping.Column;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;

class SimpleValueBinder extends AbstractBinder {

  private SimpleValueBinder(BinderContext binderContext) {
    super(binderContext);
  }

  static SimpleValueBinder create(BinderContext binderContext) {
    return new SimpleValueBinder(binderContext);
  }

  SimpleValue bind(Table table, Column column, boolean generatedIdentifier) {
    SimpleValue value = new SimpleValue(getMetadataBuildingContext(), table);
    value.addColumn(column);
    value.setTypeName(
        TypeUtils.determinePreferredType(
            getMetadataCollector(), getRevengStrategy(), table, column, generatedIdentifier));
    if (generatedIdentifier) {
      value.setNullValue("undefined");
    }
    return value;
  }
}
