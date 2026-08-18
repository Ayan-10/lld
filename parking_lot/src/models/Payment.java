package models;

import enums.PaymentMethod;
import enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class Payment {
  private final String id;
  private final BigDecimal amount;
  private final PaymentMethod method;
  private final Instant paidAt;
  private PaymentStatus status;


  public Payment(String id, BigDecimal amount, PaymentMethod method, Instant paidAt) {
    this.id = id;
    this.amount = amount;
    this.method = method;
    this.paidAt = paidAt;
    this.status = PaymentStatus.PENDING;
  }

  public String getId() {
    return id;
  }
  public BigDecimal getAmount() {
    return amount;
  }

  public PaymentMethod getMethod() {
    return method;
  }

  public Instant getPaidAt() {
    return paidAt;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  // FIX #10: a PaymentProcessor needs a way to move a PENDING payment to a terminal
  // state; without a mutator the status was stuck at PENDING and unpark always failed
  // the SUCCESS check.
  public void markSuccess() {
    this.status = PaymentStatus.SUCCESS;
  }

  public void markFailed() {
    this.status = PaymentStatus.FAILED;
  }
}
