import enums.ParkingSpotType;
import enums.PaymentMethod;
import enums.VehicleType;
import exceptions.InvalidTicketException;
import models.ParkingFloor;
import models.ParkingLot;
import models.ParkingSpot;
import models.Receipt;
import models.Ticket;
import observers.SpotObserver;
import payment.CardPaymentProcessor;
import strategy.HourlyFeeStrategy;
import strategy.NearestEntranceStrategy;
import utils.DisplayBoard;
import utils.EntryGate;
import utils.ExitGate;

import java.util.ArrayList;
import java.util.List;

// FIX #10: replaced the IntelliJ template with a runnable park -> availability ->
// unpark -> availability demo that wires the whole lot together end-to-end.
public class Main {
  public static void main(String[] args) throws InvalidTicketException {
    // 1. Build the lot (Singleton) and plug in the strategies + payment gateway.
    ParkingLot lot = ParkingLot.getInstance();
    lot.setAssignmentStrategy(new NearestEntranceStrategy());
    lot.setFeeStrategy(new HourlyFeeStrategy());
    lot.setPaymentProcessor(new CardPaymentProcessor());

    // 2. The display board is a SpotObserver reading the lot's authoritative counts.
    DisplayBoard board = new DisplayBoard(lot);
    lot.registerObserver(board);

    // 3. One floor with a small mix of spots. Each spot shares the observer list so
    //    state changes fan out to the board.
    List<SpotObserver> observers = new ArrayList<>();
    observers.add(board);

    // ⬆ HARDEN (Pass 2): the floor now creates each spot so it can inject the shared
    // AtomicInteger counter (the spot moves it in lockstep inside its own lock).
    ParkingFloor floor = new ParkingFloor(1);
    floor.createSpot("M1", ParkingSpotType.MOTORCYCLE, observers);
    floor.createSpot("C1", ParkingSpotType.COMPACT, observers);
    floor.createSpot("C2", ParkingSpotType.COMPACT, observers);
    floor.createSpot("L1", ParkingSpotType.LARGE, observers);
    lot.addFloor(floor);

    // 4. Gates sit in front of the lot.
    EntryGate entry = new EntryGate(lot);
    ExitGate exit = new ExitGate(lot);

    System.out.println("--- Initial availability ---");
    board.show();

    // 5. Park a car -> a COMPACT spot is taken, board updates via the observer.
    System.out.println("\n--- Parking a CAR ---");
    Ticket ticket = entry.admit(VehicleType.CAR, "KA-01-1234");
    System.out.println("Issued ticket: " + ticket.getId()
            + " for " + ticket.getVehicle().getLicensePlate());

    // 6. Unpark -> fee charged via CARD, spot freed, board updates again.
    System.out.println("\n--- Unparking (pay by CARD) ---");
    Receipt receipt = exit.processExit(ticket, PaymentMethod.CARD);
    System.out.println("Receipt " + receipt.getTicketId()
            + " amount=" + receipt.getAmount()
            + " status=" + receipt.getPayment().getStatus());

    System.out.println("\n--- Final availability ---");
    board.show();
  }
}
