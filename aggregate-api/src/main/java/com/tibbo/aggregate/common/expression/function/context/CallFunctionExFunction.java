package com.tibbo.aggregate.common.expression.function.context;

import java.util.List;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.context.CallerController;
import com.tibbo.aggregate.common.context.Context;
import com.tibbo.aggregate.common.context.FunctionDefinition;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.DataTableException;
import com.tibbo.aggregate.common.datatable.DataTableReplication;
import com.tibbo.aggregate.common.datatable.SimpleDataTable;
import com.tibbo.aggregate.common.expression.EvaluationException;
import com.tibbo.aggregate.common.expression.Evaluator;
import com.tibbo.aggregate.common.util.Pair;
import com.tibbo.aggregate.common.util.SyntaxErrorException;

public class CallFunctionExFunction extends CallFunctionFunction
{
  private static final int MINIMAL_COUNT = 3;
  
  public CallFunctionExFunction()
  {
    super("callFunctionEx", "String context, String function, DataTable parameters, [, String schema]", Cres.get().getString("fDescCallFunctionEx"), MINIMAL_COUNT);
  }
  
  @Override
  protected DataTable constructInputTable(List<Object> input, FunctionDefinition fd, Evaluator evaluator) throws SyntaxErrorException, DataTableException, EvaluationException
  {
    Object inputObject = input.get(0);
    if (!(inputObject instanceof DataTable))
    {
      throw new EvaluationException("Incorrect value: " + inputObject);
    }
    DataTable dataTable = (DataTable) inputObject;
    if (fd.getInputFormat() == null)
    {
      return dataTable;
    }
    DataTable defaultTable = new SimpleDataTable(fd.getInputFormat());
    defaultTable.addRecord();
    DataTableReplication.copy(dataTable, defaultTable, true, true, false, false);
    if (defaultTable.getFormat().equals(fd.getInputFormat()))
    {
      return dataTable;
    }
    throw new EvaluationException("Incorrect DataTable format: " + dataTable.getFormat());
  }
  
  @Override
  protected Pair<Context, CallerController> resolveContext(Object[] parameters, String contextPath, Evaluator evaluator) throws EvaluationException
  {
    return parameters.length > 3 ? resolveContext(contextPath, (String) parameters[3], evaluator) : super.resolveContext(parameters, contextPath, evaluator);
  }
}
