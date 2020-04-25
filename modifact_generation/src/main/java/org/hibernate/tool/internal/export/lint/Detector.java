package org.hibernate.tool.internal.export.lint;

import org.hibernate.boot.Metadata;

public abstract class Detector {

  private Metadata metadata;

  public void initialize(Metadata metadata) {
    this.metadata = metadata;
  }

  protected Metadata getMetadata() {
    return metadata;
  }

  public abstract void visit(IssueCollector collector);

  public abstract String getName();
}
