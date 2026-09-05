package state;

import enums.ElevatorStateType;
import enums.RequestType;
import models.CarRequest;
import models.ElevatorCar;

public class IdleState implements ElevatorState{
  @Override
  public void move(ElevatorCar elevatorCar) {
    Integer nextStop = elevatorCar.getNextStop();

    if (nextStop != null) {
      if (nextStop == elevatorCar.getCurrentFloor()){
        elevatorCar.setElevatorState(new DoorOpenState());
      } else if (nextStop > elevatorCar.getCurrentFloor()) {
        elevatorCar.setElevatorState(new MovingUpState());
      } else {
        elevatorCar.setElevatorState(new MovingDownState());
      }
    }
  }

  @Override
  public void openDoor(ElevatorCar elevatorCar) {
    elevatorCar.setElevatorState(new DoorOpenState());
  }

  @Override
  public void pressFloor(ElevatorCar elevatorCar, int floor) {
    elevatorCar.addRequest(new CarRequest(floor, RequestType.CAR));

    if (floor > elevatorCar.getCurrentFloor()) {
      elevatorCar.setElevatorState(new MovingUpState());
    } else if (floor < elevatorCar.getCurrentFloor()) {
      elevatorCar.setElevatorState(new MovingDownState());
    } else {
      elevatorCar.setElevatorState(new DoorOpenState());
    }
  }

  @Override
  public ElevatorStateType getType() {
    return ElevatorStateType.IDLE;
  }
}
