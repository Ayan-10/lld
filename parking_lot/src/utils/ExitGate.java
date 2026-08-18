package utils;

import enums.PaymentMethod;
import exceptions.InvalidTicketException;
import models.ParkingLot;
import models.Receipt;
import models.Ticket;

public class ExitGate {

  private final ParkingLot lot;

  public ExitGate(ParkingLot lot) {
    this.lot = lot;
  }

  public Receipt processExit(
          Ticket ticket,
          PaymentMethod method) throws InvalidTicketException {

    // FIX #9: forward the chosen payment method to the lot instead of dropping it.
    return lot.unparkVehicle(ticket, method);
  }
}
