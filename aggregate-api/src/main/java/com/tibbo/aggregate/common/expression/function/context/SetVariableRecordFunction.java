package com.tibbo.aggregate.common.expression.function.context;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.context.CallerController;
import com.tibbo.aggregate.common.context.Context;
import com.tibbo.aggregate.common.context.ContextException;
import com.tibbo.aggregate.common.context.VariableDefinition;
import com.tibbo.aggregate.common.expression.EvaluationEnvironment;
import com.tibbo.aggregate.common.expression.EvaluationException;
import com.tibbo.aggregate.common.expression.Evaluator;
import com.tibbo.aggregate.common.expression.Function;
import com.tibbo.aggregate.common.expression.function.AbstractFunction;
import com.tibbo.aggregate.common.util.Pair;
import com.tibbo.aggregate.common.util.Util;

public class SetVariableRecordFunction extends AbstractFunction
{
  public SetVariableRecordFunction()
  {
    this("setVariableRecord","String context, String variable, Integer record, Object parameter1, Object parameter2, ...", Cres.get().getString("fDescSetVariableRecord"));
  }

  public SetVariableRecordFunction(String name, String parametersFootprint, String description)
  {
    super(name, Function.GROUP_CONTEXT_RELATED, parametersFootprint, "Null", description);
  }

  @Override
  public Object execute(Evaluator evaluator, EvaluationEnvironment environment, Object... parameters) throws EvaluationException
  {
    checkParameters(4, true, parameters);

    String contextPath = parameters[0].toString();
    Pair<Context, CallerController> contextAndCaller = resolveContext(parameters, contextPath, evaluator);
    Context<?> context = contextAndCaller.getFirst();
    CallerController caller = contextAndCaller.getSecond();

    try
    {
      VariableDefinition vd = context.getVariableDefinition(parameters[1].toString(), caller);

      if (vd == null)
      {
        throw new ContextException(MessageFormat.format(Cres.get().getString("conVarNotAvailExt"), parameters[1].toString(), context.getPath()));
      }

      List<Object> input = getValueList(parameters);

      int row = Util.convertToNumber(parameters[2], true, false).intValue();

      for (int i = 0; i < vd.getFormat().getFieldCount() && i < input.size(); i++)
      {
        context.setVariableField(parameters[1].toString(), vd.getFormat().getFieldName(i), row, input.get(i), caller);
      }

      return null;
    }
    catch (Exception ex)
    {
      throw new EvaluationException(ex);
    }
  }

  protected List<Object> getValueList(Object[] parameters) throws EvaluationException {
    return Arrays.asList(Arrays.copyOfRange(parameters, 3, parameters.length));
  }

  protected Pair<Context, CallerController> resolveContext(Object[] parameters, String contextPath, Evaluator evaluator) throws EvaluationException
  {
    return resolveContext(contextPath, evaluator);
  }
}
