package com.tibbo.aggregate.common.expression.function.math;

import org.apache.commons.math3.stat.descriptive.moment.Variance;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.expression.Function;
import com.tibbo.aggregate.common.expression.function.AbstractSingleValueCollectorFunction;

public class VarianceFunction extends AbstractSingleValueCollectorFunction
{
  public VarianceFunction()
  {
    super(new Variance(), "variance", Function.GROUP_MATH, "DataTable table [, String field]", "Double", Cres.get().getString("fDescDispersion"));
  }
}
