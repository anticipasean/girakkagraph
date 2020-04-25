package io.github.anticipasean.girakkagraph.protocol.base.util.valuesupplier;

import java.util.function.Supplier;

/**
 * Interface for interfaces that act as a container for some third-party api interfaces that:
 *
 * <ul>
 *   <li>may be subject to lots of change from release to release
 *   <li>may have multiple generic parameters, which will force type checking, upcasting,
 *       downcasting, and other logic to be duplicated elsewhere
 *   <li>may require interface or implementation specific serialization techniques for "held"
 *       components down-the-line if this project evolves in the distributed architecture direction
 * </ul>
 *
 * Using this interface, the classes for which the value is a field:
 *
 * <ul>
 *   <li>no longer need to have additional generic type parameters declared, which complicate the
 *       interfaces and methods that interact with these classes
 *   <li>through default methods on value supplier interface, can be more focused on their purpose
 *       since methods added just for dealing or extracting information from the supplied value can
 *       remain on the value supplier interface
 * </ul>
 *
 * The use of the {@link Supplier} interface means that clients can create instances of these
 * ValueSuppliers without the need for static factory methods or constructors eg. static factory
 * method {@code MyClass.builder().value(MyContainer.<T,U,V>getInstance(myMap)).build(); }. Merely,
 * {@code () -> V } is sufficient: {@code MyClass.builder().value(() -> myMap).build(); }
 *
 * <p>Extensions of this interface must be careful to use static methods, default methods (on
 * interface types), or concretely implemented methods (on class types). Adding any abstract methods
 * may break the functional interface aspect of the descendent disabling the ability to use the
 * special syntax {@code () -> V } when generating a reference to an object of the descendent since
 * functional interfaces may have only one abstract method.
 */
public interface ValueSupplier<V> extends Supplier<V> {}
