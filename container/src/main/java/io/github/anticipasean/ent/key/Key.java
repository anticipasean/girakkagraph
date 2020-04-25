package io.github.anticipasean.ent.key;

public interface Key<ID> {

    ID id();

    static <ID> Key<ID> of(ID id){
        return new Key<ID>() {
            @Override
            public ID id() {
                return id;
            }
        };
    }

}
