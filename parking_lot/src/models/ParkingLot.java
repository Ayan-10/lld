package models;

import enums.ParkingSpotType;
import enums.PaymentMethod;
import enums.PaymentStatus;
import exceptions.InvalidTicketException;
import exceptions.LotFullException;
import observers.SpotObserver;
import payment.PaymentProcessor;
import strategy.FeeStrategy;
import strategy.SpotAssignmentStrategy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class ParkingLot {
  private static ParkingLot instance;

  private final List<ParkingFloor> floors;
  private final List<SpotObserver> observers;
  private SpotAssignmentStrategy assignmentStrategy;
  private final Map<String, Ticket> activeTickets;
  private FeeStrategy feeStrategy;
  private PaymentProcessor paymentProcessor;

  private ParkingLot() {
    this.floors = new ArrayList<>();
    this.observers = new ArrayList<>();
    this.activeTickets = new HashMap<>();
  }

  public static ParkingLot getInstance() {
    if (instance == null) {
      instance = new ParkingLot();
    }
    return instance;
  }

  public void addFloor(ParkingFloor floor) {
    floors.add(floor);
  }

  public void setAssignmentStrategy(SpotAssignmentStrategy strategy) {
    this.assignmentStrategy = strategy;
  }

  public void setFeeStrategy(FeeStrategy strategy) {
    this.feeStrategy = strategy;
  }

  public void setPaymentProcessor(PaymentProcessor processor) {
    this.paymentProcessor = processor;
  }

  public Ticket parkVehicle(Vehicle vehicle) throws LotFullException {
    Optional<ParkingSpot> parkingSpotOpt = assignmentStrategy.findSpot(floors, vehicle.vehicleType);
    ParkingSpot parkingSpot = parkingSpotOpt.orElse(null);
    // FIX #1: no spot must THROW LotFullException, not silently return null
    // (a null ticket propagates and breaks every downstream caller).
    if (parkingSpot == null) {
      throw new LotFullException("lot full");
    }

    boolean assigned = parkingSpot.assignSpot(vehicle);

    if (assigned) {
      // FIX #2: keep the owning floor's live free-count in sync on park.
      decrementFloorCount(parkingSpot);
      // FIX #1: record the ticket in activeTickets via createTicket so unpark can
      // later validate it (previously `new Ticket(...)` bypassed the registry,
      // making every unparkVehicle fail validation).
      return createTicket(vehicle, parkingSpot);
    } else {
      throw new LotFullException("lot full");
    }
  }

  // FIX #9: accept the PaymentMethod chosen at the exit gate instead of hardcoding CARD.
  public Receipt unparkVehicle(Ticket ticket, PaymentMethod method) throws InvalidTicketException {
    // 1. Validate ticket
    validateTicket(ticket);
    // 2. stamp exitTime
    Instant exitTime = Instant.now();
    ticket.setExitTime(exitTime);

    // 3. Calculate fee
    BigDecimal fee = feeStrategy.calculateFee(ticket);

    // 4. Process payment
    // FIX #9: use the caller-supplied method (was the hardcoded PaymentMethod.CARD).
    Payment payment =
            paymentProcessor.pay(
                    fee,
                    method
            );

    if (payment.getStatus() != PaymentStatus.SUCCESS) {
      throw new IllegalStateException(
              "Payment failed for ticket: " + ticket.getId()
      );
    }

    // 5. Free parking spot
    ParkingSpot spot = ticket.getParkingSpot();
    spot.freeSpot();
    // FIX #2: mirror the park-time decrement — put the freed spot back into the count.
    incrementFloorCount(spot);

    // 6. Mark ticket paid
    ticket.markPaid(true);

    // FIX #8: the ticket is now closed — remove it from activeTickets so it can't be
    // reused and so getInstance state doesn't leak stale tickets.
    activeTickets.remove(ticket.getId());

    // 7. Create receipt
    Receipt receipt = new Receipt(
            ticket.getId(),
            fee,
            payment
    );

    notifyObservers(spot);

    return receipt;

  }

  public int getAvailableCount(ParkingSpotType type) {
    int total = 0;

    for (ParkingFloor floor : floors) {
      total += floor.getAvailableCount(type);
    }

    return total;
  }

  public void registerObserver(SpotObserver observer) {
    observers.add(observer);
  }

  private Ticket createTicket(Vehicle vehicle, ParkingSpot spot) {
    Ticket ticket = new Ticket(vehicle, spot, Instant.now());
    activeTickets.put(ticket.getId(), ticket);
    return ticket;
  }

  private void notifyObservers(ParkingSpot parkingSpot) {
    for (SpotObserver observer : observers) {
      // FIX #4: call the renamed single interface method (was onSpotChanged, which
      // no longer exists after collapsing SpotObserver to one callback).
      observer.onSpotStateChanged(parkingSpot);
    }
  }

  // FIX #2: locate the floor that owns this spot and adjust its live free-count.
  // Pass 1: plain lookups; Pass 2 will guard these under the same lock as the
  // spot-status change so count and status never diverge.
  private void decrementFloorCount(ParkingSpot spot) {
    for (ParkingFloor floor : floors) {
      if (floor.containsSpot(spot)) {
        floor.decrementAvailable(spot.getType());
        return;
      }
    }
  }

  private void incrementFloorCount(ParkingSpot spot) {
    for (ParkingFloor floor : floors) {
      if (floor.containsSpot(spot)) {
        floor.incrementAvailable(spot.getType());
        return;
      }
    }
  }

  private void validateTicket(Ticket ticket) throws InvalidTicketException {

    if (ticket == null) {
      throw new InvalidTicketException(
              "Ticket cannot be null"
      );
    }

    Ticket storedTicket = activeTickets.get(ticket.getId());

    if (storedTicket == ticket) {
      return;
    }

    throw new InvalidTicketException(
            "Invalid or unknown ticket: " + ticket.getId()
    );
  }

}
