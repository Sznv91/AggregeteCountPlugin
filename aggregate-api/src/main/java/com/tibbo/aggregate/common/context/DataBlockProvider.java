package com.tibbo.aggregate.common.context;

import com.tibbo.aggregate.common.dao.DaoException;
import com.tibbo.aggregate.common.data.Data;

public interface DataBlockProvider
{
  Data provideDataBlock(long id) throws DaoException;
}
