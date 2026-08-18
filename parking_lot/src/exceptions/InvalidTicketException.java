package exceptions;

// FIX #7: extend Exception (a proper checked exception), not raw Throwable.
public class InvalidTicketException extends Exception {
  public InvalidTicketException(String ticketCannotBeNull) {
    super(ticketCannotBeNull);
  }
}
