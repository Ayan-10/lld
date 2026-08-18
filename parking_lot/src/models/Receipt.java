package models;

import java.math.BigDecimal;
import java.time.Instant;

public class Receipt {
  private final String ticketId;
  private final BigDecimal amount;

  public String getTicketId() {
    return ticketId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Payment getPayment() {
    return payment;
  }

  public Instant getIssuedAt() {
    return issuedAt;
  }

  private final Payment payment;
  private final Instant issuedAt;

  public Receipt(String ticketId, BigDecimal amount, Payment payment) {
    this.ticketId = ticketId;
    this.amount = amount;
    this.payment = payment;
    this.issuedAt = Instant.now();
  }
}
