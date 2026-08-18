package strategy;

import enums.VehicleType;
import models.Ticket;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

public class HourlyFeeStrategy implements FeeStrategy {
  private static final Map<VehicleType, BigDecimal> HOURLY_RATE = Map.of(
          VehicleType.MOTORCYCLE, new BigDecimal("10"),
          VehicleType.CAR,        new BigDecimal("20"),
          VehicleType.TRUCK,      new BigDecimal("40"));

  @Override
  public BigDecimal calculateFee(Ticket ticket) {
    long minutes = Duration.between(ticket.getEntryTime(), ticket.getExitTime()).toMinutes();
    long hours = (long) Math.ceil(minutes / 60.0);           // round up a partial hour
    BigDecimal rate = HOURLY_RATE.get(ticket.getVehicle().getVehicleType());
    return rate.multiply(BigDecimal.valueOf(Math.max(1, hours))); // minimum 1 hour
  }
}
