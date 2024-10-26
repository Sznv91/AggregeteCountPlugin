package com.tibbo.aggregate.common.util;

public class Pair<F, S> implements Cloneable
{
  private F first;
  private S second;
  
  public Pair(F first, S second)
  {
    super();
    this.first = first;
    this.second = second;
  }
  
  public F getFirst()
  {
    return first;
  }
  
  public void setFirst(F first)
  {
    this.first = first;
  }
  
  public S getSecond()
  {
    return second;
  }
  
  public void setSecond(S second)
  {
    this.second = second;
  }
  
  @Override
  public String toString()
  {
    return "[" + first + " & " + second + "]";
  }
  
  @Override
  public int hashCode()
  {
    int result = first != null ? first.hashCode() : 0;
    result = 31 * result + (second != null ? second.hashCode() : 0);
    return result;
  }
  
  @Override
  public boolean equals(Object o)
  {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    
    Pair<?, ?> pair = (Pair<?, ?>) o;
    
    if (first != null ? !first.equals(pair.first) : pair.first != null)
      return false;
    return second != null ? second.equals(pair.second) : pair.second == null;
  }
  
  @Override
  public Pair clone()
  {
    try
    {
      return (Pair) super.clone();
    }
    catch (CloneNotSupportedException ex)
    {
      throw new IllegalStateException(ex.getMessage(), ex);
    }
  }
}
