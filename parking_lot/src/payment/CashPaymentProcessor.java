package payment;

import enums.PaymentMethod;
import enums.PaymentStatus;
import models.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Strategy (pluggable payment backend): a cash payment settles immediately as SUCCESS.
// A new provider = a new class implementing PaymentProcessor -> Open/Closed.
public class CashPaymentProcessor implements PaymentProcessor {

  @Override
  public Payment pay(BigDecimal fee, PaymentMethod paymentMethod) {
    Payment payment = new Payment(
            UUID.randomUUID().toString(),
            fee,
            paymentMethod,
            Instant.now()
    );
    payment.markSuccess(); // cash handed over at the gate -> immediately settled
    return payment;
  }
}
