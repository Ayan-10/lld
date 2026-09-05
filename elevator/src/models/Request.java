package models;

import enums.RequestType;

abstract class Request {
  protected final int floor;
  protected final RequestType requestType;
  protected final long timestamp;

  public Request(int floor, RequestType requestType) {
    this.floor = floor;
    this.requestType = requestType;
    this.timestamp = System.currentTimeMillis();
  }

  public int getFloor() {
    return floor;
  }

  public RequestType getRequestType() {
    return requestType;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
