package com.tibbo.aggregate.common.expression.function.math;

import org.apache.commons.math3.stat.descriptive.moment.Mean;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.expression.Function;
import com.tibbo.aggregate.common.expression.function.AbstractSingleValueCollectorFunction;

public class AverageFunction extends AbstractSingleValueCollectorFunction
{
  public AverageFunction()
  {
    super(new Mean(), "avg", Function.GROUP_MATH, "DataTable table [, String field]", "Double", Cres.get().getString("fDescAverage"));
  }
}
