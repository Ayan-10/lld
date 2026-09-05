package models;

import enums.Direction;
import enums.RequestType;

public class HallRequest extends Request{

  private final Direction direction;

  public HallRequest(int floor, RequestType requestType,  Direction direction) {
    super(floor, requestType);
    this.direction = direction;
  }

  public Direction getDirection() {
    return direction;
  }
}
