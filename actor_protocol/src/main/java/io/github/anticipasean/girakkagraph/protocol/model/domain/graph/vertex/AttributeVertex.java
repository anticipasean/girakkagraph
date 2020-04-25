package io.github.anticipasean.girakkagraph.protocol.model.domain.graph.vertex;

import io.github.anticipasean.girakkagraph.protocol.model.domain.graph.ModelVertex;
import io.github.anticipasean.girakkagraph.protocol.model.domain.index.attribute.PersistableGraphQLAttribute;

public interface AttributeVertex extends ModelVertex {

  //  default <T, A> Bindable<A> bindableType() {
  //    @SuppressWarnings("unchecked")
  //    Attribute<T, A> attribute = (Attribute<T, A>) persistableGraphQlAttribute().jpaAttribute();
  //    if (attribute instanceof SingularAttribute) {
  //      @SuppressWarnings("unchecked")
  //      SingularAttribute<T, A> singularAttribute = (SingularAttribute<T, A>) attribute;
  //      return singularAttribute;
  //    } else if (attribute instanceof PluralAttribute) {
  //      @SuppressWarnings("unchecked")
  //      PluralAttribute<T, ?, A> pluralAttribute = (PluralAttribute<T, ?, A>) attribute;
  //      return pluralAttribute;
  //    } else {
  //      throw new IllegalStateException("attribute is neither singular nor plural jpa attribute");
  //    }
  //  }

  PersistableGraphQLAttribute persistableGraphQlAttribute();
}
