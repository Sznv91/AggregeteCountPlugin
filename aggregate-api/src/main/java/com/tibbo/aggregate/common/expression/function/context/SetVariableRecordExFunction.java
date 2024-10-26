package com.tibbo.aggregate.common.expression.function.context;

import java.util.List;
import java.util.stream.Collectors;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.context.CallerController;
import com.tibbo.aggregate.common.context.Context;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.expression.EvaluationException;
import com.tibbo.aggregate.common.expression.Evaluator;
import com.tibbo.aggregate.common.util.Pair;

public class SetVariableRecordExFunction extends SetVariableRecordFunction
{
  public SetVariableRecordExFunction()
  {
    super("setVariableRecordEx", "String context, String variable, Integer record, DataTable value, [, String schema]", Cres.get().getString("fDescSetVariableRecordEx"));
  }
  
  @Override
  protected Pair<Context, CallerController> resolveContext(Object[] parameters, String contextPath, Evaluator evaluator) throws EvaluationException
  {
    return parameters.length > 4 ? resolveContext(contextPath, (String) parameters[4], evaluator) : super.resolveContext(parameters, contextPath, evaluator);
  }
  
  @Override
  protected List<Object> getValueList(Object[] parameters) throws EvaluationException
  {
    if (parameters[3] instanceof DataTable)
    {
      return ((DataTable) parameters[3]).stream()
          .map(dataRecord -> dataRecord.getValue(0))
          .collect(Collectors.toList());
    }
    else
    {
      throw new EvaluationException("Incorrect value: " + parameters[3] + ". Expected type: 'DataTable'");
    }
  }
}
