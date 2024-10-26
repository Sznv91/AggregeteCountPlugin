package com.tibbo.aggregate.common.datatable;

import java.util.HashMap;
import java.util.Map;

import com.tibbo.aggregate.common.context.CallerController;
import com.tibbo.aggregate.common.context.ContextException;
import com.tibbo.aggregate.common.context.ContextManager;
import com.tibbo.aggregate.common.data.Data;

public class DataBlocksPreprocessor
{
    public enum Mode
    {
        INLINE_BLOCKS_WITH_ID,
        INLINE_BLOCKS_WITHOUT_ID,
        CACHE_BLOCKS
    }

    private final Mode mode;
    private Map<Long, Data> dataBlocksCache;

    public DataBlocksPreprocessor()
    {
        this(Mode.INLINE_BLOCKS_WITHOUT_ID);
    }

    public DataBlocksPreprocessor(Mode mode)
    {
        this.mode = mode;
        if (this.mode == Mode.CACHE_BLOCKS)
        {
            dataBlocksCache = new HashMap<>();
        }
    }

    public Map<Long, Data> getDataBlocks()
    {
        return dataBlocksCache;
    }

    public void process(DataTable table, ContextManager cm, CallerController cc) throws ContextException
    {
        if (table == null)
        {
            return;
        }

        for (FieldFormat ff : table.getFormat())
        {
            if (ff.getType() == FieldFormat.DATA_FIELD)
            {
                for (DataRecord rec : table)
                {
                    Data data = rec.getData(ff.getName());
                    if (data == null || data.getId() == null)
                    {
                        continue;
                    }

                    if (mode == Mode.CACHE_BLOCKS)
                    {
                        if (dataBlocksCache.containsKey(data.getId()))
                        {
                            rec.setValue(ff.getName(), new Data(data.getId()));
                        }
                        else
                        {
                            data.fetchData(cm, cc);

                            if (data.getData() != null)
                            {
                                dataBlocksCache.put(data.getId(), data);
                            }
                        }
                    }
                    else
                    {
                        data.fetchData(cm, cc);

                        if (mode == Mode.INLINE_BLOCKS_WITHOUT_ID)
                        {
                            data.setId(null);
                        }
                    }
                }
            }

            if (ff.getType() == FieldFormat.DATATABLE_FIELD)
            {
                for (DataRecord rec : table)
                {
                    DataTable dt = rec.getDataTable(ff.getName());
                    process(dt, cm, cc);
                }
            }
        }
    }
}
