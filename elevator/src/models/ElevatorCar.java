package models;

import enums.Direction;
import state.ElevatorState;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

public class ElevatorCar {
  private final int id;
  private final Door door;
  private final NavigableSet<Integer> upRequests;
  private final NavigableSet<Integer> downRequests;
  private final int maxFloor;
  private final int minFloor;
  private final int maxLoad;
  private final List<Display> displays;
  private int currentLoad;
  private int currentFloor;
  private Direction direction;
  private ElevatorState elevatorState;


  public ElevatorCar(int id, int maxFloor, int minFloor, int maxLoad) {
    this.id = id;
    this.maxFloor = maxFloor;
    this.minFloor = minFloor;
    this.maxLoad = maxLoad;
    this.door = new Door();
    this.upRequests = new TreeSet<>();
    this.downRequests = new TreeSet<>();
    this.displays = new ArrayList<>();
  }

  public void setElevatorState(ElevatorState elevatorState) {
    this.elevatorState = elevatorState;
  }

  public void addRequest(Request request){

    if(request instanceof HallRequest hallRequest){
      if (hallRequest.getDirection() == Direction.UP){
        upRequests.add(hallRequest.getFloor());
      } else {
        downRequests.add(hallRequest.getFloor());
      }
    } else if (request instanceof  CarRequest carRequest){
      if(currentFloor > carRequest.getFloor()){
        downRequests.add(carRequest.getFloor());
      } else if (currentFloor < carRequest.getFloor()){
        upRequests.add(carRequest.getFloor());
      } else {
        openDoors();
      }
    }
  }

  public void removeStop(int floor){
    upRequests.remove(floor);
    downRequests.remove(floor);
  }

  public void stepUpOneFloor(){
    currentFloor++;
    notifyDisplay();
  }

  public void stepDownOneFloor(){
    currentFloor--;
    notifyDisplay();
  }

  public Integer getNextStop() {
    if(direction == Direction.UP){
      Integer nextFloor = upRequests.ceiling(currentFloor);
      if (nextFloor != null) {
        return nextFloor;
      }

      direction = (downRequests.isEmpty()) ? Direction.IDLE : Direction.DOWN;

      return (downRequests.isEmpty()) ? null : downRequests.floor(currentFloor);
    } else if (direction == Direction.DOWN){
      Integer nextFloor = downRequests.floor(currentFloor);

      if (nextFloor != null) {
        return nextFloor;
      }

      direction = (upRequests.isEmpty()) ? Direction.IDLE : Direction.UP;
      return (upRequests.isEmpty()) ? null : upRequests.ceiling(currentFloor);
    } else {
      if (!upRequests.isEmpty()) {
        direction = Direction.UP;
        return upRequests.ceiling(currentFloor);
      }
      if (!downRequests.isEmpty()) {
        direction = Direction.DOWN;
        return downRequests.floor(currentFloor);
      }
      return null;
    }
  }

  public void pressFloor(int destination){
    elevatorState.pressFloor(this, destination);
  }

  public void step(){
    elevatorState.move(this);
  }



  public int getCurrentFloor() {
    return currentFloor;
  }
}
