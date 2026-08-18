package exceptions;

// FIX #7: extend Exception (a proper checked exception), not raw Throwable — Throwable
// also covers Errors and is not the intended base for recoverable application failures.
public class LotFullException extends Exception {
  public LotFullException(String ticketCannotBeNull) {
    super(ticketCannotBeNull);
  }
}
