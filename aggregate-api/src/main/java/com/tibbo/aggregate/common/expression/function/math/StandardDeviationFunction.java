package com.tibbo.aggregate.common.expression.function.math;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.expression.Function;
import com.tibbo.aggregate.common.expression.function.AbstractSingleValueCollectorFunction;

public class StandardDeviationFunction extends AbstractSingleValueCollectorFunction
{
  public StandardDeviationFunction()
  {
    super(new StandardDeviation(), "standardDeviation", Function.GROUP_MATH, "DataTable table [, String field]", "Double", Cres.get().getString("fDescStandardDeviation"));
  }
}
