package io.github.anticipasean.girakkagraph.protocol.base.exception.lookup;

import io.github.anticipasean.girakkagraph.protocol.base.exception.ProtocolException;

public class LookUpException extends ProtocolException {
  public LookUpException() {}

  public LookUpException(String message) {
    super(message);
  }

  public LookUpException(String message, Throwable cause) {
    super(message, cause);
  }

  public LookUpException(Throwable cause) {
    super(cause);
  }

  public LookUpException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
