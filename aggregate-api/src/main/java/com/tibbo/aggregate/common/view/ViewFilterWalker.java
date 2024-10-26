package com.tibbo.aggregate.common.view;

import java.util.Iterator;

import com.tibbo.aggregate.common.datatable.DataRecord;
import com.tibbo.aggregate.common.datatable.DataTable;

/**
 * Filter tree walker
 */
public class ViewFilterWalker
{
  
  private final ViewFilterProcessor processor;
  
  public ViewFilterWalker(ViewFilterProcessor processor)
  {
    this.processor = processor;
  }
  
  public ViewFilterProcessResult walk(DataTable filterTable)
  {
    ViewFilterProcessResult result = ViewFilterProcessResult.OK;
    if (filterTable != null)
    {
      for (Iterator<DataRecord> iterator = filterTable.iterator(); iterator.hasNext();)
      {
        DataRecord filter = iterator.next();
        ViewFilterProcessResult innerResult = processor.processResult(walkRecord(filter));
        if (innerResult == ViewFilterProcessResult.ERROR)
        {
          iterator.remove();
          if (filterTable.getRecordCount() == 0)
          {
            return ViewFilterProcessResult.ERROR;
          }
        }
        else if (innerResult == ViewFilterProcessResult.HAS_TO_PROCESS_FULLY)
        {
          result = ViewFilterProcessResult.HAS_TO_PROCESS_FULLY;
        }
      }
    }
    return result;
  }
  
  private ViewFilterProcessResult walkRecord(DataRecord filter)
  {
    if (filter.getInt(ViewFilterElement.FIELD_TYPE) == ViewFilterElement.TYPE_NESTED_CONDITIONS)
    {
      return walk(filter.getDataTable(ViewFilterElement.FIELD_NESTED));
    }
    
    return processor.processRecord(filter);
  }
}
