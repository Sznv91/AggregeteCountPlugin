package com.tibbo.aggregate.common.sql;

import java.util.Map;

import com.tibbo.aggregate.common.datatable.DataTable;

public class WrapResult
{
  private final Integer fullSize;
  private final DataTable data;
  private final Map<Long, SqlSingleFieldHelper> dataBlocks;
  
  public WrapResult(Integer fullSize, DataTable data, Map<Long, SqlSingleFieldHelper> dataBlocks)
  {
    super();
    this.fullSize = fullSize;
    this.data = data;
    this.dataBlocks = dataBlocks;
  }
  
  public Integer getFullSize()
  {
    return fullSize;
  }
  
  public DataTable getData()
  {
    return data;
  }

  public Map<Long, SqlSingleFieldHelper> getDataBlockColumns()
  {
    return dataBlocks;
  }
}
