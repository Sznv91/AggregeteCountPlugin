package com.tibbo.aggregate.common.expression.function.type;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.Log;
import com.tibbo.aggregate.common.context.ContextException;
import com.tibbo.aggregate.common.data.Data;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.expression.EvaluationEnvironment;
import com.tibbo.aggregate.common.expression.EvaluationException;
import com.tibbo.aggregate.common.expression.Evaluator;
import com.tibbo.aggregate.common.expression.Function;
import com.tibbo.aggregate.common.expression.ReferenceResolver;
import com.tibbo.aggregate.common.expression.function.AbstractFunction;

public class StringFunction extends AbstractFunction
{
  public StringFunction()
  {
    super("string", Function.GROUP_TYPE_CONVERSION, "Object value [, String charset]", "String", Cres.get().getString("fDescString"));
  }
  
  @Override
  public Object execute(Evaluator evaluator, EvaluationEnvironment environment, Object... parameters) throws EvaluationException
  {
    checkParameters(1, true, parameters);
    
    Object value = parameters[0];
    
    if (value == null)
    {
      return null;
    }
    
    if (value instanceof DataTable)
    {
      DataTable parameterTable = (DataTable) value;
      if (parameterTable.getRecordCount() == 1 && parameterTable.get() instanceof Data)
      {
        value = parameterTable.get();
      }
    }
    if (value instanceof Data)
    {
      if (evaluator != null)
      {
        ReferenceResolver resolver = evaluator.getDefaultResolver();
        try
        {
          ((Data) value).fetchData(resolver.getContextManager(), resolver.getCallerController());
        }
        catch (ContextException e)
        {
          Log.CONTEXT_FUNCTIONS.error("Unable to fetch the data", e);
        }
      }
      
      Charset charset = null;
      if (parameters.length > 1)
      {
        try
        {
          charset = Charset.forName((String) parameters[1]);
        }
        catch (Exception ex)
        {
          throw new EvaluationException(ex.getLocalizedMessage(), ex);
        }
      }
      
      if (charset == null)
      {
        charset = StandardCharsets.UTF_8;
      }
      
      return ((Data) value).toCleanString(charset);
    }
    
    return value.toString();
  }
}
