package com.tibbo.aggregate.common.filter.converter;

public class BaseExpressionBuilder
{
  
  protected final StringBuilder builder = new StringBuilder();
  
  public BaseExpressionBuilder withLogicalOr()
  {
    withLogicalOr(builder);
    return this;
  }
  
  public BaseExpressionBuilder withLogicalAnd()
  {
    withLogicalAnd(builder);
    return this;
  }
  
  public BaseExpressionBuilder withExpressionByAnd(String expression)
  {
    if (expression.isEmpty())
    {
      return this;
    }
    withLogicalAnd();
    builder.append(wrap(expression));
    return this;
  }
  
  private String wrap(String expression)
  {
    return "(" + expression + ")";
  }
  
  public BaseExpressionBuilder withExpressionByOr(String expression)
  {
    if (expression.isEmpty())
    {
      return this;
    }
    withLogicalOr();
    builder.append(wrap(expression));
    return this;
  }
  
  public static void withLogicalAnd(StringBuilder sb)
  {
    if (sb.length() > 0)
    {
      sb.append(" && ");
    }
  }
  
  public static void withLogicalOr(StringBuilder sb)
  {
    if (sb.length() > 0)
    {
      sb.append(" || ");
    }
  }
  
  public String build()
  {
    return builder.toString();
  }
}
