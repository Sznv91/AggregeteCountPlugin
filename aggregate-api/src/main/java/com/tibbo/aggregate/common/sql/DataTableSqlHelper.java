package com.tibbo.aggregate.common.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tibbo.aggregate.common.Cres;
import com.tibbo.aggregate.common.Log;
import com.tibbo.aggregate.common.context.CallerController;
import com.tibbo.aggregate.common.context.ContextException;
import com.tibbo.aggregate.common.data.Data;
import com.tibbo.aggregate.common.datatable.DataRecord;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.DataTableBuilding;
import com.tibbo.aggregate.common.datatable.DataTableReplication;
import com.tibbo.aggregate.common.datatable.FieldFormat;
import com.tibbo.aggregate.common.datatable.SimpleDataTable;
import com.tibbo.aggregate.common.datatable.TableFormat;
import com.tibbo.aggregate.common.datatable.encoding.ClassicEncodingSettings;
import com.tibbo.aggregate.common.query.DataTableWrappingUtils;
import com.tibbo.aggregate.common.query.FieldDescriptor;
import com.tibbo.aggregate.common.query.TypeMapper;
import com.tibbo.aggregate.common.server.ClassContextConstants;
import com.tibbo.aggregate.common.util.WatchdogHolder;
import com.tibbo.aggregate.common.view.StorageException;
import com.tibbo.aggregate.common.view.StorageHelper;

public class DataTableSqlHelper
{
  public static WrapResult wrap(ResultSet rs, CallerController callerController) throws SQLException, StorageException
  {
    return wrap(rs, callerController, null, null, true, false);
  }
  
  public static WrapResult wrap(ResultSet rs, CallerController callerController, Integer first, Integer count, boolean fixRecords, boolean lazyDataBlockFetching) throws SQLException, StorageException
  {
    return wrap(rs, SqlTablesMetadata.EMPTY, callerController, first, count, fixRecords, lazyDataBlockFetching);
  }
  
  public static WrapResult wrap(ResultSet rs, SqlTablesMetadata sqlTablesMetadata, CallerController callerController, Integer first, Integer count, boolean fixRecords, boolean lazyDataBlockFetching)
      throws SQLException, StorageException
  {
    // Linked map is required for getting data from ResultSet by indexes, not by names
    // Names of ResultSet columns may be void
    Map<String, FieldDescriptor> fields = DataTableWrappingUtils.extractResultSetFields(rs, sqlTablesMetadata, false);
    
    TableFormat rf = createTableFormat(fields.values(), callerController);
    
    DataTable dataTable = new SimpleDataTable(rf);
    Map<Long, SqlSingleFieldHelper> dataBlocks = new ConcurrentHashMap<>();
    Integer fullSize = null;
    
    if ((first != null && !scroll(rs, first)) || (first == null && !rs.next()))
      return new WrapResult(null, dataTable, dataBlocks);
    
    int remaining = count != null ? count : Integer.MAX_VALUE;
    
    int found = 0;
    
    fullSize = count;
    
    while (remaining > 0)
    {
      remaining--;
      found++;
      
      if (!WatchdogHolder.getInstance().isEnoughMemory())
      {
        throw new StorageException(Cres.get().getString("storageMemoryOverflow") + String.valueOf(found));
      }
      
      DataRecord record = populateRecord(rs, fields.values(), rf);
      
      if (lazyDataBlockFetching)
      {
        
        dataBlocks.putAll(extractDataBlocks(record, fields));
      }
      
      if (!rf.equals(record.getFormat()))
      {
        rf = record.getFormat().clone();
        dataTable.setFormat(rf);
        
        for (FieldFormat<?> ff : rf.getFields())
        {
          FieldDescriptor fd = fields.get(ff.getName());
          TypeMapper.Type curType = TypeMapper.getSqlTypeForFieldFormat(ff.getType());
          if (fd.getType() != curType)
          {
            fields.put(fd.getFieldName(), new FieldDescriptor(fd.getColumnName(), fd.getTableName(), fd.getFieldName(), fd.getFieldDescription(), curType, fd.isPrimaryKey()));
          }
        }
      }
      
      dataTable.addRecord(record);
      if (!rs.next())
      {
        fullSize = (first != null ? first : 1) + found - 1;
        break;
      }
    }
    
    if (fixRecords)
    {
      dataTable.fixRecords();
    }
    
    return new WrapResult(fullSize, dataTable, dataBlocks);
  }

  private static Map<Long, SqlSingleFieldHelper> extractDataBlocks(DataRecord record, Map<String, FieldDescriptor> fieldDescriptorMap)
  {
    Map<Long, SqlSingleFieldHelper> dataBlocks = new HashMap<>();
    record.getFormat().getFields().stream().filter(ff -> ff.getType() == FieldFormat.DATA_FIELD).forEach(fieldFormat -> {
      Data data = record.getData(fieldFormat.getName());
      if (data != null)
      {
        if (data.getId() == null)
        {
          return;
        }
        Map<String, Object> primaryKeys = fieldDescriptorMap.entrySet().stream()
            .filter(e -> e.getValue().isPrimaryKey())
            .map(Map.Entry::getKey)
            .collect(Collectors.toMap(Function.identity(), record::getValue));
        FieldDescriptor fieldDescriptor = fieldDescriptorMap.get(fieldFormat.getName());
        SqlSingleFieldHelper sqlDataBlockQuery = new SqlSingleFieldHelper(fieldDescriptor.getTableName(), fieldDescriptor.getColumnName(), primaryKeys);
        dataBlocks.put(data.getId(), sqlDataBlockQuery);
        // To clean dataBlock from the result set to provide lazy fetching
        data.setData(null);
      }
    });
    return dataBlocks;
  }
  
  private static boolean scroll(ResultSet rs, int first) throws SQLException
  {
    if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY)
      return rs.absolute(first);
    
    int count = 0;
    while (rs.next())
    {
      if (++count == first)
        return true;
    }
    return false;
  }
  
  public static DataRecord populateRecord(ResultSet rs, Collection<FieldDescriptor> fields, TableFormat rf) throws SQLException
  {
    DataRecord record = new DataRecord(rf);
    int columnIndex = 1;
    for (FieldDescriptor fd : fields)
    {
      Object value = null;
      
      String columnName = fd.getColumnName();
      
      List<FieldFormat> ffs = rf.getFields();
      
      for (FieldFormat ff : ffs)
      {
        if (DataTableWrappingUtils.escapeColumnName(columnName).equals(ff.getName()))
        {
          value = DataTableWrappingUtils.getFieldValue(rs, columnIndex, ff);
          if (value == null && !ff.isNullable())
          {
            // Sometimes SQL returns null values for non-nullable fields
            throw new SQLException("SQL query returned NULL value for non-nullable field: " + ff);
          }
          
          if (fd.getType() == TypeMapper.Type.CLOB && value instanceof String)
          {
            TableFormat adjustedRF = new TableFormat(rf.getMinRecords(), rf.getMaxRecords());
            for (FieldFormat<?> curFF : rf.getFields())
            {
              adjustedRF.addField(curFF != ff ? curFF : FieldFormat.create(curFF.getName(), FieldFormat.STRING_FIELD, curFF.getDescription(), curFF.getDefaultValue(), curFF.isNullable()));
            }
            DataRecord adjustedRecord = new DataRecord(adjustedRF);
            DataTableReplication.copyRecord(record, adjustedRecord, true, true);
            record = adjustedRecord;
            rf = adjustedRF;
          }
          
          record.setValue(ff.getName(), value);
          break;
        }
      }
      columnIndex++;
    }
    return record;
  }
  
  public static TableFormat createTableFormat(ResultSet rs, CallerController callerController) throws SQLException
  {
    ResultSetMetaData rsmd = rs.getMetaData();
    
    // Linked map is required for getting data from ResultSet by indexes, not by names
    // Names of ResultSet columns may be void
    Map<String, FieldDescriptor> fields = DataTableWrappingUtils.extractResultSetFields(rs, false);
    
    return createTableFormat(fields.values(), callerController);
  }
  
  public static TableFormat createTableFormat(Collection<FieldDescriptor> fields, CallerController callerController)
  {
    TableFormat rf = new TableFormat();
    
    for (FieldDescriptor fd : fields)
    {
      FieldFormat ff = FieldFormat.create(fd.getFieldName(), TypeMapper.getFieldFormatByType(fd.getType()));
      ff.setNullable(true);
      ff.setDescription(fd.getFieldDescription());
      rf.addField(ff);
    }
    
    return rf;
  }
  
  public static DataTable convertToOutputFormat(DataTable source, TableFormat outputFormat, boolean addMissingFields, boolean sortBySourceFieldOrder, DataTable viewFields, DataRecord viewRec)
  {
    TableFormat newFormat = outputFormat.clone();
    List<String> removeList = new LinkedList<String>();
    for (Iterator<FieldFormat> iterator = newFormat.iterator(); iterator.hasNext();)
    {
      FieldFormat newFf = iterator.next();
      
      FieldFormat origFf = source.getFormat(newFf.getName());
      
      if (origFf != null)
      {
        if (!newFf.hasDescription())
        {
          newFf.setDescription(origFf.getDescription());
        }
        
        if (origFf.hasSelectionValues() && !newFf.hasSelectionValues())
        {
          for (Object value : origFf.getSelectionValues().keySet())
          {
            newFf.addSelectionValue(newFf.valueFromString(origFf.valueToString(value)), origFf.getSelectionValues().get(value).toString());
          }
        }
      }
      else if (!addMissingFields)
      {
        removeList.add(newFf.getName());
      }
    }
    
    for (String removeName : removeList)
      newFormat.removeField(removeName);
    
    for (FieldFormat origFf : source.getFormat().clone())
    {
      if (!newFormat.hasField(origFf.getName()))
      {
        newFormat.addField(origFf);
      }
    }
    
    if (viewFields != null)
    {
      for (DataRecord viewField : viewFields)
      {
        String columnName = viewField.getString(StorageHelper.FIELD_COLUMNS_NAME).toLowerCase();
        if (!newFormat.hasField(columnName) && viewField.hasField(StorageHelper.FIELD_COLUMNS_IS_CALCULATED_FIELD)
            && viewField.getBoolean(StorageHelper.FIELD_COLUMNS_IS_CALCULATED_FIELD))
        {
          try
          {
            newFormat = addCalculatedFields(newFormat, viewField);
          }
          catch (ContextException e)
          {
            Log.DATATABLE.debug(e.getMessage(), e);
          }
        }
        if (newFormat.hasField(columnName))
        {
          if (viewField.hasField(StorageHelper.FIELD_COLUMNS_READONLY))
          {
            newFormat.getField(columnName).setReadonly(viewField.getBoolean(StorageHelper.FIELD_COLUMNS_READONLY));
          }
          if (viewField.hasField(StorageHelper.FIELD_COLUMNS_VISIBILITY))
          {
            newFormat.getField(columnName).setHidden(
                !viewField.getValueAsString(StorageHelper.FIELD_COLUMNS_VISIBILITY).equals(String.valueOf(StorageHelper.VISIBILITY_VISIBLE)));
          }
        }
      }
    }
    
    if (viewRec != null && viewRec.getDataTable(ClassContextConstants.VF_VIEWS_FIELDS) != null && viewRec.getDataTable(ClassContextConstants.VF_VIEWS_FIELDS).getRecordCount() > 0)
    {
      DataTable orderTable = viewRec.getDataTable(ClassContextConstants.VF_VIEWS_FIELDS);
      TableFormat newReorderFormat = newFormat.clone();
      for (DataRecord orderRec : orderTable)
      {
        if (newReorderFormat.hasField(orderRec.getString(StorageHelper.FIELD_COLUMNS_NAME)))
        {
          newReorderFormat.removeField(orderRec.getString(StorageHelper.FIELD_COLUMNS_NAME));
        }
      }
      for (int i = 0; i < orderTable.getRecordCount(); i++)
      {
        DataRecord orderRec = orderTable.getRecord(i);
        if (newFormat.hasField(orderRec.getString(StorageHelper.FIELD_COLUMNS_NAME)))
        {
          newReorderFormat.addField(newFormat.getField(orderRec.getString(StorageHelper.FIELD_COLUMNS_NAME)));
        }
      }
      newReorderFormat.setBindings(newFormat.getBindings());
      newFormat = newReorderFormat;
    }
    
    DataTable converted;
    if (sortBySourceFieldOrder)
    {
      TableFormat sortedFormat = outputFormat.clone();
      for (FieldFormat ff : sortedFormat.getFields())
        removeList.add(ff.getName());
      for (String removeName : removeList)
        sortedFormat.removeField(removeName);
      for (FieldFormat origFf : source.getFormat().clone())
      {
        if (newFormat.hasField(origFf.getName()))
        {
          sortedFormat.addField(newFormat.getField(origFf.getName()));
        }
      }
      converted = new SimpleDataTable(sortedFormat);
    }
    else
      converted = new SimpleDataTable(newFormat);
    
    DataTableReplication.copy(source, converted, true, true, true);
    
    return converted;
  }
  
  public static TableFormat addCalculatedFields(TableFormat fieldFormats, DataRecord colRecord) throws ContextException
  {
    TableFormat fieldFormatsClone = fieldFormats.clone();
    String formatName = colRecord.getString(StorageHelper.FIELD_COLUMNS_NAME);
    if (!colRecord.hasField(StorageHelper.FIELD_COLUMNS_FIELD_DATATABLE) || colRecord.getDataTable(StorageHelper.FIELD_COLUMNS_FIELD_DATATABLE) == null
        || colRecord.getDataTable(StorageHelper.FIELD_COLUMNS_FIELD_DATATABLE).getRecordCount() == 0)
      return fieldFormats;
    TableFormat addFormat = DataTableBuilding.createTableFormat(0, Integer.MAX_VALUE, false, colRecord.getDataTable(StorageHelper.FIELD_COLUMNS_FIELD_DATATABLE), new ClassicEncodingSettings(true));
    if (addFormat != null && addFormat.getFieldCount() > 0)
    {
      for (FieldFormat ff : addFormat)
      {
        ff.setName(formatName);
        if (!fieldFormatsClone.hasField(formatName))
          fieldFormatsClone.addField(ff);
      }
    }
    return fieldFormatsClone;
  }
}
