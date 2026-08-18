package payment;

import enums.PaymentMethod;
import models.Payment;

import java.math.BigDecimal;

public interface PaymentProcessor {
  Payment pay(BigDecimal fee, PaymentMethod paymentMethod);
}
