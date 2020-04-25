package io.github.anticipasean.girakkagraph.protocol.model.domain.index;

/**
 * * Interface for types of components in the model in question based on a database schema, graphql
 * schema, and/or other types of schemas Each subtype is intended to be used as a way of referencing
 * different parts of a model quickly and easily through graphs or sub-type thereof e.g. a tree with
 * vertices, edges, and paths
 */
interface ModelIndex {

  ModelPath path();
}
