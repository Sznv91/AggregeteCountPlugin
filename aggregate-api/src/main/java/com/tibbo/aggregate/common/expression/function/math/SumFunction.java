package com.tibbo.aggregate.common.expression.function.math;

import java.text.MessageFormat;

import org.apache.commons.math3.stat.descriptive.summary.Sum;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.datatable.DataRecord;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.FieldFormat;
import com.tibbo.aggregate.common.expression.EvaluationEnvironment;
import com.tibbo.aggregate.common.expression.EvaluationException;
import com.tibbo.aggregate.common.expression.Evaluator;
import com.tibbo.aggregate.common.expression.Function;
import com.tibbo.aggregate.common.expression.function.AbstractSingleValueCollectorFunction;

public class SumFunction extends AbstractSingleValueCollectorFunction
{
  
  public SumFunction()
  {
    super(new Sum(), "sum", Function.GROUP_MATH, "DataTable table [, String field]", "Double", Cres.get().getString("fDescSum"));
  }
}
