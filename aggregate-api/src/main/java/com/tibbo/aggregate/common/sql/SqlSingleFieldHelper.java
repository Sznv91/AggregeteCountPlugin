package com.tibbo.aggregate.common.sql;

import java.util.Map;

import com.tibbo.aggregate.common.datatable.DataRecord;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.SimpleDataTable;
import com.tibbo.aggregate.common.view.ViewFilterElement;

/**
 * Helper to prepare the data to identify the single field
 */
public class SqlSingleFieldHelper
{
  private final String tableName;
  private final String columnName;
  private final Map<String, Object> primaryKeys;
  
  public SqlSingleFieldHelper(String tableName, String columnName, Map<String, Object> primaryKeys)
  {
    this.tableName = tableName;
    this.columnName = columnName;
    this.primaryKeys = primaryKeys;
  }
  
  public String getColumnName()
  {
    return columnName;
  }
  
  public DataTable prepareFilter()
  {
    SimpleDataTable filter = new SimpleDataTable(ViewFilterElement.FORMAT);
    
    for (String column : primaryKeys.keySet())
    {
      DataRecord efr = filter.addRecord();
      efr.setValue(ViewFilterElement.FIELD_LOGICAL, ViewFilterElement.LOGICAL_OPERATION_AND);
      efr.setValue(ViewFilterElement.FIELD_TYPE, ViewFilterElement.TYPE_CONDITION);
      efr.setValue(ViewFilterElement.FIELD_COLUMN, column);
      efr.setValue(ViewFilterElement.FIELD_TABLE, tableName);
      efr.setValue(ViewFilterElement.FIELD_VALUE, primaryKeys.get(column));
    }
    return filter;
  }
}
