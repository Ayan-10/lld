package strategy;

import models.Ticket;

import java.math.BigDecimal;

public interface FeeStrategy {
  BigDecimal calculateFee(Ticket ticket);
}
