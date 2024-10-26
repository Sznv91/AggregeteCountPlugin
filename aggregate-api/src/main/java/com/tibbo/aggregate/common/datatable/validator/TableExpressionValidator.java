package com.tibbo.aggregate.common.datatable.validator;

import org.apache.commons.net.util.Base64;

import com.tibbo.aggregate.common.Log;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.TableFormat;
import com.tibbo.aggregate.common.datatable.ValidationException;
import com.tibbo.aggregate.common.expression.Evaluator;
import com.tibbo.aggregate.common.expression.Expression;
import com.tibbo.aggregate.common.expression.ExpressionUtils;
import com.tibbo.aggregate.common.util.SyntaxErrorException;

public class TableExpressionValidator extends AbstractTableValidator
{
  private final Expression expression;
  
  public TableExpressionValidator(String expression)
  {
    expression = tryParse(expression);
    this.expression = new Expression(expression);
  }

  // FIXME 6.4 To replace tryParse() after 6.4 release. Requires to fix the broken data.
  private String updateIfNeeded(String expression)
  {
    if (Base64.isArrayByteBase64(expression.getBytes()))
    {
      return new String(Base64.decodeBase64(expression.getBytes()));
    }
    return expression;
  }

  private String tryParse(String expression)
  {
    boolean parsed = false;
    int tries = 10;
    String decoded = expression;
    do
    {
      try
      {
        ExpressionUtils.parse(new Expression(decoded), true);
        parsed = true;
      }
      catch (SyntaxErrorException e)
      {
        tries--;
        decoded = new String(Base64.decodeBase64(decoded.getBytes()));
      }
    }
    while (!parsed && tries > 0);
    if (parsed)
    {
      return decoded;
    }
    return expression;
  }
  
  @Override
  public Character getType()
  {
    return TableFormat.TABLE_VALIDATOR_EXPRESSION;
  }
  
  @Override
  public String encode()
  {
    return Base64.encodeBase64StringUnChunked(expression.getText().getBytes());
  }
  
  @Override
  public void validate(DataTable table) throws ValidationException
  {
    Evaluator evaluator = new Evaluator(table);
    
    try
    {
      Object result = evaluator.evaluate(expression);
      
      if (result != null)
      {
        throw new ValidationException(result.toString());
      }
    }
    catch (ValidationException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      Log.DATATABLE.warn("Error evaluating data table validator's expression '" + expression + "': " + ex.getMessage(), ex);
    }
  }
}
