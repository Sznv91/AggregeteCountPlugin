package com.tibbo.aggregate.common.expression.function.context;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.expression.EvaluationEnvironment;
import com.tibbo.aggregate.common.expression.EvaluationException;
import com.tibbo.aggregate.common.expression.Evaluator;
import com.tibbo.aggregate.common.expression.Function;
import com.tibbo.aggregate.common.expression.function.AbstractFunction;

public class DtFunction extends AbstractFunction
{
  public DtFunction()
  {
    super("dt", Function.GROUP_CONTEXT_RELATED, "", "DataTable", Cres.get().getString("fDescDt"));
  }
  
  @Override
  public Object execute(Evaluator evaluator, EvaluationEnvironment environment, Object... parameters) throws EvaluationException
  {
    return evaluator.getDefaultResolver().getDefaultTable();
  }
}