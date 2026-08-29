package strategy;

import models.Ticket;

import java.math.BigDecimal;

// Strategy (Open/Closed): a new pricing rule = a new class, ZERO edits to FeeStrategy
// or the lot. One flat charge regardless of how long the vehicle stayed.
public class FlatRateFeeStrategy implements FeeStrategy {
  private final BigDecimal flatRate;

  public FlatRateFeeStrategy(BigDecimal flatRate) {
    this.flatRate = flatRate;
  }

  @Override
  public BigDecimal calculateFee(Ticket ticket) {
    return flatRate;
  }
}
