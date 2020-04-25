package org.hibernate.tool.internal.export.lint;

public interface IssueCollector {

  void reportIssue(Issue analyze);
}
