package io.github.anticipasean.girakkagraph.protocol.base.exception.lookup;

import io.github.anticipasean.girakkagraph.protocol.base.command.lookup.LookUpCommand;

public class MoreThanOneResultException extends LookUpException {
  private int numberOfResultsReturned;
  private LookUpCommand command;

  public MoreThanOneResultException(int numberOfResultsReturned, LookUpCommand command) {
    super(
        String.format(
            "the number of results exceeded 1 for command [ %s ]: total results count: %d",
            command, numberOfResultsReturned));
    this.numberOfResultsReturned = numberOfResultsReturned;
    this.command = command;
  }

  public MoreThanOneResultException(
      String message, int numberOfResultsReturned, LookUpCommand command) {
    super(message);
    this.numberOfResultsReturned = numberOfResultsReturned;
    this.command = command;
  }

  public MoreThanOneResultException(
      String message, Throwable cause, int numberOfResultsReturned, LookUpCommand command) {
    super(message, cause);
    this.numberOfResultsReturned = numberOfResultsReturned;
    this.command = command;
  }

  public MoreThanOneResultException(
      Throwable cause, int numberOfResultsReturned, LookUpCommand command) {
    super(cause);
    this.numberOfResultsReturned = numberOfResultsReturned;
    this.command = command;
  }

  public MoreThanOneResultException(
      String message,
      Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace,
      int numberOfResultsReturned,
      LookUpCommand command) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.numberOfResultsReturned = numberOfResultsReturned;
    this.command = command;
  }
}
