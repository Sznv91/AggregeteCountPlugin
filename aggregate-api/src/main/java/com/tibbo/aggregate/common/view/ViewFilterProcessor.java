package com.tibbo.aggregate.common.view;

import com.tibbo.aggregate.common.datatable.DataRecord;

/**
 * Filter record processor
 */
public interface ViewFilterProcessor
{
  ViewFilterProcessResult processRecord(DataRecord viewFilter);

  default ViewFilterProcessResult processResult(ViewFilterProcessResult innerResult)
  {
    return innerResult;
  }
}
