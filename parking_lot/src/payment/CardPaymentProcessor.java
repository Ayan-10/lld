package payment;

import enums.PaymentMethod;
import models.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// FIX #10: a concrete PaymentProcessor so the lot can actually be wired and run.
// This is a stub gateway: it always approves. In a real system this would call an
// external PSP; here it just mints a SUCCESS Payment so the end-to-end flow is testable.
public class CardPaymentProcessor implements PaymentProcessor {

  @Override
  public Payment pay(BigDecimal fee, PaymentMethod paymentMethod) {
    Payment payment = new Payment(
            UUID.randomUUID().toString(),
            fee,
            paymentMethod,
            Instant.now()
    );
    payment.markSuccess(); // FIX #10: approve so unparkVehicle's SUCCESS check passes
    return payment;
  }
}
