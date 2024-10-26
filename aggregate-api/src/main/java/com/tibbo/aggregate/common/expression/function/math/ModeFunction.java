package com.tibbo.aggregate.common.expression.function.math;

import org.apache.commons.math3.stat.StatUtils;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.datatable.DataRecord;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.FieldFormat;
import com.tibbo.aggregate.common.expression.EvaluationException;
import com.tibbo.aggregate.common.expression.Function;
import com.tibbo.aggregate.common.expression.function.AbstractSingleValueCollectorFunction;

public class ModeFunction extends AbstractSingleValueCollectorFunction
{
  public ModeFunction()
  {
    super(null, "mode", Function.GROUP_MATH, "DataTable table [, String field]", "Double", Cres.get().getString("fDescMode"));
  }

  @Override
  protected Object collect(DataTable table, FieldFormat ff) throws EvaluationException
  {
    String field = ff.getName();
    double[] data = new double[table.getRecordCount()];
    int i = 0;

    for (DataRecord rec : table)
    {
      data[i++] = ((Number) rec.getValue(field)).doubleValue();
    }
    return StatUtils.mode(data)[0];
  }
}
