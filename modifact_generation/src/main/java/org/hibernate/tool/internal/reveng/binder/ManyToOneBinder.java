package org.hibernate.tool.internal.reveng.binder;

import java.util.Iterator;
import java.util.Set;
import org.hibernate.FetchMode;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;

class ManyToOneBinder extends AbstractBinder {

  private final EntityPropertyBinder entityPropertyBinder;

  private ManyToOneBinder(BinderContext binderContext) {
    super(binderContext);
    this.entityPropertyBinder = EntityPropertyBinder.create(binderContext);
  }

  static ManyToOneBinder create(BinderContext binderContext) {
    return new ManyToOneBinder(binderContext);
  }

  Property bind(
      String propertyName,
      boolean mutable,
      Table table,
      ForeignKey fk,
      Set<Column> processedColumns) {
    ManyToOne value = new ManyToOne(getMetadataBuildingContext(), table);
    value.setReferencedEntityName(fk.getReferencedEntityName());
    addColumns(value, fk, processedColumns);
    value.setFetchMode(FetchMode.SELECT);
    return entityPropertyBinder.bind(propertyName, mutable, table, fk, value, false);
  }

  private void addColumns(ManyToOne value, ForeignKey fk, Set<Column> processedColumns) {
    Iterator<Column> columns = fk.getColumnIterator();
    while (columns.hasNext()) {
      Column fkcolumn = columns.next();
      BinderUtils.checkColumnForMultipleBinding(fkcolumn);
      value.addColumn(fkcolumn);
      processedColumns.add(fkcolumn);
    }
  }
}
