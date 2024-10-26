package com.tibbo.aggregate.common.expression.function;

import javax.annotation.Nullable;

import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.FieldFormat;
import com.tibbo.aggregate.common.expression.EvaluationEnvironment;
import com.tibbo.aggregate.common.expression.EvaluationException;
import com.tibbo.aggregate.common.expression.Evaluator;
import org.apache.commons.math3.stat.descriptive.AbstractStorelessUnivariateStatistic;

public abstract class AbstractSingleValueCollectorFunction extends AbstractFunction
{
  protected final AbstractStorelessUnivariateStatistic collector;

  /**
   * Note: If {@code collector} is null, developer should override {@code collect(DataTable table, FieldFormat ff)},
   * otherwise current implementation will throw {@code EvaluationException}.
   *
   * @see com.tibbo.aggregate.common.expression.function.math.ModeFunction
   */
  public AbstractSingleValueCollectorFunction(@Nullable AbstractStorelessUnivariateStatistic collector, String name, String category, String parametersFootprint, String returnValue, String description)
  {
    super(name, category, parametersFootprint, returnValue, description);
    this.collector = collector;
  }

  @Override
  public Object execute(Evaluator evaluator, EvaluationEnvironment environment, Object... parameters) throws EvaluationException
  {
    checkParameters(1, false, parameters);
    checkParameterType(0, parameters[0], DataTable.class);

    if (collector != null)
    {
      collector.clear();
    }

    DataTable table = (DataTable) parameters[0];
    if (table.getRecordCount() == 0)
    {
      return 0.0d;
    }

    FieldFormat ff = table.getFormat(0);
    if (parameters.length >= 2)
    {
      ff = checkAndGetNumberTypeField(table, parameters[1]);
    }
    else
    {
      checkNumberTypeField(ff);
    }

    return collect(table, ff);
  }

  protected Object collect(DataTable table, FieldFormat ff) throws EvaluationException
  {
    if (collector == null)
    {
      throw new EvaluationException("No collector is set");
    }

    String fieldName = ff.getName();
    table.forEach(rec -> {
      Object value = rec.getValue(fieldName);
      if (value != null) {
        collector.increment(((Number) value).doubleValue());
      }
    });

    return collector.getResult();
  }
}
