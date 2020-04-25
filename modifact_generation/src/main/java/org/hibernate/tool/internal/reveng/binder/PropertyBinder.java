package org.hibernate.tool.internal.reveng.binder;

import java.util.Iterator;
import java.util.Map;
import org.hibernate.FetchMode;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Fetchable;
import org.hibernate.mapping.MetaAttribute;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.Value;
import org.hibernate.tool.api.reveng.AssociationInfo;
import org.hibernate.tool.api.reveng.TableIdentifier;
import org.hibernate.tool.internal.reveng.util.RevengUtils;

class PropertyBinder extends AbstractBinder {

  private PropertyBinder(BinderContext binderContext) {
    super(binderContext);
  }

  static PropertyBinder create(BinderContext binderContext) {
    return new PropertyBinder(binderContext);
  }

  Property bind(Table table, String propertyName, Value value, AssociationInfo associationInfo) {
    return bindMetaAttributes(createProperty(propertyName, value, associationInfo), table);
  }

  private Property createProperty(
      String propertyName, Value value, AssociationInfo associationInfo) {
    Property result = new Property();
    result.setName(propertyName);
    result.setValue(value);
    result.setInsertable(associationInfo.getInsert());
    result.setUpdateable(associationInfo.getUpdate());
    String cascade = associationInfo.getCascade();
    cascade = cascade == null ? "none" : cascade;
    result.setCascade(cascade);
    boolean lazy = false;
    if (value instanceof Fetchable) {
      lazy = ((Fetchable) value).getFetchMode() != FetchMode.JOIN;
    }
    result.setLazy(lazy);
    result.setPropertyAccessorName("property");
    return result;
  }

  private Property bindMetaAttributes(Property property, Table table) {
    Iterator<Selectable> columnIterator = property.getValue().getColumnIterator();
    while (columnIterator.hasNext()) {
      Column col = (Column) columnIterator.next();
      Map<String, MetaAttribute> map =
          getColumnToMetaAttributesInRevengStrategy(table, col.getName());
      if (map != null) {
        property.setMetaAttributes(map);
      }
    }

    return property;
  }

  private Map<String, MetaAttribute> getColumnToMetaAttributesInRevengStrategy(
      Table table, String column) {
    Map<String, MetaAttribute> result = null;
    TableIdentifier tableIdentifier = TableIdentifier.create(table);
    result = getRevengStrategy().columnToMetaAttributes(tableIdentifier, column);
    if (result == null) {
      tableIdentifier =
          RevengUtils.createTableIdentifier(table, getDefaultCatalog(), getDefaultSchema());
      result = getRevengStrategy().columnToMetaAttributes(tableIdentifier, column);
    }
    return result;
  }
}
