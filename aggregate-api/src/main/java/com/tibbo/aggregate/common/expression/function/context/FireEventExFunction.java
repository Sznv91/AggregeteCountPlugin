package com.tibbo.aggregate.common.expression.function.context;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.context.CallerController;
import com.tibbo.aggregate.common.context.Context;
import com.tibbo.aggregate.common.expression.EvaluationException;
import com.tibbo.aggregate.common.expression.Evaluator;
import com.tibbo.aggregate.common.util.Pair;

public class FireEventExFunction extends FireEventFunction
{
  public FireEventExFunction()
  {
    super("fireEventEx", "String context, String event, Integer level, DataTable value, [, String schema]", Cres.get().getString("fDescFireEventEx"));
  }

  @Override
  protected Pair<Context, CallerController> resolveContext(Object[] parameters, String contextPath, Evaluator evaluator) throws EvaluationException
  {
    return parameters.length > 4 ? resolveContext(contextPath, (String) parameters[4], evaluator) : super.resolveContext(parameters, contextPath, evaluator);
  }
}
