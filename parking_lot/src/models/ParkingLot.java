package models;

import enums.ParkingSpotType;
import enums.PaymentMethod;
import enums.PaymentStatus;
import enums.VehicleType;
import exceptions.InvalidTicketException;
import exceptions.LotFullException;
import observers.SpotObserver;
import payment.PaymentProcessor;
import strategy.FeeStrategy;
import strategy.SpotAssignmentStrategy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ParkingLot {
  // ⬆ HARDEN (Pass 2, H4): volatile so the double-checked-locking below publishes a fully
  // constructed instance safely to other threads (no half-built object leaking).
  private static volatile ParkingLot instance;

  private final List<ParkingFloor> floors;
  private final List<SpotObserver> observers;
  private SpotAssignmentStrategy assignmentStrategy;
  // ⬆ HARDEN (Pass 2, H4): registry is now concurrent — multiple exit gates read/remove
  // tickets in parallel; a plain HashMap would corrupt under concurrent structural edits.
  private final Map<String, Ticket> activeTickets;
  private FeeStrategy feeStrategy;
  private PaymentProcessor paymentProcessor;

  private ParkingLot() {
    this.floors = new ArrayList<>();
    this.observers = new ArrayList<>();
    this.activeTickets = new ConcurrentHashMap<>();
  }

  // ⬆ HARDEN (Pass 2, H4): double-checked locking. Fast path skips the lock once the
  // instance exists; the synchronized block guards the first concurrent creation so two
  // threads can't build two lots. (Alternative worth naming in the room: drop the
  // hand-rolled Singleton for a Spring @Bean — easier to test.)
  public static ParkingLot getInstance() {
    if (instance == null) {
      synchronized (ParkingLot.class) {
        if (instance == null) {
          instance = new ParkingLot();
        }
      }
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

  // ⬆ HARDEN (Pass 2, H4): retry-next-candidate. tryAssign can now return false on a lost
  // race, so instead of failing we loop to the next candidate spot. The counter is moved
  // by the spot itself (inside its lock) — the orchestrator no longer touches it.
  public Ticket parkVehicle(Vehicle vehicle) throws LotFullException {
    VehicleType type = vehicle.getVehicleType();

    for (ParkingFloor floor : floors) {
      for (ParkingSpot spot : floor.candidateSpots(type)) {
        if (spot.tryAssign(vehicle)) {          // atomic; may lose the race -> false
          return createTicket(vehicle, spot);   // RECORD IT — unpark validates against this
        }
        // lost the race (or wrong fit) -> just try the next candidate spot
      }
    }

    // FIX #1: no spot anywhere must THROW, never silently return null.
    throw new LotFullException("No spot available for " + type);
  }

  // FIX #9: accept the PaymentMethod chosen at the exit gate instead of hardcoding CARD.
  public Receipt unparkVehicle(Ticket ticket, PaymentMethod method) throws InvalidTicketException {
    if (ticket == null) {
      throw new InvalidTicketException("Ticket cannot be null");
    }

    // ⬆ HARDEN (Pass 2, H4): exit idempotency FIRST. A double-scan at the gate must not
    // double-charge. Because a closed ticket is removed from activeTickets, we check the
    // paid flag BEFORE registry validation — otherwise the second scan would wrongly throw
    // InvalidTicket instead of being a harmless no-op.
    if (ticket.isPaid()) {
      return new Receipt(ticket.getId(), BigDecimal.ZERO, null);
    }

    // 1. Validate ticket (still open => must be a known, live ticket)
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

    // 5. Free parking spot (idempotent; increments the shared counter inside the spot lock).
    ParkingSpot spot = ticket.getParkingSpot();
    spot.freeSpot();

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
