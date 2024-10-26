package com.tibbo.aggregate.common.expression.function.context;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.context.CallerController;
import com.tibbo.aggregate.common.context.Context;
import com.tibbo.aggregate.common.context.ContextException;
import com.tibbo.aggregate.common.context.EventDefinition;
import com.tibbo.aggregate.common.data.Event;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.DataTableConstruction;
import com.tibbo.aggregate.common.datatable.DataTableException;
import com.tibbo.aggregate.common.datatable.TableFormat;
import com.tibbo.aggregate.common.event.EventLevel;
import com.tibbo.aggregate.common.event.FireEventRequestController;
import com.tibbo.aggregate.common.expression.EvaluationEnvironment;
import com.tibbo.aggregate.common.expression.EvaluationException;
import com.tibbo.aggregate.common.expression.Evaluator;
import com.tibbo.aggregate.common.expression.Function;
import com.tibbo.aggregate.common.expression.function.AbstractFunction;
import com.tibbo.aggregate.common.util.Pair;
import com.tibbo.aggregate.common.util.SyntaxErrorException;
import com.tibbo.aggregate.common.util.Util;

public class FireEventFunction extends AbstractFunction
{
  public FireEventFunction()
  {
    this("fireEvent", "String context, String event, Integer level, Object parameter1, Object parameter2, ...", Cres.get().getString("fDescFireEvent"));
  }

  public FireEventFunction(String name, String parametersFootprint, String description)
  {
    super(name, Function.GROUP_CONTEXT_RELATED, parametersFootprint, "Long", description);
  }

  @Override
  public Object execute(Evaluator evaluator, EvaluationEnvironment environment, Object... parameters) throws EvaluationException
  {
    checkParameters(3, true, parameters);

    String contextPath = parameters[0].toString();
    Pair<Context, CallerController> contextAndCaller = resolveContext(parameters, contextPath, evaluator);
    Context<?> context = contextAndCaller.getFirst();
    CallerController caller = contextAndCaller.getSecond();
    
    try
    {
      String name = parameters[1].toString();
      
      EventDefinition ed = context.getEventDefinition(name);
      
      if (ed == null)
      {
        throw new ContextException(MessageFormat.format(Cres.get().getString("conEvtNotAvailExt"), name, context.getPath()));
      }
      
      Number level = Util.convertToNumber(parameters[2], true, true);
      
      if (level != null && !EventLevel.isValid(level.intValue()))
      {
        throw new EvaluationException("Invalid event level: " + level);
      }
      
      List<Object> input = Arrays.asList(Arrays.copyOfRange(parameters, 3, parameters.length));

      DataTable data = constructInputTable(input, ed, evaluator);

      Event ev = context.fireEvent(
          ed.getName(),
          (level != null) ? level.intValue() : ed.getLevel(),
          caller,
          environment.obtainPinpoint().map(FireEventRequestController::new).orElse(null),
          data);

      return ev != null ? ev.getId() : null;
    }
    catch (Exception ex)
    {
      throw new EvaluationException(ex);
    }
  }

  protected Pair<Context, CallerController> resolveContext(Object[] parameters, String contextPath, Evaluator evaluator) throws EvaluationException
  {
    return resolveContext(contextPath, evaluator);
  }

  protected DataTable constructInputTable(List<Object> input, EventDefinition ed, Evaluator evaluator) throws SyntaxErrorException, DataTableException, EvaluationException
  {
    if (input.size() == 1 && input.get(0) instanceof DataTable)
    {
      DataTable tmpTable = (DataTable) input.get(0);
      TableFormat tmpFormat = tmpTable.getFormat();
      TableFormat eventFormat = ed.getFormat();

      if (tmpFormat != null && eventFormat != null && (tmpFormat.getFieldCount() == eventFormat.getFieldCount()))
      {
        for (int i = 0; i < tmpFormat.getFieldCount(); i++)
        {
          if (tmpFormat.getField(i).getType() != eventFormat.getField(i).getType())
          {
            return DataTableConstruction.constructTable(input, eventFormat, evaluator, null);
          }
        }

        return tmpTable;
      }
    }

    return DataTableConstruction.constructTable(input, ed != null ? ed.getFormat() : null, evaluator, null);
  }
}
