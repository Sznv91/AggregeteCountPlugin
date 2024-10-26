package com.tibbo.aggregate.common.data;

public class Location
{
  private Double latitude;
  private Double longitude;
  
  public Location(Double latitude, Double longitude)
  {
    this.latitude = latitude;
    this.longitude = longitude;
  }
  
  public Location(double latitude, double longitude)
  {
    this.latitude = new Double(latitude);
    this.longitude = new Double(longitude);
  }
  
  public Double getLatitude()
  {
    return latitude;
  }
  
  public void setLatitude(Double latitude)
  {
    this.latitude = latitude;
  }
  
  public Double getLongitude()
  {
    return longitude;
  }
  
  public void setLongitude(Double longitude)
  {
    this.longitude = longitude;
  }
  
  @Override
  public String toString()
  {
    return "[Lat=" + latitude + ", Lon=" + longitude + "]";
  }
}
