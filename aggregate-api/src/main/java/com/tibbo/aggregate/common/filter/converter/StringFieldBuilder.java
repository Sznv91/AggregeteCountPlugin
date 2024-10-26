package com.tibbo.aggregate.common.filter.converter;

import static com.tibbo.aggregate.common.filter.FilterApiConstants.PANE_FIELD_INCLUDED_IN;

import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.filter.FunctionOperation;

public class StringFieldBuilder extends FieldExpressionBuilder
{
  public void withStringFieldOperation(String columnName, DataTable paneTable, String paneField, String opCode)
  {
    String strValue = paneTable.rec().getString(paneField);
    withStringFieldOperation(columnName, strValue, opCode, true);
  }
  
  public void withStringFieldInOperation(String columnName, DataTable paneTable)
  {
    DataTable inValuesList = paneTable.rec().getDataTable(PANE_FIELD_INCLUDED_IN);
    String stringList = extractStrings(inValuesList);
    withStringFieldInOperation(columnName, stringList, true);
  }
  
  public void withStringFieldFunction(String columnName, DataTable paneTable, String paneField, String func)
  {
    String strValue = paneTable.rec().getString(paneField);
    withStringFieldFunction(columnName, strValue, func, true);
  }
  
  public void withStringFieldOperation(String columnName, String strValue, String opCode, boolean and)
  {
    if (strValue != null)
    {
      strValue = strValue.replaceAll("\\\\", "\\\\\\\\");
      strValue = strValue.replaceAll("'", "\\\\'");
      strValue = strValue.replaceAll("\"", "\\\\\"");
      combine(and);
      wrapBinaryOperationColumn(builder, columnName, opCode, "\"" + strValue + "\"");
    }
  }
  
  public void withStringFieldInOperation(String columnName, String stringList, boolean and)
  {
    if (!stringList.isEmpty())
    {
      combine(and);
      wrapFunctionWithTwoParameters(builder, FunctionOperation.FilterFunctions.IN.getName(), columnName, stringList);
    }
  }

  public void withStringFieldFunction(String columnName, String strValue, String func, boolean and)
  {
    if (strValue != null && !strValue.isEmpty())
    {
      strValue = strValue.replaceAll("\\\\", "\\\\\\\\");
      strValue = strValue.replaceAll("'", "\\\\'");
      strValue = strValue.replaceAll("\"", "\\\\\"");
      combine(and);
      wrapFunctionWithTwoParameters(builder, func, columnName, "\"" + strValue + "\"");
    }
  }

  private void combine(boolean and)
  {
    if (and)
    {
      withLogicalAnd();
    }
    else
    {
      withLogicalOr();
    }
  }

  private String extractStrings(DataTable inValuesList)
  {
    if (inValuesList == null || inValuesList.getRecordCount() == 0)
    {
      return "";
    }
    if (inValuesList.getFieldCount() != 1)
    {
      throw new IllegalArgumentException("Illegal table format. nx1 string table is expected");
    }
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (int i = 0; i < inValuesList.getRecordCount(); i++)
    {
      Object value = inValuesList.getRecord(i).getValue(0);
      if (!(value instanceof String))
      {
        throw new IllegalArgumentException("Illegal value type. String is expected, but found: " + value.getClass());
      }
      if (!first)
      {
        sb.append(", ");
      }
      
      value = ((String) value).replaceAll("\\\\", "\\\\\\\\");
      value = ((String) value).replaceAll("'", "\\\\'");
      value = ((String) value).replaceAll("\"", "\\\\\"");
      
      first = false;
      sb.append('\"');
      sb.append(value);
      sb.append('\"');
    }
    return sb.toString();
  }
}
