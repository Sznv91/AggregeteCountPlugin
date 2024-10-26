package com.tibbo.aggregate.common.expression.function.table;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.DataTableUtils;
import com.tibbo.aggregate.common.expression.EvaluationEnvironment;
import com.tibbo.aggregate.common.expression.EvaluationException;
import com.tibbo.aggregate.common.expression.Evaluator;
import com.tibbo.aggregate.common.expression.Function;
import com.tibbo.aggregate.common.expression.function.AbstractFunction;

public class ProcessBindingsFunction extends AbstractFunction
{
  
  public ProcessBindingsFunction()
  {
    super("processBindings", Function.GROUP_DATA_TABLE_PROCESSING, "DataTable table", "DataTable", Cres.get().getString("fDescProcessBindings"));
  }
  
  @Override
  public Object execute(Evaluator evaluator, EvaluationEnvironment environment, Object... parameters) throws EvaluationException
  {
    checkParameters(1, false, parameters);
    checkParameterType(0, parameters[0], DataTable.class);
    DataTable source = (DataTable) parameters[0];
    DataTableUtils.processBindings(source, evaluator);
    return source;
  }
}