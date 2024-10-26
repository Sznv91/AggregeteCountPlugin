package com.tibbo.aggregate.common.filter.converter;

import static com.tibbo.aggregate.common.filter.FilterApiConstants.PANE_FIELD_VALUE_PRESENCE;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.VALUE_PRESENCE_NOT_SET;
import static com.tibbo.aggregate.common.filter.FilterApiConstants.VALUE_PRESENCE_SET;

import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.filter.FunctionOperation;

public class FieldExpressionBuilder extends BaseExpressionBuilder
{
  
  public static void expandValuePresence(String columnName, DataTable paneTable, StringBuilder fb)
  {
    if (paneTable.rec().getInt(PANE_FIELD_VALUE_PRESENCE) == VALUE_PRESENCE_SET)
    {
      withLogicalAnd(fb);
      wrapSingleColumnArgument(fb, FunctionOperation.FilterFunctions.IS_NOT_NULL.getName(), columnName);
    }
    else if (paneTable.rec().getInt(PANE_FIELD_VALUE_PRESENCE) == VALUE_PRESENCE_NOT_SET)
    {
      withLogicalAnd(fb);
      wrapSingleColumnArgument(fb, FunctionOperation.FilterFunctions.IS_NULL.getName(), columnName);
    }
  }
  
  public static void wrapBinaryOperationColumn(StringBuilder sb, String columnName, String opCode, String value)
  {
    sb.append("{");
    sb.append(columnName);
    sb.append("} ");
    sb.append(opCode);
    sb.append(" ");
    sb.append(value);
  }
  
  public static void wrapSingleColumnArgument(StringBuilder sb, String funcName, String columnName)
  {
    sb.append(funcName);
    sb.append("({");
    sb.append(columnName);
    sb.append("})");
  }
  
  public static void wrapFunctionWithTwoParameters(StringBuilder sb, String funcName, String columnName, String value)
  {
    wrapFunctionWithTwoParametersNotBraced(sb, funcName, "{" + columnName + "}", value);
  }
  
  public static void wrapFunctionWithTwoParametersNotBraced(StringBuilder sb, String funcName, String arg1, String arg2)
  {
    sb.append(funcName);
    sb.append("(");
    sb.append(arg1);
    sb.append(", ");
    sb.append(arg2);
    sb.append(")");
  }
  
  public void withValuePresence(String columnName, DataTable paneTable)
  {
    expandValuePresence(columnName, paneTable, builder);
  }
}
