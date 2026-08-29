package strategy;

import models.Ticket;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.ZoneId;

// Strategy + Decorator flavour: wraps a base FeeStrategy and applies a weekend
// multiplier on top. The literal "new rule = new class, wrapping the old one" demo —
// no edits to the base strategy or the lot (Open/Closed).
public class WeekendFeeStrategy implements FeeStrategy {
  private final FeeStrategy base;
  private final BigDecimal weekendMultiplier;

  public WeekendFeeStrategy(FeeStrategy base, BigDecimal weekendMultiplier) {
    this.base = base;
    this.weekendMultiplier = weekendMultiplier;
  }

  @Override
  public BigDecimal calculateFee(Ticket ticket) {
    BigDecimal baseFee = base.calculateFee(ticket);

    DayOfWeek day = ticket.getEntryTime().atZone(ZoneId.systemDefault()).getDayOfWeek();
    boolean isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;

    return isWeekend ? baseFee.multiply(weekendMultiplier) : baseFee;
  }
}
