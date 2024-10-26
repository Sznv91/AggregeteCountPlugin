package com.tibbo.aggregate.common.expression.function.context;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.context.CallerController;
import com.tibbo.aggregate.common.context.Context;
import com.tibbo.aggregate.common.context.VariableDefinition;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.DataTableException;
import com.tibbo.aggregate.common.expression.EvaluationException;
import com.tibbo.aggregate.common.expression.Evaluator;
import com.tibbo.aggregate.common.util.Pair;
import com.tibbo.aggregate.common.util.SyntaxErrorException;

public class SetVariableExFunction extends SetVariableFunction
{
  private static final int MINIMAL_COUNT = 3;
  
  public SetVariableExFunction()
  {
    super("setVariableEx","String context, String variable, DataTable value, [, String schema]", Cres.get().getString("fDescSetVariableEx"), MINIMAL_COUNT);
  }
  
  @Override
  protected DataTable getValueTable(Evaluator evaluator, VariableDefinition vd, Object[] parameters) throws SyntaxErrorException, EvaluationException, DataTableException
  {
    if (parameters[2] instanceof DataTable)
    {
      return (DataTable) parameters[2];
    }
    else
    {
      throw new EvaluationException("Incorrect value: " + parameters[2]);
    }
  }

  @Override
  protected Pair<Context, CallerController> resolveContext(Object[] parameters, String contextPath, Evaluator evaluator) throws EvaluationException
  {
    return parameters.length > 3 ? resolveContext(contextPath, (String) parameters[3], evaluator) : super.resolveContext(parameters, contextPath, evaluator);
  }
}
